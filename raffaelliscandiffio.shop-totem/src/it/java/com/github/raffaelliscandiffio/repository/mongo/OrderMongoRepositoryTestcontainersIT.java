package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class OrderMongoRepositoryTestcontainersIT {

	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final double PRICE = 3.0;
	private static final int QUANTITY = 4;
	private MongoClient client;
	private OrderMongoRepository orderRepository;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> orderCollection;
	private Product product_1;
	private Product product_2;
	private OrderItem item_1;
	private OrderItem item_2;

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		orderRepository = new OrderMongoRepository(client, DATABASE_NAME, ORDER_COLLECTION_NAME,
				PRODUCT_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(DATABASE_NAME);
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		orderCollection = database.getCollection(ORDER_COLLECTION_NAME);
		product_1 = new Product(PRODUCT_NAME_1, PRICE);
		product_2 = new Product(PRODUCT_NAME_2, PRICE);
		item_1 = new OrderItem(product_1, QUANTITY);
		item_2 = new OrderItem(product_2, QUANTITY);
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("Insert Order in database with 'save'")
	void testSaveOrder() {
		Order order = new Order(new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN);
		orderRepository.save(order);
		String assignedId = order.getId();
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllOrderFromDatabase()).containsExactly(
				createOrderWithId(assignedId, new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN));
		softly.assertAll();
	}

	@Test
	@DisplayName("Retrieve Order by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String orderId = getNewStringId();
		addTestOrderToDatabase(getNewStringId(), new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN);
		addTestOrderToDatabase(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.CLOSED);
		assertThat(orderRepository.findById(orderId)).isEqualTo(
				createOrderWithId(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.CLOSED));
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missing_id = getNewStringId();
		assertThat(orderRepository.findById(missing_id)).isNull();
	}

	@Test
	@DisplayName("Remove the specified order from the collection with 'delete'")
	void testDelete() {
		String orderId = getNewStringId();
		String removeId = getNewStringId();
		addTestOrderToDatabase(removeId, new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN);
		addTestOrderToDatabase(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.CLOSED);
		orderRepository.delete(
				createOrderWithId(removeId, new LinkedHashSet<OrderItem>(Arrays.asList(item_1)), OrderStatus.OPEN));
		assertThat(readAllOrderFromDatabase()).containsExactly(
				createOrderWithId(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.CLOSED));
	}

	@Test
	@DisplayName("Update Order with 'update'")
	void updateOrder() {
		String modifyId = getNewStringId();
		String orderId = getNewStringId();
		Set<OrderItem> items = new LinkedHashSet<OrderItem>(Arrays.asList(item_1));
		Order toModify = createOrderWithId(modifyId, items, OrderStatus.OPEN);
		addTestOrderToDatabase(modifyId, items, OrderStatus.OPEN);
		addTestOrderToDatabase(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.CLOSED);
		items.add(item_2);
		orderRepository.update(toModify);
		assertThat(readAllOrderFromDatabase()).containsExactly(
				createOrderWithId(modifyId, new LinkedHashSet<OrderItem>(Arrays.asList(item_1, item_2)),
						OrderStatus.OPEN),
				createOrderWithId(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)), OrderStatus.CLOSED));
	}

	@Test
	@DisplayName("Update Order when order does not exist should throw")
	void updateOrderWhenDoesNotExistShouldThrow() {
		String orderId = getNewStringId();
		Order unsavedOrder = createOrderWithId(orderId, new LinkedHashSet<OrderItem>(Arrays.asList(item_2)),
				OrderStatus.CLOSED);
		assertThatThrownBy(() -> orderRepository.update(unsavedOrder)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Order with id " + orderId + " not found.");
		assertThat(readAllOrderFromDatabase()).isEmpty();
	}

	// Private utility methods

	private String getNewStringId() {
		return new ObjectId().toString();
	}

	private List<Order> readAllOrderFromDatabase() {
		return StreamSupport.stream(orderCollection.find().spliterator(), false).map(orderDocument -> {
			List<Document> itemDocuments = orderDocument.getList("items", Document.class);
			LinkedHashSet<OrderItem> items = new LinkedHashSet<>();
			itemDocuments.forEach(document -> items.add(fromDocumentToOrderItem(document)));
			Order order = new Order(items, OrderStatus.valueOf(orderDocument.getString("status")));
			order.setId(orderDocument.get("_id").toString());
			return order;
		}).collect(Collectors.toList());
	}

	private OrderItem fromDocumentToOrderItem(Document document) {
		Document productDocument = productCollection.find(eq("_id", new ObjectId(document.getString("product"))))
				.first();
		Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
		product.setId(productDocument.get("_id").toString());
		return new OrderItem(product, document.getInteger("quantity"));
	}

	private Order createOrderWithId(String id, Set<OrderItem> items, OrderStatus status) {
		Order order = new Order(items, status);
		order.setId(id);
		return order;
	}

	private void addTestProductToDatabase(Product product) {
		Document productDocument = new Document().append("name", product.getName()).append("price", product.getPrice());
		productCollection.insertOne(productDocument);
		product.setId(productDocument.get("_id").toString());
	}

	private void addTestOrderToDatabase(String id, Set<OrderItem> items, OrderStatus status) {
		List<Document> embeddedItems = new ArrayList<>();
		items.forEach(item -> embeddedItems.add(
				new Document().append("product", item.getProduct().getId()).append("quantity", item.getQuantity())));
		orderCollection.insertOne(new Document().append("_id", new ObjectId(id)).append("items", embeddedItems)
				.append("status", status.toString()));

	}

}
