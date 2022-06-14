package com.github.raffaelliscandiffio.multithreading;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionException;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;

class MultithreadingMysqlIT {

	private static final String DATABASE_NAME = "totem";

	private static final String PRODUCT_NAME = "product_1";
	private static final int STOCK_QUANTITY = 10;

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
	@DisplayName("Shopping service should not check stale data when")
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

}
