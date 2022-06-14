package com.github.raffaelliscandiffio.multithreading;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.exception.TransactionException;
import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;

class MultithreadingMysqlIT {

	private static final String DATABASE_NAME = "totem";

	private static final String PRODUCT_NAME = "product_1";
	private static final int STOCK_QUANTITY = 100;
	private static final int PURCHASE_QUANTITY = 20;
	private static final int RETURN_QUANTITY = 10;
	private static final int EMPTY = 0;

	private static final int nThreads = 10;

	private static EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	@BeforeAll
	static void createEntityManagerFactory() {
		System.setProperty("db.port", "3306");
		System.setProperty("db.name", DATABASE_NAME);
		entityManagerFactory = Persistence.createEntityManagerFactory("mysql-test");
	}

	@AfterAll
	static void closeEntityManagerFactory() {
		entityManagerFactory.close();
	}

	@BeforeEach()
	void setup() {
		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();

		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();

	}

	@AfterEach
	void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	@DisplayName("Shopping service should not check stale data when there are multiple entity managers")
	void testMultiManagerBuyStaleData() {
		Product product = new Product(PRODUCT_NAME, 2.0);
		Stock stock = new Stock(product, STOCK_QUANTITY);
		Order order_A = new Order(OrderStatus.OPEN);
		Order order_B = new Order(OrderStatus.OPEN);
		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.persist(stock);
		entityManager.persist(order_A);
		entityManager.persist(order_B);
		entityManager.getTransaction().commit();
		String productId = product.getId();

		// Pre-load the entities in the persistence context of a new entity manager
		// Data will be stored in the second level cache
		EntityManager entityManager_B = entityManagerFactory.createEntityManager();
		entityManager_B.find(Product.class, productId);
		entityManager_B.find(Stock.class, stock.getId());

		ShoppingService shoppingService_A = new ShoppingService(new TransactionManagerMySql(entityManager));
		ShoppingService shoppingService_B = new ShoppingService(new TransactionManagerMySql(entityManager_B));

		// User_A adds the product to his chart. The product is now out of stock
		shoppingService_A.buyProduct(order_A.getId(), productId, STOCK_QUANTITY);

		// Assert that the product is out of stock also for User_B
		String orderId_B = order_B.getId();
		assertThatThrownBy(() -> shoppingService_B.buyProduct(orderId_B, productId, STOCK_QUANTITY))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Not enough quantity. Cannot buy product: " + PRODUCT_NAME);

	}

	@Test
	@DisplayName("Test that the stock quantity is correctly updated when concurrent threads return the same Product")
	void testReturnItemOnTheSameProductConcurrently() {
		Product product = new Product(PRODUCT_NAME, 2.0);
		Stock stock = new Stock(product, EMPTY);
		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.persist(stock);
		entityManager.getTransaction().commit();

		List<EntityManager> managers = new ArrayList<>();
		List<ShoppingService> services = new ArrayList<>();
		List<OrderItem> items = new ArrayList<>();
		for (int i = 0; i < nThreads; i++) {
			EntityManager manager = entityManagerFactory.createEntityManager();
			Order order = new Order(OrderStatus.OPEN);
			OrderItem item = new OrderItem(product, order, PURCHASE_QUANTITY);
			ShoppingService shoppingService = new ShoppingService(new TransactionManagerMySql(manager));
			manager.getTransaction().begin();
			manager.persist(order);
			manager.persist(item);
			manager.getTransaction().commit();
			manager.find(Product.class, product.getId());
			manager.find(Stock.class, stock.getId());
			items.add(item);
			managers.add(manager);
			services.add(shoppingService);
		}

		List<Thread> threads = IntStream.range(0, nThreads).mapToObj(i -> new Thread(() -> {

			try {
				services.get(i).returnItem(items.get(i), RETURN_QUANTITY);
			} catch (Throwable pass) {
			} finally {
				managers.get(i).close();
			}

		})).peek(Thread::start).collect(Collectors.toList());

		await().atMost(20, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(Thread::isAlive));
		threads.forEach(Thread::interrupt);

		Stock refreshStock = entityManager.find(Stock.class, stock.getId());
		entityManager.refresh(refreshStock);
		List<OrderItem> allItems = entityManager.createQuery("SELECT item FROM OrderItem item", OrderItem.class)
				.getResultList();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(stock.getQuantity()).isEqualTo(RETURN_QUANTITY * nThreads);
		allItems.stream()
				.map(item -> softly.assertThat(item.getQuantity()).isEqualTo(PURCHASE_QUANTITY - RETURN_QUANTITY));
		softly.assertAll();

	}

	@Test
	@DisplayName("Test that concurrent threads cannot buy more than is available")
	void testBuyProductOnTheSameProductConcurrently() {
		Product product = new Product("product_1", 2.0);
		Stock stock = new Stock(product, STOCK_QUANTITY);

		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.persist(stock);
		entityManager.getTransaction().commit();

		List<EntityManager> managers = new ArrayList<>();
		List<ShoppingService> services = new ArrayList<>();
		List<Order> orders = new ArrayList<>();
		for (int i = 0; i < nThreads; i++) {

			EntityManager manager = entityManagerFactory.createEntityManager();
			Order order = new Order(OrderStatus.OPEN);
			manager.getTransaction().begin();
			manager.persist(order);
			manager.getTransaction().commit();
			manager.find(Product.class, product.getId());
			manager.find(Stock.class, stock.getId());
			orders.add(order);
			managers.add(manager);
			TransactionManager transactionManager = new TransactionManagerMySql(manager);
			ShoppingService shoppingService = new ShoppingService(transactionManager);
			services.add(shoppingService);
		}

		List<Thread> threads = IntStream.range(0, nThreads).mapToObj(i -> new Thread(() -> {
			try {
				services.get(i).buyProduct(orders.get(i).getId(), product.getId(), PURCHASE_QUANTITY);
			} catch (Throwable pass) {
			} finally {
				managers.get(i).close();
			}
		})).peek(Thread::start).collect(Collectors.toList());
		await().atMost(20, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(Thread::isAlive));
		threads.forEach(Thread::interrupt);

		Stock refreshStock = entityManager.find(Stock.class, stock.getId());
		entityManager.refresh(refreshStock);
		// the return value of SUM is always long
		int purchasedQuantity = Math.toIntExact(
				entityManager.createQuery("SELECT SUM(quantity) FROM OrderItem", Long.class).getSingleResult());

		int nExpectedBuyers = (STOCK_QUANTITY / PURCHASE_QUANTITY);
		int expectedPurchasedQuantity = PURCHASE_QUANTITY * nExpectedBuyers;
		int expectedStockQuantity = STOCK_QUANTITY - expectedPurchasedQuantity;
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(purchasedQuantity).isEqualTo((expectedPurchasedQuantity));
		softly.assertThat(refreshStock.getQuantity()).isEqualTo((expectedStockQuantity));
		softly.assertAll();
	}

}
