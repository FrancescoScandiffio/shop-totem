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

@Testcontainers(disabledWithoutDocker = true)
class ProductMysqlRepositoryTestcontainersIT {

	private static final String DATABASE_NAME = "totem";
	private static EntityManagerFactory managerFactory;
	private EntityManager entityManager;
	private ProductMysqlRepository productRepository;

	private Product product_1;
	private Product product_2;

	@Container
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.28")
			.withDatabaseName(DATABASE_NAME).withUsername("mysql").withPassword("mysql");

	@BeforeAll
	public static void createEntityManagerFactory() {
		System.setProperty("db.port", mysqlContainer.getFirstMappedPort().toString());
		System.setProperty("db.name", DATABASE_NAME);
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
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();
		productRepository = new ProductMysqlRepository(entityManager);

		product_1 = new Product("pizza", 5.5);
		product_2 = new Product("pasta", 0.8);
	}

	@AfterEach
	void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();

	}

	@Test
	@DisplayName("Save Product to database with 'save'")
	void testSaveProduct() {
		entityManager.getTransaction().begin();
		productRepository.save(product_1);
		entityManager.getTransaction().commit();
		assertThat(readAllProductsFromDatabase()).containsExactly(product_1);
	}

	@Test
	@DisplayName("Find Product by id with 'findById' when the product exists")
	void testFindByIdWhenIdIsFound() {
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
		assertThat(productRepository.findById(product_2.getId())).isEqualTo(product_2);
	}

	@Test
	@DisplayName("Find Product by id with 'findById' when the product does not exist should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(productRepository.findById("1")).isNull();
	}

	@Test
	@DisplayName("Retrieve the products with 'findAll' when the database is not empty")
	void testFindAllWhenDatabaseIsNotEmpty() {
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
		assertThat(productRepository.findAll()).containsExactlyInAnyOrder(product_1, product_2);
	}

	@Test
	@DisplayName("Method 'findAll' should return an empty container when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}

	private void addTestProductToDatabase(Product product) {
		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.getTransaction().commit();
	}

	private List<Product> readAllProductsFromDatabase() {
		return entityManager.createQuery("SELECT p FROM Product p", Product.class).getResultList();
	}

}
