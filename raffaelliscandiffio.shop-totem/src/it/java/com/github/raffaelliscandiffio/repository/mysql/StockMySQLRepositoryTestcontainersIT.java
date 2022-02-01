package com.github.raffaelliscandiffio.repository.mysql;
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

import com.github.raffaelliscandiffio.model.Stock;


@Testcontainers(disabledWithoutDocker = true)
class StockMySQLRepositoryTestcontainersIT {
	private static final String TOTEM_DB_NAME = "totem";

	private static EntityManagerFactory emf;
	private EntityManager entityManager;
	private StockMySQLRepository stockRepository;

	@Container
	public static final MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0.28")
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
        entityManager.createQuery("DELETE FROM Stock").executeUpdate();
        entityManager.getTransaction().commit();
		stockRepository = new StockMySQLRepository(entityManager);
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
	
	@Test
	@DisplayName("'findById' when the id is not found should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(stockRepository.findById(1)).isNull();
	}
	
	@Test
	@DisplayName("'findById' when the id is found")
	void testFindByIdWhenIdIsFound() {
		Stock stock1 = new Stock(1, 100);
		Stock stock2 = new Stock(2, 50);
		addTestStockToDatabase(stock1); 
		addTestStockToDatabase(stock2); 
		assertThat(stockRepository.findById(2)).isEqualTo(stock2);
	}
	
	private void addTestStockToDatabase(Stock stock) {
		entityManager.getTransaction().begin();
		entityManager.persist(stock);
		entityManager.getTransaction().commit();
	}
	

}
