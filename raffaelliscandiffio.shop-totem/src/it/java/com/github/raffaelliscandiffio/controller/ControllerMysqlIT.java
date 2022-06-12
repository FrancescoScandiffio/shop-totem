package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;

class ControllerMysqlIT {

	private static final String DATABASE_NAME = "totem";

	private static final String PRODUCT_NAME_1 = "product_1";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final int LOW_QUANTITY = 2;
	private static final int MID_QUANTITY = 4;
	private static final int GREAT_QUANTITY = 10;
	private static final double PRICE = 2.0;
	private static final OrderStatus ORDER_OPEN = OrderStatus.OPEN;
	private static final OrderStatus ORDER_CLOSED = OrderStatus.CLOSED;

	private static EntityManagerFactory managerFactory;
	private EntityManager entityManager;

	private TransactionManagerMySql transactionManager;
	private ShoppingService serviceLayer;
	private TotemSwingView view;
	private TotemController controller;

	@BeforeAll
	public static void createEntityManagerFactory() {
		System.setProperty("db.port", "3306");
		System.setProperty("db.name", DATABASE_NAME);
		managerFactory = Persistence.createEntityManagerFactory("mysql-test");
	}

	@AfterAll
	public static void closeEntityManagerFactory() {
		managerFactory.close();
	}

	@BeforeEach()
	public void setup() {
		entityManager = managerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();

		transactionManager = new TransactionManagerMySql(entityManager);
		serviceLayer = new ShoppingService(transactionManager);
		view = mock(TotemSwingView.class);
		controller = new TotemController(serviceLayer, view);
	}

	@AfterEach
	public void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	void testStartShoppingIT() {
		controller.startShopping();

		List<Order> orders = getAllOrders();
		assertThat(orders).singleElement().usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new Order(ORDER_OPEN));

		verify(view).setOrderId(orders.get(0).getId());
		verify(view).showAllProducts(getAllProducts());
		verify(view, never()).showShoppingErrorMessage(any());
	}

	@Test
	void testOpenShoppingIT() {
		Product product_1 = new Product(PRODUCT_NAME_1, PRICE);
		Product product_2 = new Product(PRODUCT_NAME_2, PRICE);
		persistObjects(asList(product_1, product_2));

		controller.openShopping();
		verify(view).showAllProducts(getAllProducts());
		verify(view, never()).showShoppingErrorMessage(any());
	}

	@Test
	void testCancelShoppingIT() {
		Product product_1 = new Product(PRODUCT_NAME_1, PRICE);
		Product product_2 = new Product(PRODUCT_NAME_2, PRICE);
		Stock stock_1 = new Stock(product_1, MID_QUANTITY);
		Stock stock_2 = new Stock(product_2, MID_QUANTITY);
		Order order_1 = new Order(ORDER_CLOSED);
		Order order_2 = new Order(ORDER_OPEN);
		OrderItem item_1 = new OrderItem(product_1, order_1, MID_QUANTITY);
		OrderItem item_2 = new OrderItem(product_1, order_2, LOW_QUANTITY);
		OrderItem item_3 = new OrderItem(product_2, order_2, GREAT_QUANTITY);
		persistObjects(asList(product_1, product_2, stock_1, stock_2, order_1, order_2, item_1, item_2, item_3));
		String orderToDelete = order_2.getId();
		Stock modifiedStock_1 = newStockWithId(stock_1.getId(), product_1, MID_QUANTITY + LOW_QUANTITY);
		Stock modifiedStock_2 = newStockWithId(stock_2.getId(), product_2, MID_QUANTITY + GREAT_QUANTITY);

		controller.cancelShopping(orderToDelete);

		assertThat(getAllOrders()).containsExactly(order_1);
		assertThat(getAllItems()).containsExactly(item_1);
		assertThat(getAllStocks()).containsExactlyInAnyOrder(modifiedStock_1, modifiedStock_2);
		verify(view, never()).showShoppingErrorMessage(any());
	}

	@Test
	void testBuyProductIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, GREAT_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		persistObjects(asList(product, stock, order));
		String orderToModify = order.getId();
		String productToBuy = product.getId();
		Stock modifiedStock = newStockWithId(stock.getId(), product, GREAT_QUANTITY - MID_QUANTITY);
		when(view.getOrderId()).thenReturn(orderToModify);

		controller.buyProduct(orderToModify, productToBuy, MID_QUANTITY);

		List<OrderItem> items = getAllItems();
		assertThat(items).singleElement().usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new OrderItem(product, order, MID_QUANTITY));
		assertThat(getAllStocks()).containsExactly(modifiedStock);
		verify(view).showAllOrderItems(asList(items.get(0)));
		verify(view).showShoppingMessage("Added " + MID_QUANTITY + " " + PRODUCT_NAME_1);
		verify(view, never()).showShoppingErrorMessage(any());
	}

	@Test
	void testBuyProductWhenItemExistsIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, GREAT_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item = new OrderItem(product, order, LOW_QUANTITY);
		persistObjects(asList(product, stock, order, item));
		String orderToModify = order.getId();
		String productToBuy = product.getId();
		OrderItem modifiedItem = newItemWithId(item.getId(), product, order, LOW_QUANTITY + MID_QUANTITY);
		Stock modifiedStock = newStockWithId(stock.getId(), product, GREAT_QUANTITY - MID_QUANTITY);
		
		when(view.getOrderId()).thenReturn(orderToModify);

		controller.buyProduct(orderToModify, productToBuy, MID_QUANTITY);
		
		assertThat(getAllItems()).containsExactly(modifiedItem);
		assertThat(getAllStocks()).containsExactly(modifiedStock);
		verify(view).showAllOrderItems(asList(modifiedItem));
		verify(view).showShoppingMessage("Added " + MID_QUANTITY + " " + PRODUCT_NAME_1);
		verify(view, never()).showShoppingErrorMessage(any());
	}

	@Test
	void testBuyProductWhenExceptionIsThrownIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item = new OrderItem(product, order, MID_QUANTITY);
		persistObjects(asList(product, stock, order, item));
		String orderToModify = order.getId();
		String productToBuy = product.getId();

		controller.buyProduct(orderToModify, productToBuy, GREAT_QUANTITY);

		assertThat(getAllItems()).containsExactly(item);
		assertThat(getAllStocks()).containsExactly(stock);
		verify(view).showShoppingErrorMessage("Not enough quantity. Cannot buy product: " + PRODUCT_NAME_1);
	}

	@Test
	void testRemoveItemIT() {
		Product product_1 = new Product(PRODUCT_NAME_1, PRICE);
		Product product_2 = new Product(PRODUCT_NAME_2, PRICE);
		Stock stock = new Stock(product_1, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item_1 = new OrderItem(product_1, order, MID_QUANTITY);
		OrderItem item_2 = new OrderItem(product_2, order, MID_QUANTITY);

		persistObjects(asList(product_1, product_2, stock, order, item_1, item_2));

		controller.removeItem(item_1);

		assertThat(getAllItems()).containsExactly(item_2);
		verify(view).showCartMessage("Removed all " + PRODUCT_NAME_1);
		verify(view, never()).resetView();
	}

	@Test
	void testRemoveItemWhenExceptionIsThrownIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item = new OrderItem(product, order, MID_QUANTITY);
		String fakeId = "fake_id";
		item.setId(fakeId);
		persistObjects(asList(product, stock, order));
		when(view.getOrderId()).thenReturn(order.getId());

		controller.removeItem(item);

		assertThat(getAllStocks()).containsExactly(stock);
		assertThat(getAllItems()).isEmpty();
		verify(view).resetView();
		verify(view).showAllProducts(getAllProducts());
		verify(view).showAllOrderItems(getAllItemsByOrderId(order.getId()));
		verify(view).showCartErrorMessage("Item not found: " + fakeId);

	}

	@Test
	void testReturnItemIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item = new OrderItem(product, order, GREAT_QUANTITY);
		persistObjects(asList(product, stock, order, item));

		Stock stockModified = newStockWithId(stock.getId(), product, MID_QUANTITY + LOW_QUANTITY);
		OrderItem itemModified = newItemWithId(item.getId(), product, order, GREAT_QUANTITY - LOW_QUANTITY);

		controller.returnItem(item, LOW_QUANTITY);

		assertThat(getAllItems()).containsExactly(itemModified);
		assertThat(getAllStocks()).containsExactly(stockModified);
		verify(view).itemModified(item, itemModified);
		verify(view).showCartMessage("Removed " + LOW_QUANTITY + " " + PRODUCT_NAME_1);
		verify(view, never()).resetView();

	}

	@Test
	void testReturnItemWhenExceptionIsThrownIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item = new OrderItem(product, order, GREAT_QUANTITY);
		String fakeId = "fake_id";
		item.setId(fakeId);
		persistObjects(asList(product, stock, order));
		when(view.getOrderId()).thenReturn(order.getId());

		controller.returnItem(item, MID_QUANTITY);

		verify(view).resetView();
		verify(view).showAllProducts(getAllProducts());
		verify(view).showAllOrderItems(getAllItemsByOrderId(order.getId()));
		verify(view).showCartErrorMessage("Item not found: " + fakeId);
		assertThat(getAllStocks()).containsExactly(stock);
		assertThat(getAllItems()).isEmpty();
	}

	@Test
	void testCheckoutIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		OrderItem item = new OrderItem(product, order, GREAT_QUANTITY);
		persistObjects(asList(product, stock, order, item));
		String orderToCheckout = order.getId();
		Order confirmedOrder = new Order(ORDER_CLOSED);
		confirmedOrder.setId(orderToCheckout);

		controller.checkout(orderToCheckout);

		assertThat(getAllOrders()).containsExactly(confirmedOrder);
		verify(view, never()).showCartErrorMessage(any());
	}

	@Test
	void testCheckoutWhenExceptionIsThrownIT() {
		Product product = new Product(PRODUCT_NAME_1, PRICE);
		Stock stock = new Stock(product, MID_QUANTITY);
		Order order = new Order(ORDER_OPEN);
		String fakeId = "fake_id";
		order.setId(fakeId);
		persistObjects(asList(product, stock));
		String orderToCheckout = order.getId();
		Order confirmedOrder = new Order(ORDER_CLOSED);
		confirmedOrder.setId(orderToCheckout);

		controller.checkout(orderToCheckout);

		verify(view).showCartErrorMessage("Order not found: " + fakeId);

	}

	// Private utility methods

	private List<Product> getAllProducts() {
		return entityManager.createQuery("SELECT p FROM Product p", Product.class).getResultList();
	}

	private List<Stock> getAllStocks() {
		return entityManager.createQuery("SELECT s FROM Stock s", Stock.class).getResultList();
	}

	private List<Order> getAllOrders() {
		return entityManager.createQuery("SELECT o FROM Order o", Order.class).getResultList();
	}

	private List<OrderItem> getAllItems() {
		return entityManager.createQuery("SELECT i FROM OrderItem i", OrderItem.class).getResultList();
	}

	private List<OrderItem> getAllItemsByOrderId(String orderId) {
		return entityManager.createQuery("SELECT i FROM OrderItem i WHERE i.order.id =:orderId", OrderItem.class)
				.setParameter("orderId", orderId).getResultList();
	}

	private void persistObjects(List<Object> objects) {
		entityManager.getTransaction().begin();
		for (Object obj : objects)
			entityManager.persist(obj);
		entityManager.getTransaction().commit();
	}

	private Stock newStockWithId(String id, Product product, int quantity) {
		Stock s = new Stock(product, quantity);
		s.setId(id);
		return s;
	}

	private OrderItem newItemWithId(String id, Product product, Order order, int quantity) {
		OrderItem item = new OrderItem(product, order, quantity);
		item.setId(id);
		return item;
	}

}
