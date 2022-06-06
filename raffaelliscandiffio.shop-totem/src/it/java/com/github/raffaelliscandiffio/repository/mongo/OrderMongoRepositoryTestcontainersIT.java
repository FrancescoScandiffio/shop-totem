package com.github.raffaelliscandiffio.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.api.SoftAssertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class OrderMongoRepositoryTestcontainersIT {

	private static final String DATABASE_NAME = "totem";
	private static final String ORDER_COLLECTION_NAME = "order";

	private MongoClient client;
	private ClientSession session;
	private OrderMongoRepository orderRepository;
	private MongoCollection<Document> orderCollection;

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		MongoDatabase database = client.getDatabase(DATABASE_NAME);
		database.drop();
		orderCollection = database.getCollection(ORDER_COLLECTION_NAME);
		session = client.startSession();
		orderRepository = new OrderMongoRepository(client, session, DATABASE_NAME, ORDER_COLLECTION_NAME);
	}

	@AfterEach
	public void tearDown() {
		session.close();
		client.close();
	}

	@Test
	@DisplayName("Insert Order in database with 'save'")
	void testSaveOrder() {
		Order order = new Order(OrderStatus.OPEN);
		SoftAssertions softly = new SoftAssertions();
		orderRepository.save(order);
		String assignedId = order.getId();
		Order expectedResult = newOrderWithId(assignedId, OrderStatus.OPEN);
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllOrderFromDatabase()).containsExactly(expectedResult);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should be bound to the repository session")
	void testSaveOrderShouldBeBoundToTheRepositorySession() {
		Order order = new Order(OrderStatus.OPEN);
		session.startTransaction();
		orderRepository.save(order);
		assertThat(readAllOrderFromDatabase()).isEmpty();
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve Order by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String idToFind = getNewStringId();
		Order order_1 = newOrderWithId(getNewStringId(), OrderStatus.OPEN);
		Order order_2 = newOrderWithId(idToFind, OrderStatus.OPEN);
		Order expectedResult = newOrderWithId(idToFind, OrderStatus.OPEN);
		saveTestOrderToDatabase(order_1);
		saveTestOrderToDatabase(order_2);
		assertThat(orderRepository.findById(idToFind)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missingId = getNewStringId();
		assertThat(orderRepository.findById(missingId)).isNull();
	}

	@Test
	@DisplayName("Method 'findById' should be bound to the repository session")
	void testFindByIdShouldBeBoundToTheRepositorySession() {
		String idToFind = getNewStringId();
		Order order_1 = newOrderWithId(idToFind, OrderStatus.OPEN);
		Order expectedResult = newOrderWithId(idToFind, OrderStatus.OPEN);
		session.startTransaction();
		saveTestOrderToDatabaseWithSession(session, order_1);
		assertThat(orderRepository.findById(idToFind)).isEqualTo(expectedResult);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Remove Order from the collection by id with 'delete'")
	void testDelete() {
		String idToRemove = getNewStringId();
		Order toRemove = newOrderWithId(idToRemove, OrderStatus.OPEN);
		Order order_1 = newOrderWithId(getNewStringId(), OrderStatus.OPEN);
		saveTestOrderToDatabase(toRemove);
		saveTestOrderToDatabase(order_1);
		orderRepository.delete(idToRemove);
		assertThat(readAllOrderFromDatabase()).containsExactly(order_1);
	}

	@Test // document the behaviour
	@DisplayName("Method 'delete' when the order does not exist should not throw exception")
	void testDeleteWhenOrderDoesNotExistShouldNotThrow() {
		String idToRemove = getNewStringId();
		assertThatCode(() -> orderRepository.delete(idToRemove)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Method 'delete' should be bound to the repository session")
	void testDeleteShouldBeBoundToTheRepositorySession() {
		String idToRemove = getNewStringId();
		Order toRemove = newOrderWithId(idToRemove, OrderStatus.OPEN);
		Order order_1 = newOrderWithId(getNewStringId(), OrderStatus.OPEN);
		session.startTransaction();
		saveTestOrderToDatabaseWithSession(session, toRemove);
		saveTestOrderToDatabaseWithSession(session, order_1);
		orderRepository.delete(idToRemove);
		session.commitTransaction();
		assertThat(readAllOrderFromDatabase()).containsExactly(order_1);
	}

	@Test
	@DisplayName("Update Order with 'update'")
	void updateOrder() {
		String idToUpdate = getNewStringId();
		Order toUpdate = newOrderWithId(idToUpdate, OrderStatus.OPEN);
		Order order_1 = newOrderWithId(getNewStringId(), OrderStatus.OPEN);
		saveTestOrderToDatabase(toUpdate);
		saveTestOrderToDatabase(order_1);
		toUpdate.setStatus(OrderStatus.CLOSED);
		Order expectedResult = newOrderWithId(idToUpdate, OrderStatus.CLOSED);
		orderRepository.update(toUpdate);
		assertThat(readAllOrderFromDatabase()).containsExactlyInAnyOrder(order_1, expectedResult);
	}

	@Test
	@DisplayName("Update Order when order does not exist should throw")
	void updateOrderWhenItDoesNotExistShouldThrow() {
		String missingId = getNewStringId();
		Order missingOrder = newOrderWithId(missingId, OrderStatus.OPEN);
		assertThatThrownBy(() -> orderRepository.update(missingOrder)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Order with id " + missingId + " not found.");
	}

	@Test
	@DisplayName("Method 'update' should be bound to the repository session")
	void updateOrderShouldBeBoundToTheRepositorySession() {
		String idToUpdate = getNewStringId();
		Order toUpdate = newOrderWithId(idToUpdate, OrderStatus.OPEN);
		Order notUpdated = newOrderWithId(idToUpdate, OrderStatus.OPEN);
		saveTestOrderToDatabase(toUpdate);
		toUpdate.setStatus(OrderStatus.CLOSED);
		session.startTransaction();
		orderRepository.update(toUpdate);
		assertThat(readAllOrderFromDatabase()).containsExactlyInAnyOrder(notUpdated);
		session.commitTransaction();
	}

	// Private utility methods

	private String getNewStringId() {
		return new ObjectId().toString();
	}

	private Order newOrderWithId(String id, OrderStatus status) {
		Order order = new Order(status);
		order.setId(id);
		return order;
	}

	private Document fromOrderToDocument(Order orderWithId) {
		return new Document().append("_id", new ObjectId(orderWithId.getId())).append("status",
				orderWithId.getStatus().toString());
	}

	private void saveTestOrderToDatabase(Order orderWithId) {
		orderCollection.insertOne(fromOrderToDocument(orderWithId));
	}

	private void saveTestOrderToDatabaseWithSession(ClientSession session, Order orderWithId) {
		orderCollection.insertOne(session, fromOrderToDocument(orderWithId));
	}

	private List<Order> readAllOrderFromDatabase() {
		return StreamSupport.stream(orderCollection.find().spliterator(), false).map(orderDocument -> {
			Order order = new Order(OrderStatus.valueOf(orderDocument.getString("status")));
			order.setId(orderDocument.get("_id").toString());
			return order;
		}).collect(Collectors.toList());
	}

}
