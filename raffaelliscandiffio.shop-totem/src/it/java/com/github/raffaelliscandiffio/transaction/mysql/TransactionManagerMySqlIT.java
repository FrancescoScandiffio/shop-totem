package com.github.raffaelliscandiffio.transaction.mysql;

import java.util.List;

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
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.mysql.OrderItemMySqlRepository;
import com.github.raffaelliscandiffio.repository.mysql.OrderMySqlRepository;
import com.github.raffaelliscandiffio.repository.mysql.ProductMySqlRepository;
import com.github.raffaelliscandiffio.repository.mysql.StockMySqlRepository;


class TransactionManagerMySqlIT {

	private static final String DB_NAME = "totem";
	private static EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private TransactionManagerMySql transactionManager;


	@BeforeAll
	public static void setupClass() {
		System.setProperty("db.port", "3306");
		System.setProperty("db.name", DB_NAME);
		entityManagerFactory = Persistence.createEntityManagerFactory("mysql-test");

	}

	@BeforeEach
	void setup() {
		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();
		transactionManager = new TransactionManagerMySql(entityManager);
	}

	@AfterEach
	void teardDown() {
		entityManager.close();
	}

	@AfterAll
	public static void tearDownClass() {
		entityManagerFactory.close();
	}

	@Test
	@DisplayName("Run code in transaction and initialise mysql repositories")
	void testRunInTransaction() {
		SoftAssertions softly = new SoftAssertions();
		Product product = new Product("product", 1.0);
		Product result = transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, orderItemRepository) -> {
					softly.assertThat(entityManager.getTransaction().isActive()).isTrue();
					softly.assertThat(productRepository).usingRecursiveComparison()
							.isEqualTo(new ProductMySqlRepository(entityManager));
					softly.assertThat(stockRepository).usingRecursiveComparison()
							.isEqualTo(new StockMySqlRepository(entityManager));
					softly.assertThat(orderRepository).usingRecursiveComparison()
							.isEqualTo(new OrderMySqlRepository(entityManager));
					softly.assertThat(orderItemRepository).usingRecursiveComparison()
							.isEqualTo(new OrderItemMySqlRepository(entityManager));
					entityManager.persist(product);
					return product;
				});
		softly.assertThat(result).isEqualTo(product);
		softly.assertThat(entityManager.getTransaction().isActive()).isFalse();
		softly.assertThat(queryProductList()).containsExactly(product);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'runInTransaction' should rollback and throw a new exception when an Exception occurs")
	void runInTransactionWhenExceptionIsThrownShouldRollbackAndThrowNew() {
		SoftAssertions softly = new SoftAssertions();
		Product product = new Product("product", 1.0);
		String exceptionMessage = "Exception message";
		softly.assertThatThrownBy(() -> transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, orderItemRepository) -> {
					entityManager.persist(product);
					throw new RuntimeException(exceptionMessage);
				})).isInstanceOf(TransactionException.class).hasMessage(exceptionMessage);
		softly.assertThat(entityManager.getTransaction().isActive()).isFalse();
		softly.assertThat(queryProductList()).isEmpty();
		softly.assertAll();
	}

	private List<Product> queryProductList() {
		return entityManager.createQuery("SELECT product FROM Product product", Product.class).getResultList();
	}
}
