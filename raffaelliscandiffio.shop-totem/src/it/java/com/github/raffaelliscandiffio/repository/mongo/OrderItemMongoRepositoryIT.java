package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
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
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class OrderItemMongoRepositoryIT {

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");
	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDERITEM_COLLECTION_NAME = "orderItem";
	private static final int QUANTITY_1 = 1;
	private static final int QUANTITY_2 = 2;
	private static final String NAME_1 = "product_1";
	private static final String NAME_2 = "product_2";
	private static final double PRICE_1 = 1.0;
	private static final double PRICE_2 = 2.0;
	private static final OrderStatus ORDER_OPEN = OrderStatus.OPEN;

	private MongoClient client;
	private ClientSession session;
	private OrderItemMongoRepository orderItemRepository;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> orderCollection;
	private MongoCollection<Document> orderItemCollection;

	private Product product_1;
	private Product product_2;
	private Order order_1;
	private Order order_2;

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		MongoDatabase database = client.getDatabase(DATABASE_NAME);
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		orderCollection = database.getCollection(ORDER_COLLECTION_NAME);
		orderItemCollection = database.getCollection(ORDERITEM_COLLECTION_NAME);
		session = client.startSession();
		orderItemRepository = new OrderItemMongoRepository(client, session, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
				ORDER_COLLECTION_NAME, ORDERITEM_COLLECTION_NAME);
		product_1 = saveTestProductToDatabase(new Product(NAME_1, PRICE_1));
		product_2 = saveTestProductToDatabase(new Product(NAME_2, PRICE_2));
		order_1 = saveTestOrderToDatabase(new Order(OrderStatus.OPEN));
		order_2 = saveTestOrderToDatabase(new Order(OrderStatus.CLOSED));
	}

	@AfterEach
	public void tearDown() {
		session.close();
		client.close();
	}

	@Test
	@DisplayName("Insert OrderItem in database with 'save'")
	void testSaveOrderItem() {
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		SoftAssertions softly = new SoftAssertions();
		orderItemRepository.save(orderItem);
		String assignedId = orderItem.getId();
		OrderItem expectedResult = newOrderItemWithId(assignedId, product_1, order_1, QUANTITY_1);
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> getObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllOrderItemFromDatabase()).containsExactly(expectedResult);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should throw when the referenced product does not exist")
	void testSaveOrderItemWhenReferencedProductDoesNotExistShouldThrow() {
		productCollection.drop();
		OrderItem orderItem = new OrderItem(product_1, order_1, QUANTITY_1);
		assertThatThrownBy(() -> orderItemRepository.save(orderItem)).isInstanceOf(NoSuchElementException.class)
				.hasMessage(
						"Reference error, cannot save OrderItem: Product with " + product_1.getId() + " not found.");
	}

	@Test
	@DisplayName("Method 'save' should throw when the referenced order does not exist")
	void testSaveOrderItemWhenReferencedOrderDoesNotExistShouldThrow() {
		orderCollection.drop();
		OrderItem orderItem = new OrderItem(product_1, order_1, QUANTITY_1);
		assertThatThrownBy(() -> orderItemRepository.save(orderItem)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Reference error, cannot save OrderItem: Order with " + order_1.getId() + " not found.");
	}

	@Test
	@DisplayName("In method 'save', the existence check of order should be bound to the repository session")
	void testSaveOrderItemCheckOnOrderShouldBeBoundToTheRepositorySession() {
		orderCollection.drop();
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		session.startTransaction();
		saveTestOrderToDatabaseWithSession(session, order_1);
		assertThatCode(() -> orderItemRepository.save(orderItem)).doesNotThrowAnyException();
		session.commitTransaction();
	}

	@Test
	@DisplayName("Method 'save' should be bound to the repository session")
	void testSaveOrderItemShouldBeBoundToTheRepositorySession() {
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		session.startTransaction();
		orderItemRepository.save(orderItem);
		assertThat(readAllOrderItemFromDatabase()).isEmpty();
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve OrderItem by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String idToFind = getNewStringId();
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		OrderItem itemToFind = newOrderItemWithId(idToFind, product_2, order_2, QUANTITY_2);
		OrderItem expectedResult = newOrderItemWithId(idToFind, product_2, order_2, QUANTITY_2);
		saveTestOrderItemToDatabase(orderItem);
		saveTestOrderItemToDatabase(itemToFind);
		assertThat(orderItemRepository.findById(idToFind)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missingId = getNewStringId();
		assertThat(orderItemRepository.findById(missingId)).isNull();
	}

	@Test
	@DisplayName("Method 'findById' should be bound to the repository session")
	void testFindByIdShouldBeBoundToTheRepositorySession() {
		session.startTransaction();
		Product sessionProduct = saveTestProductToDatabaseWithSession(session, new Product(NAME_1, PRICE_1));
		Order sessionOrder = saveTestOrderToDatabaseWithSession(session, new Order(ORDER_OPEN));
		String idToFind = getNewStringId();
		OrderItem itemToFind = newOrderItemWithId(idToFind, sessionProduct, sessionOrder, QUANTITY_1);
		saveTestOrderItemToDatabaseWithSession(session, itemToFind);
		assertThat(orderItemRepository.findById(idToFind)).isEqualTo(itemToFind);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Remove OrderItem from the collection by id with 'delete'")
	void testDelete() {
		String idToRemove = getNewStringId();
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		OrderItem itemToRemove = newOrderItemWithId(idToRemove, product_2, order_2, QUANTITY_2);
		saveTestOrderItemToDatabase(orderItem);
		saveTestOrderItemToDatabase(itemToRemove);
		orderItemRepository.delete(idToRemove);
		assertThat(readAllOrderItemFromDatabase()).containsExactly(orderItem);
	}

	@Test
	@DisplayName("Method 'delete' should be bound to the repository session")
	void testDeleteShouldBeBoundToTheRepositorySession() {
		String idToRemove = getNewStringId();
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		OrderItem itemToRemove = newOrderItemWithId(idToRemove, product_2, order_2, QUANTITY_2);
		session.startTransaction();
		saveTestOrderItemToDatabaseWithSession(session, orderItem);
		saveTestOrderItemToDatabaseWithSession(session, itemToRemove);
		orderItemRepository.delete(idToRemove);
		session.commitTransaction();
		assertThat(readAllOrderItemFromDatabase()).containsExactly(orderItem);
	}

	// Private utility methods

	private String getNewStringId() {
		return new ObjectId().toString();
	}

	private ObjectId getObjectId(String id) {
		return new ObjectId(id);
	}

	// --- OrderItem ---

	private OrderItem newOrderItemWithId(String id, Product product, Order order, int quantity) {
		OrderItem item = new OrderItem(product, order, quantity);
		item.setId(id);
		return item;
	}

	private List<OrderItem> readAllOrderItemFromDatabase() {
		return StreamSupport.stream(orderItemCollection.find().spliterator(), false).map(orderItemDocument -> {
			String productId = orderItemDocument.getString("product");
			String orderId = orderItemDocument.getString("order");
			Document productDocument = productCollection.find(eq("_id", getObjectId(productId))).first();
			Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
			product.setId(productId);
			Document orderDocument = orderCollection.find(eq("_id", getObjectId(orderId))).first();
			Order order = new Order(OrderStatus.valueOf(orderDocument.getString("status")));
			order.setId(orderId);
			OrderItem orderItem = new OrderItem(product, order, orderItemDocument.getInteger("quantity"));
			orderItem.setId(orderItemDocument.get("_id").toString());
			return orderItem;
		}).collect(Collectors.toList());
	}

	private void saveTestOrderItemToDatabase(OrderItem orderItemWithId) {
		orderItemCollection.insertOne(fromOrderItemToDocument(orderItemWithId));
	}

	private void saveTestOrderItemToDatabaseWithSession(ClientSession session, OrderItem orderItemWithId) {
		orderItemCollection.insertOne(session, fromOrderItemToDocument(orderItemWithId));
	}

	private Document fromOrderItemToDocument(OrderItem orderItemWithId) {
		return new Document().append("_id", getObjectId(orderItemWithId.getId()))
				.append("product", orderItemWithId.getProduct().getId())
				.append("order", orderItemWithId.getOrder().getId()).append("quantity", orderItemWithId.getQuantity());
	}

	// --- Product ---

	private Document fromProductToDocument(Product productWithoutId) {
		return new Document().append("name", productWithoutId.getName()).append("price", productWithoutId.getPrice());
	}

	private Product saveTestProductToDatabase(Product productWithoutId) {
		Document doc = fromProductToDocument(productWithoutId);
		productCollection.insertOne(doc);
		productWithoutId.setId(doc.get("_id").toString());
		return productWithoutId;
	}

	private Product saveTestProductToDatabaseWithSession(ClientSession session, Product productWithoutId) {
		Document doc = fromProductToDocument(productWithoutId);
		productCollection.insertOne(session, doc);
		productWithoutId.setId(doc.get("_id").toString());
		return productWithoutId;
	}

	// --- Order ---

	private Document fromOrderToDocument(Order orderWithoutId) {
		return new Document().append("status", orderWithoutId.getStatus().toString());
	}

	private Order saveTestOrderToDatabase(Order orderWithoutId) {
		Document doc = fromOrderToDocument(orderWithoutId);
		orderCollection.insertOne(doc);
		orderWithoutId.setId(doc.get("_id").toString());
		return orderWithoutId;
	}

	private Order saveTestOrderToDatabaseWithSession(ClientSession session, Order orderWithoutId) {
		Document doc = fromOrderToDocument(orderWithoutId);
		orderCollection.insertOne(session, doc);
		orderWithoutId.setId(doc.get("_id").toString());
		return orderWithoutId;
	}

}
