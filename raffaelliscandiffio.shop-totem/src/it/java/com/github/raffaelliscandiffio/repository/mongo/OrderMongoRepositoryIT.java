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
class OrderMongoRepositoryIT {

	private static final OrderStatus CLOSED = OrderStatus.CLOSED;
	private static final OrderStatus OPEN = OrderStatus.OPEN;
	private static final String DATABASE_NAME = "totem";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDER_ITEM_COLLECTION_NAME = "item";

	private MongoClient client;
	private ClientSession session;
	private OrderMongoRepository orderRepository;
	private MongoCollection<Document> orderCollection;
	private MongoCollection<Document> itemCollection;

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		MongoDatabase database = client.getDatabase(DATABASE_NAME);
		database.drop();
		orderCollection = database.getCollection(ORDER_COLLECTION_NAME);
		itemCollection = database.getCollection(ORDER_ITEM_COLLECTION_NAME);

		session = client.startSession();
		orderRepository = new OrderMongoRepository(client, session, DATABASE_NAME, ORDER_COLLECTION_NAME,
				ORDER_ITEM_COLLECTION_NAME);
	}

	@AfterEach
	public void tearDown() {
		session.close();
		client.close();
	}

	@Test
	@DisplayName("Insert Order in database with 'save'")
	void testSaveOrder() {
		Order order = new Order(OPEN);
		SoftAssertions softly = new SoftAssertions();
		orderRepository.save(order);
		String assignedId = order.getId();
		Order expectedResult = newOrderWithId(assignedId, OPEN);
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllOrderFromDatabase()).containsExactly(expectedResult);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should be bound to the repository session")
	void testSaveOrderShouldBeBoundToTheRepositorySession() {
		Order order = new Order(OPEN);
		session.startTransaction();
		orderRepository.save(order);
		assertThat(readAllOrderFromDatabase()).isEmpty();
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve Order by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String idToFind = getNewStringId();
		Order order_1 = newOrderWithId(getNewStringId(), OPEN);
		Order order_2 = newOrderWithId(idToFind, OPEN);
		Order expectedResult = newOrderWithId(idToFind, OPEN);
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
		Order order_1 = newOrderWithId(idToFind, OPEN);
		Order expectedResult = newOrderWithId(idToFind, OPEN);
		session.startTransaction();
		saveTestOrderToDatabaseWithSession(session, order_1);
		assertThat(orderRepository.findById(idToFind)).isEqualTo(expectedResult);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Remove Order from the collection by id with 'delete'")
	void testDelete() {
		String idToRemove = getNewStringId();
		Order toRemove = newOrderWithId(idToRemove, OPEN);
		Order order_1 = newOrderWithId(getNewStringId(), OPEN);
		saveTestOrderToDatabase(toRemove);
		saveTestOrderToDatabase(order_1);
		orderRepository.delete(idToRemove);
		assertThat(readAllOrderFromDatabase()).containsExactly(order_1);
	}

	@Test // document the default behaviour
	@DisplayName("Method 'delete' when the order does not exist should not throw exception")
	void testDeleteWhenOrderDoesNotExistShouldNotThrow() {
		String idToRemove = getNewStringId();
		assertThatCode(() -> orderRepository.delete(idToRemove)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Method 'delete' should be bound to the repository session")
	void testDeleteShouldBeBoundToTheRepositorySession() {
		String idToRemove = getNewStringId();
		Order toRemove = newOrderWithId(idToRemove, OPEN);
		Order order_1 = newOrderWithId(getNewStringId(), OPEN);
		session.startTransaction();
		saveTestOrderToDatabaseWithSession(session, toRemove);
		saveTestOrderToDatabaseWithSession(session, order_1);
		orderRepository.delete(idToRemove);
		session.commitTransaction();
		assertThat(readAllOrderFromDatabase()).containsExactly(order_1);
	}

	@Test
	@DisplayName("Method 'delete' should throw exception when there is an OrderItem with a reference to this order")
	void testDeleteWhenAnOrderItemHasAReferenceToTheOrderShouldThrowException() {
		SoftAssertions softly = new SoftAssertions();
		String idItemReference = getNewStringId();
		String idCannotDelete = getNewStringId();
		Order cannotDelete = newOrderWithId(idCannotDelete, OPEN);
		saveTestOrderToDatabase(cannotDelete);
		// No need to store an OrderItem: fields '_id' and 'order_id' are enough
		itemCollection
				.insertOne(new Document().append("_id", new ObjectId(idItemReference)).append("order", idCannotDelete));
		assertThatThrownBy(() -> orderRepository.delete(idCannotDelete)).isInstanceOf(IllegalStateException.class)
				.hasMessage("Reference error: cannot delete Order with id " + idCannotDelete
						+ " because OrderItem with id " + idItemReference + " has a reference to it.");
		softly.assertThat(readAllOrderFromDatabase()).containsExactly(cannotDelete);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'delete' reference check should be bound to the repository session")
	void testDeleteReferenceCheckShouldBeBoundToTheRepositorySession() {
		SoftAssertions softly = new SoftAssertions();
		String idItemReference = getNewStringId();
		String idCannotDelete = getNewStringId();
		Order cannotDelete = newOrderWithId(idCannotDelete, OPEN);
		saveTestOrderToDatabase(cannotDelete);
		session.startTransaction();
		// No need to store an OrderItem: fields '_id' and 'order_id' are enough
		itemCollection.insertOne(session,
				new Document().append("_id", new ObjectId(idItemReference)).append("order", idCannotDelete));
		softly.assertThatThrownBy(() -> orderRepository.delete(idCannotDelete))
				.isInstanceOf(IllegalStateException.class).hasMessage("Reference error: cannot delete Order with id "
						+ idCannotDelete + " because OrderItem with id " + idItemReference + " has a reference to it.");
		session.commitTransaction();
		softly.assertThat(readAllOrderFromDatabase()).containsExactly(cannotDelete);
		softly.assertAll();

	}

	@Test
	@DisplayName("Update Order with 'update'")
	void testUpdateOrder() {
		String idToUpdate = getNewStringId();
		Order toUpdate = newOrderWithId(idToUpdate, OPEN);
		Order order_1 = newOrderWithId(getNewStringId(), OPEN);
		saveTestOrderToDatabase(toUpdate);
		saveTestOrderToDatabase(order_1);
		toUpdate.setStatus(CLOSED);
		Order expectedResult = newOrderWithId(idToUpdate, CLOSED);
		orderRepository.update(toUpdate);
		assertThat(readAllOrderFromDatabase()).containsExactlyInAnyOrder(order_1, expectedResult);
	}

	@Test
	@DisplayName("Update Order when order does not exist should throw")
	void testUpdateOrderWhenItDoesNotExistShouldThrow() {
		String missingId = getNewStringId();
		Order missingOrder = newOrderWithId(missingId, OPEN);
		assertThatThrownBy(() -> orderRepository.update(missingOrder)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Order with id " + missingId + " not found.");
	}

	@Test
	@DisplayName("Method 'update' should be bound to the repository session")
	void testUpdateOrderShouldBeBoundToTheRepositorySession() {
		String idToUpdate = getNewStringId();
		Order toUpdate = newOrderWithId(idToUpdate, OPEN);
		Order notUpdated = newOrderWithId(idToUpdate, OPEN);
		saveTestOrderToDatabase(toUpdate);
		toUpdate.setStatus(CLOSED);
		session.startTransaction();
		orderRepository.update(toUpdate);
		assertThat(readAllOrderFromDatabase()).containsExactly(notUpdated);
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
