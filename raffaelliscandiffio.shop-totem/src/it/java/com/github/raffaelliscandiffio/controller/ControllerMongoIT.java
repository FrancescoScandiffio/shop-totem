package com.github.raffaelliscandiffio.controller;

import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.mongo.TransactionManagerMongo;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

class ControllerMongoIT {

	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDERITEM_COLLECTION_NAME = "orderItem";

	private static final String PRODUCT_NAME_1 = "product_1";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final int LOW_QUANTITY = 2;
	private static final int MID_QUANTITY = 4;
	private static final int GREAT_QUANTITY = 10;
	private static final double PRICE = 2.0;
	private static final OrderStatus ORDER_OPEN = OrderStatus.OPEN;
	private static final OrderStatus ORDER_CLOSED = OrderStatus.CLOSED;

	private MongoDatabase database;
	private MongoClient client;
	private ClientSession session;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;
	private MongoCollection<Document> orderCollection;
	private MongoCollection<Document> orderItemCollection;

	private TransactionManagerMongo transactionManager;
	private ShoppingService serviceLayer;
	private TotemSwingView view;
	private TotemController controller;

	@BeforeEach()
	void setup() {
		String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
		client = MongoClients.create(uri);

		database = client.getDatabase(DATABASE_NAME);
		database.drop();
		database.createCollection(PRODUCT_COLLECTION_NAME);
		database.createCollection(STOCK_COLLECTION_NAME);
		database.createCollection(ORDER_COLLECTION_NAME);
		database.createCollection(ORDERITEM_COLLECTION_NAME);

		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		stockCollection = database.getCollection(STOCK_COLLECTION_NAME);
		orderCollection = database.getCollection(ORDER_COLLECTION_NAME);
		orderItemCollection = database.getCollection(ORDERITEM_COLLECTION_NAME);

		session = client.startSession();
		transactionManager = new TransactionManagerMongo(client, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
				STOCK_COLLECTION_NAME, ORDER_COLLECTION_NAME, ORDERITEM_COLLECTION_NAME);
		serviceLayer = new ShoppingService(transactionManager);
		view = mock(TotemSwingView.class);
		controller = new TotemController(serviceLayer, view);
	}

	@AfterEach
	void tearDown() {
		session.close();
		client.close();
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

		controller.buyProduct(orderToModify, productToBuy, MID_QUANTITY);

		List<OrderItem> items = getAllItems();
		assertThat(items).singleElement().usingRecursiveComparison().ignoringFields("id")
				.isEqualTo(new OrderItem(product, order, MID_QUANTITY));
		assertThat(getAllStocks()).containsExactly(modifiedStock);
		verify(view).itemAdded(items.get(0));
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

		controller.buyProduct(orderToModify, productToBuy, MID_QUANTITY);

		assertThat(getAllItems()).containsExactly(modifiedItem);
		assertThat(getAllStocks()).containsExactly(modifiedStock);
		verify(view).itemAdded(modifiedItem);
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
		String fakeId = new ObjectId().toString();
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
		String fakeId = new ObjectId().toString();
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
		String fakeId = new ObjectId().toString();
		order.setId(fakeId);
		persistObjects(asList(product, stock));
		String orderToCheckout = order.getId();
		Order confirmedOrder = new Order(ORDER_CLOSED);
		confirmedOrder.setId(orderToCheckout);

		controller.checkout(orderToCheckout);

		verify(view).showCartErrorMessage("Order not found: " + fakeId);

	}
	// Private utility methods

	private static final String _FIELD_ID = "_id";
	private static final String _FIELD_NAME = "name";
	private static final String _FIELD_PRICE = "price";
	private static final String _FIELD_PRODUCT = "product";
	private static final String _FIELD_QUANTITY = "quantity";
	private static final String _FIELD_ORDER = "order";
	private static final String _FIELD_STATUS = "status";

	private void persistObjects(List<Object> objects) {
		session.startTransaction();

		for (Object obj : objects) {
			switch (obj.getClass().getSimpleName()) {
			case "Product":
				persistProduct((Product) obj);
				break;
			case "Stock":
				persistStock((Stock) obj);
				break;
			case "Order":
				persistOrder((Order) obj);
				break;
			case "OrderItem":
				persistItem((OrderItem) obj);
				break;
			}
		}
		session.commitTransaction();

	}

	private void persistProduct(Product product) {
		Document productDocument = new Document().append(_FIELD_NAME, product.getName()).append(_FIELD_PRICE,
				product.getPrice());
		productCollection.insertOne(session, productDocument);
		product.setId(productDocument.get(_FIELD_ID).toString());
	}

	private void persistStock(Stock stock) {
		Document doc = new Document().append(_FIELD_PRODUCT, stock.getProduct().getId()).append(_FIELD_QUANTITY,
				stock.getQuantity());
		stockCollection.insertOne(session, doc);
		stock.setId(doc.get(_FIELD_ID).toString());
	}

	private void persistItem(OrderItem item) {
		Document doc = new Document().append(_FIELD_PRODUCT, item.getProduct().getId())
				.append(_FIELD_ORDER, item.getOrder().getId()).append(_FIELD_QUANTITY, item.getQuantity());
		orderItemCollection.insertOne(session, doc);
		item.setId(doc.get(_FIELD_ID).toString());
	}

	private void persistOrder(Order order) {
		Document orderDocument = new Document().append(_FIELD_STATUS, order.getStatus().toString());
		orderCollection.insertOne(session, orderDocument);
		order.setId(orderDocument.get(_FIELD_ID).toString());
	}

	// Product

	private List<Product> getAllProducts() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(productDocument -> {
			return fromDocumentToProduct(productDocument);
		}).collect(Collectors.toList());
	}

	private Product fromDocumentToProduct(Document d) {
		Product p = new Product(d.getString(_FIELD_NAME), d.getDouble(_FIELD_PRICE));
		p.setId(d.get(_FIELD_ID).toString());
		return p;
	}

	// Stock

	private List<Stock> getAllStocks() {
		return StreamSupport.stream(stockCollection.find().spliterator(), false).map(this::fromDocumentToStock)
				.collect(Collectors.toList());
	}

	private Stock fromDocumentToStock(Document stockDocument) {
		String productId = stockDocument.getString(_FIELD_PRODUCT);
		Document productDocument = productCollection.find(session, eq(_FIELD_ID, new ObjectId(productId))).first();
		Product product = fromDocumentToProduct(productDocument);
		Stock stock = new Stock(product, stockDocument.getInteger(_FIELD_QUANTITY));
		stock.setId(stockDocument.get(_FIELD_ID).toString());
		return stock;
	}

	// Order

	private List<Order> getAllOrders() {
		return StreamSupport.stream(orderCollection.find().spliterator(), false).map(orderDocument -> {
			Order order = new Order(OrderStatus.valueOf(orderDocument.getString(_FIELD_STATUS)));
			order.setId(orderDocument.get(_FIELD_ID).toString());
			return order;
		}).collect(Collectors.toList());
	}

	// Item

	private List<OrderItem> getAllItems() {
		return StreamSupport.stream(orderItemCollection.find().spliterator(), false).map(this::fromDocumentToItem

		).collect(Collectors.toList());
	}

	private List<OrderItem> getAllItemsByOrderId(String orderId) {
		return StreamSupport.stream(orderItemCollection.find(session, eq(_FIELD_ORDER, orderId)).spliterator(), false)
				.map(this::fromDocumentToItem).collect(Collectors.toList());
	}

	private OrderItem fromDocumentToItem(Document itemDocument) {
		String productId = itemDocument.getString(_FIELD_PRODUCT);
		String orderId = itemDocument.getString(_FIELD_ORDER);
		Document productDocument = productCollection.find(eq(_FIELD_ID, new ObjectId(productId))).first();
		Product product = new Product(productDocument.getString(_FIELD_NAME), productDocument.getDouble(_FIELD_PRICE));
		product.setId(productId);
		Document orderDocument = orderCollection.find(eq(_FIELD_ID, new ObjectId(orderId))).first();
		Order order = new Order(OrderStatus.valueOf(orderDocument.getString(_FIELD_STATUS)));
		order.setId(orderId);
		OrderItem orderItem = new OrderItem(product, order, itemDocument.getInteger(_FIELD_QUANTITY));
		orderItem.setId(itemDocument.get(_FIELD_ID).toString());
		return orderItem;
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
