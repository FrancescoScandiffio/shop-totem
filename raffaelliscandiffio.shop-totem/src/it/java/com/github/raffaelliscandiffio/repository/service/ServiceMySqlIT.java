package com.github.raffaelliscandiffio.repository.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionException;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;

class ServiceMySqlIT {

	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_NAME_1 = "product_1";

	private static final int POSITIVE_QUANTITY = 5;
	private static final int NEGATIVE_QUANTITY = -3;

	private static final double POSITIVE_PRICE = 2.0;

	private static EntityManagerFactory managerFactory;
	private EntityManager entityManager;

	private TransactionManagerMySql transactionManager;
	private ShoppingService serviceLayer;

	@BeforeAll
	public static void createEntityManagerFactory() {
		System.setProperty("db.port", "3306");
		System.setProperty("db.name", DATABASE_NAME);
		managerFactory = Persistence.createEntityManagerFactory("mysql-test");
	}

	@AfterAll
	public static void closeEntityManagerFactory() {
		managerFactory.close();
	}

	@BeforeEach()
	public void setup() {
		entityManager = managerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();

		transactionManager = new TransactionManagerMySql(entityManager);
		serviceLayer = new ShoppingService(transactionManager);
	}

	@AfterEach
	public void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	void testSaveProductAndStockIT() {
		Product expectedProduct = new Product(PRODUCT_NAME_1, POSITIVE_PRICE);

		serviceLayer.saveProductAndStock(PRODUCT_NAME_1, POSITIVE_PRICE, POSITIVE_QUANTITY);

		List<Product> products = getAllProducts();

		// assert only one and equal to expected, ignoring the null id

		assertThat(products).singleElement().usingRecursiveComparison().ignoringExpectedNullFields()
				.isEqualTo(expectedProduct);
		Product actualProduct = products.get(0);
		Stock expectedStock = new Stock(actualProduct, POSITIVE_QUANTITY);
		assertThat(getAllStocks()).singleElement().usingRecursiveComparison().ignoringExpectedNullFields()
				.isEqualTo(expectedStock);

	}

	@Test
	void testSaveProductAndStockWhenExceptionIsThrownIT() {
		assertThatThrownBy(() -> serviceLayer.saveProductAndStock(PRODUCT_NAME_1, POSITIVE_PRICE, NEGATIVE_QUANTITY))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Quantity must be positive. Received: " + NEGATIVE_QUANTITY);
		assertThat(getAllProducts()).isEmpty();
		assertThat(getAllStocks()).isEmpty();
	}

	private List<Product> getAllProducts() {
		return entityManager.createQuery("SELECT p FROM Product p", Product.class).getResultList();
	}

	private List<Stock> getAllStocks() {
		return entityManager.createQuery("SELECT s FROM Stock s", Stock.class).getResultList();
	}

}
