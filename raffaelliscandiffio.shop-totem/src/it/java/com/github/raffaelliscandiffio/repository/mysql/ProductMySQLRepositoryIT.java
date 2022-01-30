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
	@DisplayName("Test container is running")
	void testContainerIsRunning() {
		assertThat(mysqlContainer.isRunning()).isTrue();
	}

}
