package com.github.raffaelliscandiffio.controller;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.github.raffaelliscandiffio.repository.mysql.ProductMySQLRepository;
import com.github.raffaelliscandiffio.repository.mysql.StockMySQLRepository;

@Testcontainers(disabledWithoutDocker = true)
class PurchaseBrokerMySQLIT {

	private ProductRepository productRepository;
	private StockRepository stockRepository;
	private PurchaseBroker broker;

	private static EntityManagerFactory emf;
	private EntityManager entityManager;

	private static final String TOTEM_DB_NAME = "totem";

	@Container
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.28")
			.withDatabaseName(TOTEM_DB_NAME).withUsername("mysql").withPassword("mysql");

	@BeforeAll
	public static void createEntityManagerFactory() {
		System.setProperty("db.port", mysqlContainer.getFirstMappedPort().toString());
		emf = Persistence.createEntityManagerFactory("mysql-test");
	}

	@AfterAll
	public static void closeEntityManagerFactory() {
		emf.close();
	}

	@BeforeEach
	void setup() {
		entityManager = emf.createEntityManager();
		// always starting with empty database
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.getTransaction().commit();
		productRepository = new ProductMySQLRepository(entityManager);
		stockRepository = new StockMySQLRepository(entityManager);
		broker = new PurchaseBroker(productRepository, stockRepository);
	}

	@AfterEach
	void tearDown() {
		if (entityManager.getTransaction().isActive()) {
			entityManager.getTransaction().rollback();
		}
		if (entityManager.isOpen()) {
			entityManager.close();
		}
	}

	@DisplayName("'saveNewProductInStock' should save product and stock in db")
	@Test
	void testsaveNewProductInStock() {
		broker.saveNewProductInStock(1, "pizza", 5.5, 100);
		assertThat(productRepository.findAll()).containsExactly(new Product(1, "pizza", 5.5));
		assertThat(stockRepository.findById(1).getQuantity()).isEqualTo(100);
	}

	@DisplayName("'retrieveProducts' should return products in db")
	@Test
	void testRetrieveProducts() {
		Product product1 = new Product(1, "pasta", 2.3);
		Product product2 = new Product(2, "pizza", 5.5);
		productRepository.save(product1);
		productRepository.save(product2);
		assertThat(broker.retrieveProducts()).containsExactly(product1, product2);
	}

	@DisplayName("'takeAvailable' should return quantity requested when available")
	@Test
	void testTakeAvailableReturnsRequested() {
		stockRepository.save(new Stock(1, 100));
		int quantity = broker.takeAvailable(1, 20);

		assertThat(quantity).isEqualTo(20);
		assertThat(stockRepository.findById(1).getQuantity()).isEqualTo(80);
	}

	@DisplayName("'takeAvailable' should return quantity available when requested is not available")
	@Test
	void testTakeAvailableReturnsAvailable() {
		stockRepository.save(new Stock(1, 50));
		int quantity = broker.takeAvailable(1, 60);

		assertThat(quantity).isEqualTo(50);
		assertThat(stockRepository.findById(1).getQuantity()).isEqualTo(0);
	}

	@DisplayName("'doesProductExist' returns true when the id is in db")
	@Test
	void testDoesProductExistWhenIdIsFound() {
		productRepository.save(new Product(1, "pasta", 2.4));
		assertThat(broker.doesProductExist(1)).isTrue();
	}

	@DisplayName("'doesProductExist' returns false when the id is not in db")
	@Test
	void testDoesProductExistWhenIdIsNotFound() {
		assertThat(broker.doesProductExist(1)).isFalse();
	}

	@Test
	@DisplayName("'returnProduct' increases the stock quantity by the specified amount")
	void testReturnProduct() {
		stockRepository.save(new Stock(1, 90));
		broker.returnProduct(1, 10);
		assertThat(stockRepository.findById(1).getQuantity()).isEqualTo(100);
	}

}