package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
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
	private static final double PRICE = 3.0;
	private static final int QUANTITY = 4;
	private MongoClient client;
	private OrderMongoRepository orderRepository;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> orderCollection;
	private Product product_1;
	private OrderItem item_1;

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		orderRepository = new OrderMongoRepository(client, DATABASE_NAME, ORDER_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(DATABASE_NAME);
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		orderCollection = database.getCollection(ORDER_COLLECTION_NAME);
		product_1 = new Product(PRODUCT_NAME_1, PRICE);
		item_1 = new OrderItem(product_1, QUANTITY);
		addTestProductToDatabase(product_1);

	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("Insert Order in database with 'save'")
	void testSaveProduct() {
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

	// Private utility methods

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

}
