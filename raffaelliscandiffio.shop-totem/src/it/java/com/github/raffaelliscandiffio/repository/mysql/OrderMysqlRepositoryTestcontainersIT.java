package com.github.raffaelliscandiffio.repository.mysql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
class OrderMysqlRepositoryTestcontainersIT {

	private static final String DATABASE_NAME = "totem";
	private static EntityManagerFactory managerFactory;
	private EntityManager entityManager;
	private OrderMysqlRepository orderRepository;
	private Product product_1;
	private Product product_2;
	private OrderItem item_1;
	private OrderItem item_2;

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
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();
		orderRepository = new OrderMysqlRepository(entityManager);
		product_1 = new Product("product_1", 1.0);
		product_2 = new Product("product_2", 2.0);
		item_1 = new OrderItem(product_1, 5);
		item_2 = new OrderItem(product_2, 10);
		persistObjectToDatabase(product_1);
		persistObjectToDatabase(product_2);
	}

	@AfterEach
	void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	@DisplayName("Save Order to database with 'save'")
	void testSaveOrder() {
		Order order_1 = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN);
		entityManager.getTransaction().begin();
		orderRepository.save(order_1);
		entityManager.getTransaction().commit();
		assertThat(readAllOrdersFromDatabase()).containsExactly(
				newOrderWithId(order_1.getId(), new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN));
	}

	@Test
	@DisplayName("Find Order by id with 'findById' when the order exists")
	void testFindByIdWhenIdIsFound() {
		Order order_1 = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN);
		Order order_2 = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.OPEN);
		persistObjectToDatabase(order_1);
		persistObjectToDatabase(order_2);
		String assignedId = order_1.getId();
		assertThat(orderRepository.findById(assignedId)).isEqualTo(
				newOrderWithId(assignedId, new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN));
	}

	@Test
	@DisplayName("Find Order by id with 'findById' when the order does not exist should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(orderRepository.findById("1")).isNull();
	}

	@Test
	@DisplayName("Update Order with 'update'")
	void testUpdateOrder() {
		Set<OrderItem> items = new LinkedHashSet<OrderItem>(Arrays.asList(item_1));
		Order order_1 = new Order(items, OrderStatus.OPEN);
		Order order_2 = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.OPEN);
		persistObjectToDatabase(order_1);
		persistObjectToDatabase(order_2);
		items.add(item_2);
		entityManager.getTransaction().begin();
		orderRepository.update(order_1);
		entityManager.getTransaction().commit();
		assertThat(readAllOrdersFromDatabase()).containsExactlyInAnyOrder(
				newOrderWithId(order_1.getId(), new LinkedHashSet<OrderItem>(Arrays.asList(item_1, item_2)),
						OrderStatus.OPEN),
				newOrderWithId(order_2.getId(), new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.OPEN));
	}

	@Test
	@DisplayName("Delete Order with 'delete'")
	void testDeleteOrder() {
		Order order_1 = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN);
		Order order_2 = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.OPEN);
		persistObjectToDatabase(order_1);
		persistObjectToDatabase(order_2);
		entityManager.getTransaction().begin();
		orderRepository.delete(order_1);
		entityManager.getTransaction().commit();
		assertThat(readAllOrdersFromDatabase()).containsExactly(order_2);
	}

	private List<Order> readAllOrdersFromDatabase() {
		return entityManager.createQuery("SELECT o FROM Order o", Order.class).getResultList();
	}

	private void persistObjectToDatabase(Object object) {
		entityManager.getTransaction().begin();
		entityManager.persist(object);
		entityManager.getTransaction().commit();
	}

	private Order newOrderWithId(String id, Set<OrderItem> items, OrderStatus status) {
		Order order = new Order(items, status);
		order.setId(id);
		return order;
	}

}
