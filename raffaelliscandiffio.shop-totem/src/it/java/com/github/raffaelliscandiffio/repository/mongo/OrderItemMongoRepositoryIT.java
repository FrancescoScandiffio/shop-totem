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

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

class OrderItemMongoRepositoryIT {

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
	private MongoDatabase database;

	private Product product_1;
	private Product product_2;
	private Order order_1;
	private Order order_2;

	@BeforeEach
	public void setup() {
		String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
		client = MongoClients.create(uri);

		database = client.getDatabase(DATABASE_NAME);
		database.drop();
		database.createCollection(PRODUCT_COLLECTION_NAME);
		database.createCollection(ORDER_COLLECTION_NAME);
		database.createCollection(ORDERITEM_COLLECTION_NAME);

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
	@DisplayName("Method 'save' should throw when the product reference does not exist")
	void testSaveOrderItemWhenTheProductReferenceDoesNotExistShouldThrow() {
		dropAndCreateCollection(productCollection);
		OrderItem orderItem = new OrderItem(product_1, order_1, QUANTITY_1);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThatThrownBy(() -> orderItemRepository.save(orderItem)).isInstanceOf(NoSuchElementException.class)
				.hasMessage(
						"Reference error, cannot save OrderItem: Product with id " + product_1.getId() + " not found.");
		softly.assertThat(readAllOrderItemFromDatabase()).isEmpty();
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should throw when the order reference does not exist")
	void testSaveOrderItemWhenTheOrderReferenceDoesNotExistShouldThrow() {
		dropAndCreateCollection(orderCollection);
		OrderItem orderItem = new OrderItem(product_1, order_1, QUANTITY_1);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThatThrownBy(() -> orderItemRepository.save(orderItem)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Reference error, cannot save OrderItem: Order with id " + order_1.getId() + " not found.");
		softly.assertThat(readAllOrderItemFromDatabase()).isEmpty();
		softly.assertAll();
	}

	@Test
	@DisplayName("In method 'save', the existence check of order should be bound to the repository session")
	void testSaveOrderItemCheckOnOrderShouldBeBoundToTheRepositorySession() {
		dropAndCreateCollection(orderItemCollection);
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

	@Test
	@DisplayName("Update OrderItem with 'update'")
	void testUpdateOrderItem() {
		String idToUpdate = getNewStringId();
		OrderItem itemToUpdate = newOrderItemWithId(idToUpdate, product_1, order_1, QUANTITY_1);
		OrderItem orderItem = newOrderItemWithId(getNewStringId(), product_2, order_2, QUANTITY_2);
		saveTestOrderItemToDatabase(orderItem);
		saveTestOrderItemToDatabase(itemToUpdate);
		itemToUpdate.setQuantity(QUANTITY_2);
		OrderItem expectedResult = newOrderItemWithId(idToUpdate, product_1, order_1, QUANTITY_2);
		orderItemRepository.update(itemToUpdate);
		assertThat(readAllOrderItemFromDatabase()).containsExactlyInAnyOrder(orderItem, expectedResult);
	}

	@Test
	@DisplayName("Update OrderItem when orderItem does not exist should throw")
	void testUpdateOrderItemWhenItDoesNotExistShouldThrow() {
		String missingId = getNewStringId();
		OrderItem missingOrderItem = newOrderItemWithId(missingId, product_1, order_1, QUANTITY_1);
		assertThatThrownBy(() -> orderItemRepository.update(missingOrderItem))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessage("OrderItem with id " + missingId + " not found.");
	}

	@Test
	@DisplayName("Method 'update' should be bound to the repository session")
	void testUpdateOrderItemShouldBeBoundToTheRepositorySession() {
		String idToUpdate = getNewStringId();
		OrderItem toUpdate = newOrderItemWithId(idToUpdate, product_1, order_1, QUANTITY_1);
		OrderItem notUpdated = newOrderItemWithId(idToUpdate, product_1, order_1, QUANTITY_1);
		saveTestOrderItemToDatabase(toUpdate);
		toUpdate.setQuantity(QUANTITY_2);
		session.startTransaction();
		orderItemRepository.update(toUpdate);
		assertThat(readAllOrderItemFromDatabase()).containsExactly(notUpdated);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Get list of OrderItems by order_id when there is exactly one match")
	void testGetListByOrderIdWhenThereIsExactlyOneMatch() {
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		OrderItem match_2 = newOrderItemWithId(getNewStringId(), product_2, order_2, QUANTITY_1);
		saveTestOrderItemToDatabase(match_1);
		saveTestOrderItemToDatabase(match_2);

		assertThat(orderItemRepository.getListByOrderId(order_1.getId())).containsExactly(match_1);
	}

	@Test
	@DisplayName("Get list of OrderItems by order_id when there are multiple matches")
	void testGetListByOrderIdWhenThereAreMultipleMatches() {
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		OrderItem match_2 = newOrderItemWithId(getNewStringId(), product_2, order_1, QUANTITY_1);
		saveTestOrderItemToDatabase(match_1);
		saveTestOrderItemToDatabase(match_2);
		assertThat(orderItemRepository.getListByOrderId(order_1.getId())).containsExactlyInAnyOrder(match_1, match_2);
	}

	@Test
	@DisplayName("Get list of OrderItems by order_id when there are 0 matches should return empty list")
	void testGetListByOrderIdWhenThereIsNoMatchShouldReturnEmptyList() {
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_2, QUANTITY_1);
		OrderItem match_2 = newOrderItemWithId(getNewStringId(), product_2, order_2, QUANTITY_1);
		saveTestOrderItemToDatabase(match_1);
		saveTestOrderItemToDatabase(match_2);
		assertThat(orderItemRepository.getListByOrderId(order_1.getId())).isEmpty();
	}

	@Test
	@DisplayName("Method 'getListByOrderId' should be bound to the repository session")
	void testGetListByOrderIdShouldBeBoundToTheRepositorySession() {
		dropAndCreateCollection(orderCollection);
		session.startTransaction();
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		saveTestOrderToDatabaseWithSession(session, order_1);
		saveTestOrderItemToDatabaseWithSession(session, match_1);
		assertThat(orderItemRepository.getListByOrderId(order_1.getId())).containsExactly(match_1);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Find OrderItem by order_id and product_id when there is a match on both fields")
	void testFindOrderItemByOrderIdAndProductId() {
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		saveTestOrderItemToDatabase(match_1);

		assertThat(orderItemRepository.findByProductAndOrderId(product_1.getId(), order_1.getId())).isEqualTo(match_1);
	}

	@Test
	@DisplayName("Find OrderItem by order_id and product_id when there is no match on both fields should return null")
	void testFindOrderItemByOrderIdAndProductIdWhenAtLeasOneFieldDoesNotMatchShouldReturnNull() {
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		OrderItem match_2 = newOrderItemWithId(getNewStringId(), product_2, order_2, QUANTITY_1);
		saveTestOrderItemToDatabase(match_1);
		saveTestOrderItemToDatabase(match_2);

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(orderItemRepository.findByProductAndOrderId(product_1.getId(), order_2.getId())).isNull();
		softly.assertThat(orderItemRepository.findByProductAndOrderId(product_2.getId(), order_1.getId())).isNull();
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'findByProductAndOrderId' should be bound to the repository session")
	void testFindOrderItemByOrderIdAndProductIdShouldBeBoundToTheRepositorySession() {
		dropAndCreateCollection(orderCollection);
		session.startTransaction();
		saveTestOrderToDatabaseWithSession(session, order_1);
		OrderItem match_1 = newOrderItemWithId(getNewStringId(), product_1, order_1, QUANTITY_1);
		saveTestOrderItemToDatabaseWithSession(session, match_1);

		assertThat(orderItemRepository.findByProductAndOrderId(product_1.getId(), order_1.getId())).isEqualTo(match_1);
		session.commitTransaction();

	}

	// Private utility methods

	private String getNewStringId() {
		return new ObjectId().toString();
	}

	private ObjectId getObjectId(String id) {
		return new ObjectId(id);
	}

	private void dropAndCreateCollection(MongoCollection<Document> collection) {
		String name = collection.getNamespace().getCollectionName();
		collection.drop();
		database.createCollection(name);
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
