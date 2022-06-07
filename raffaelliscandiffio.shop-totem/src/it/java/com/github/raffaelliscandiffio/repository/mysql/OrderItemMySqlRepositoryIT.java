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

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;

@Testcontainers(disabledWithoutDocker = true)
class OrderItemMySqlRepositoryIT {

	private static final String DATABASE_NAME = "totem";
	private static final int QUANTITY_1 = 10;
	private static final int QUANTITY_2 = 20;
	private static EntityManagerFactory managerFactory;

	private EntityManager entityManager;
	private OrderItemMySqlRepository orderItemRepository;
	private Product product_1;
	private Product product_2;
	private Order order_1;
	private Order order_2;

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
		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();
		orderItemRepository = new OrderItemMySqlRepository(entityManager);
		product_1 = new Product("product_1", 1.0);
		product_2 = new Product("product_2", 2.0);
		order_1 = new Order(OrderStatus.OPEN);
		order_2 = new Order(OrderStatus.CLOSED);
		persistObjectToDatabase(product_1);
		persistObjectToDatabase(product_2);
		persistObjectToDatabase(order_1);
		persistObjectToDatabase(order_2);
	}

	@AfterEach
	void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	@DisplayName("Save OrderItem to database with 'save'")
	void testSaveOrderItem() {
		OrderItem item = new OrderItem(product_1, order_1, QUANTITY_1);
		entityManager.getTransaction().begin();
		orderItemRepository.save(item);
		entityManager.getTransaction().commit();
		String assignedId = item.getId();
		OrderItem expectedResult = newOrderItemWithId(assignedId, product_1, order_1, QUANTITY_1);
		assertThat(readAllOrderItemsFromDatabase()).containsExactly(expectedResult);
	}

	@Test
	@DisplayName("Find OrderItem by id with 'findById' when the item exists")
	void testFindByIdWhenIdIsFound() {
		OrderItem item = new OrderItem(product_1, order_1, QUANTITY_1);
		OrderItem toFind = new OrderItem(product_2, order_2, QUANTITY_1);
		persistObjectToDatabase(item);
		persistObjectToDatabase(toFind);
		String assignedId = toFind.getId();
		OrderItem expectedResult = newOrderItemWithId(assignedId, product_2, order_2, QUANTITY_1);
		assertThat(orderItemRepository.findById(assignedId)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Find OrderItem by id with 'findById' when the orderItem does not exist should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(orderItemRepository.findById("1")).isNull();
	}

	@Test
	@DisplayName("Update orderItem with 'update'")
	void testUpdateOrderItem() {
		OrderItem item = new OrderItem(product_1, order_1, QUANTITY_1);
		OrderItem toUpdate = new OrderItem(product_2, order_2, QUANTITY_1);
		persistObjectToDatabase(item);
		persistObjectToDatabase(toUpdate);
		String assignedId = toUpdate.getId();
		toUpdate.setQuantity(QUANTITY_2);
		entityManager.getTransaction().begin();
		orderItemRepository.update(toUpdate);
		entityManager.getTransaction().commit();
		OrderItem expectedResult = newOrderItemWithId(assignedId, product_2, order_2, QUANTITY_2);
		assertThat(readAllOrderItemsFromDatabase()).containsExactlyInAnyOrder(item, expectedResult);
	}

	@Test
	@DisplayName("Delete OrderItem by id with 'delete'")
	void testDeleteOrderItem() {
		OrderItem item = new OrderItem(product_1, order_1, QUANTITY_1);
		OrderItem toDelete = new OrderItem(product_2, order_2, QUANTITY_1);
		persistObjectToDatabase(item);
		persistObjectToDatabase(toDelete);
		String idToDelete = toDelete.getId();
		entityManager.getTransaction().begin();
		orderItemRepository.delete(idToDelete);
		entityManager.getTransaction().commit();
		assertThat(readAllOrderItemsFromDatabase()).containsExactly(item);
	}

	private void persistObjectToDatabase(Object object) {
		entityManager.getTransaction().begin();
		entityManager.persist(object);
		entityManager.getTransaction().commit();
	}

	private List<OrderItem> readAllOrderItemsFromDatabase() {
		return entityManager.createQuery("SELECT item FROM OrderItem item", OrderItem.class).getResultList();
	}

	private OrderItem newOrderItemWithId(String id, Product product, Order order, int quantity) {
		OrderItem item = new OrderItem(product, order, quantity);
		item.setId(id);
		return item;
	}
}