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

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderStatus;


class OrderMySqlRepositoryIT {

	private static final String DATABASE_NAME = "totem";
	private static EntityManagerFactory managerFactory;
	private EntityManager entityManager;
	private OrderMySqlRepository orderRepository;


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

	@BeforeEach
	void setup() {
		entityManager = managerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.getTransaction().commit();
		orderRepository = new OrderMySqlRepository(entityManager);

	}

	@AfterEach
	void tearDown() {
		if (entityManager.isOpen())
			entityManager.close();
	}

	@Test
	@DisplayName("Save Order to database with 'save'")
	void testSaveOrder() {
		Order order = new Order(OrderStatus.OPEN);
		entityManager.getTransaction().begin();
		orderRepository.save(order);
		entityManager.getTransaction().commit();
		Order expectedResult = newOrderWithId(order.getId(), OrderStatus.OPEN);
		assertThat(readAllOrdersFromDatabase()).containsExactly(expectedResult);
	}

	@Test
	@DisplayName("Find Order by id with 'findById' when the order exists")
	void testFindByIdWhenIdIsFound() {
		Order order_1 = new Order(OrderStatus.OPEN);
		Order order_2 = new Order(OrderStatus.OPEN);
		persistOrderToDatabase(order_1);
		persistOrderToDatabase(order_2);
		String idToFind = order_1.getId();
		Order expectedResult = newOrderWithId(idToFind, OrderStatus.OPEN);
		assertThat(orderRepository.findById(idToFind)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Find Order by id with 'findById' when the order does not exist should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(orderRepository.findById("fake_id")).isNull();
	}

	@Test
	@DisplayName("Update Order with 'update'")
	void testUpdateOrder() {
		Order toModify = new Order(OrderStatus.OPEN);
		Order order_2 = new Order(OrderStatus.OPEN);
		persistOrderToDatabase(toModify);
		persistOrderToDatabase(order_2);
		toModify.setStatus(OrderStatus.CLOSED);
		Order expectedResult = newOrderWithId(toModify.getId(), OrderStatus.CLOSED);
		entityManager.getTransaction().begin();
		orderRepository.update(toModify);
		entityManager.getTransaction().commit();
		assertThat(readAllOrdersFromDatabase()).containsExactlyInAnyOrder(expectedResult, order_2);
	}

	@Test
	@DisplayName("Delete Order by id with 'delete'")
	void testDeleteOrder() {
		Order toDelete = new Order(OrderStatus.OPEN);
		Order order_2 = new Order(OrderStatus.OPEN);
		persistOrderToDatabase(toDelete);
		persistOrderToDatabase(order_2);
		String idToDelete = toDelete.getId();
		entityManager.getTransaction().begin();
		orderRepository.delete(idToDelete);
		entityManager.getTransaction().commit();
		assertThat(readAllOrdersFromDatabase()).containsExactly(order_2);
	}

	private List<Order> readAllOrdersFromDatabase() {
		return entityManager.createQuery("SELECT o FROM Order o", Order.class).getResultList();
	}

	private void persistOrderToDatabase(Order order) {
		entityManager.getTransaction().begin();
		entityManager.persist(order);
		entityManager.getTransaction().commit();
	}

	private Order newOrderWithId(String id, OrderStatus status) {
		Order order = new Order(status);
		order.setId(id);
		return order;
	}

}

