package com.github.raffaelliscandiffio.repository.mysql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

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

@Testcontainers(disabledWithoutDocker = true)
class StockMySqlRepositoryTestcontainersIT {

	private static final int STOCK_QUANTITY = 10;
	private Product product_1;
	private Product product_2;
	private Stock stock_1;
	private Stock stock_2;

	private static EntityManagerFactory managerFactory;
	private EntityManager entityManager;
	private StockMySqlRepository stockRepository;
	private static final String DATABASE_NAME = "totem";

	@Container
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.28")
			.withDatabaseName(DATABASE_NAME).withUsername("mysql").withPassword("mysql");

	@BeforeAll
	public static void createEntityManagerFactory() {
		System.setProperty("db.port", mysqlContainer.getFirstMappedPort().toString());
		managerFactory = Persistence.createEntityManagerFactory("mysql-test");
	}

	@AfterAll
	public static void closeEntityManagerFactory() {
		managerFactory.close();
	}

	@BeforeEach
	void setup() {
		entityManager = managerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();
		stockRepository = new StockMySqlRepository(entityManager);
		product_1 = new Product("product_1", 1.0);
		product_2 = new Product("product_2", 2.0);
		stock_1 = new Stock(product_1, STOCK_QUANTITY);
		stock_2 = new Stock(product_2, STOCK_QUANTITY);
	}

	@AfterEach
	void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	@DisplayName("Save Stock to database with 'save'")
	void testSaveProduct() {
		persistObjectToDatabase(product_1);
		entityManager.getTransaction().begin();
		stockRepository.save(stock_1);
		entityManager.getTransaction().commit();
		String assignedId = stock_1.getId();
		assertThat(readAllStocksFromDatabase()).containsExactly(newStockWithId(assignedId, product_1, STOCK_QUANTITY));
	}

	@Test
	@DisplayName("Find Stock by id with 'findById' when the stock exists")
	void testFindByIdWhenIdIsFound() {
		persistObjectToDatabase(product_1);
		persistObjectToDatabase(product_2);
		persistObjectToDatabase(stock_1);
		persistObjectToDatabase(stock_2);
		String assignedId = stock_1.getId();
		assertThat(stockRepository.findById(assignedId))
				.isEqualTo(newStockWithId(assignedId, product_1, STOCK_QUANTITY));
	}

	@Test
	@DisplayName("Find Stock by id with 'findById' when the stock does not exist should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(stockRepository.findById("1")).isNull();
	}

	@Test
	@DisplayName("Update stock with 'update'")
	void testUpdateStock() {
		persistObjectToDatabase(product_1);
		persistObjectToDatabase(stock_1);
		int modifiedQuantity = stock_1.getQuantity() + 100;
		String assignedId = stock_1.getId();
		stock_1.setQuantity(modifiedQuantity);
		entityManager.getTransaction().begin();
		stockRepository.update(stock_1);
		entityManager.getTransaction().commit();
		assertThat(readAllStocksFromDatabase())
				.containsExactly(newStockWithId(assignedId, product_1, modifiedQuantity));
	}

	private void persistObjectToDatabase(Object object) {
		entityManager.getTransaction().begin();
		entityManager.persist(object);
		entityManager.getTransaction().commit();
	}

	private List<Stock> readAllStocksFromDatabase() {
		return entityManager.createQuery("SELECT s FROM Stock s", Stock.class).getResultList();
	}

	private Stock newStockWithId(String id, Product product, int quantity) {
		Stock s = new Stock(product, quantity);
		s.setId(id);
		return s;
	}
}