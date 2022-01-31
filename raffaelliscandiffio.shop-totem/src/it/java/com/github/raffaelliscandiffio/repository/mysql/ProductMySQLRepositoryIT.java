package com.github.raffaelliscandiffio.repository.mysql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

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

@Testcontainers
class ProductMySQLRepositoryIT {

	private static final String TOTEM_DB_NAME = "totem";

	private static EntityManagerFactory emf;
	private EntityManager entityManager;
	private ProductMySQLRepository productRepository;

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
        entityManager.createQuery("DELETE FROM Product").executeUpdate();
        entityManager.getTransaction().commit();
		productRepository = new ProductMySQLRepository(entityManager);
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
	@DisplayName("'findById' when the id is found")
	void testFindByIdWhenIdIsFound() {
		Product product1 = new Product(1, "pizza", 5.5);
		Product product2 = new Product(2, "pasta", 2.3);
		addTestProductToDatabase(product1); 
		addTestProductToDatabase(product2); 
		assertThat(productRepository.findById(2)).isEqualTo(product2);
	}

	@Test
	@DisplayName("'findById' when the id is not found should throw NoSuchElementException")
	void testFindByIdWhenIdIsNotFoundShouldThrowNoSuchElementException() {
		assertThatThrownBy(() -> productRepository.findById(1)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Product with id 1 not found");
	}
	
	@Test
	@DisplayName("'findAll' when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}
	
	@Test
	@DisplayName("'findAll' when the database is not empty")
	void testFindAllWhenDatabaseIsNotEmpty() {
		Product product1 = new Product(1, "pizza", 5.5);
		Product product2 = new Product(2, "pasta", 2.3);
		addTestProductToDatabase(product1); 
		addTestProductToDatabase(product2); 
		assertThat(productRepository.findAll()).containsExactly(product1, product2);
	}
	
	private void addTestProductToDatabase(Product product) {
		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.getTransaction().commit();
	}

}
