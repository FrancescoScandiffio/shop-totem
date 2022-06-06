package com.github.raffaelliscandiffio.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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

import com.github.raffaelliscandiffio.model.Product;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class ProductMongoRepositoryTestcontainersIT {

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	private MongoClient client;
	private ClientSession session;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;
	private static final String TOTEM_DB_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";

	private static final String NAME_1 = "product_1";
	private static final String NAME_2 = "product_2";
	private static final double PRICE_1 = 1.0;
	private static final double PRICE_2 = 2.0;

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		session = client.startSession();
		productRepository = new ProductMongoRepository(client, session, TOTEM_DB_NAME, PRODUCT_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(TOTEM_DB_NAME);
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);

	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("Insert product in database with 'save'")
	void testSaveProduct() {
		Product product = new Product(NAME_1, PRICE_1);
		SoftAssertions softly = new SoftAssertions();
		session.startTransaction();
		productRepository.save(product);
		String assignedId = product.getId();
		softly.assertThat(readAllProductsFromDatabase()).isEmpty();
		session.commitTransaction();
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllProductsFromDatabase()).containsExactly(newProductWithId(assignedId, NAME_1, PRICE_1));
		softly.assertAll();
	}

	@Test
	@DisplayName("Retrieve all the products from the database with 'findAll'")
	void testFindAllWhenDatabaseIsNotEmpty() {
		String id_1 = new ObjectId().toString();
		String id_2 = new ObjectId().toString();
		session.startTransaction();
		addTestProductToDatabaseWithSession(session, id_1, NAME_1, PRICE_1);
		addTestProductToDatabaseWithSession(session, id_2, NAME_2, PRICE_2);
		assertThat(productRepository.findAll()).containsExactlyInAnyOrder(newProductWithId(id_1, NAME_1, PRICE_1),
				newProductWithId(id_2, NAME_2, PRICE_2));
		session.commitTransaction();

	}

	@Test
	@DisplayName("Method 'findAll' should return an empty collection when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("Retrieve Product by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String id = new ObjectId().toString();
		addTestProductToDatabase(id, NAME_1, PRICE_1);
		addTestProductToDatabase(new ObjectId().toString(), NAME_2, PRICE_2);
		assertThat(productRepository.findById(id)).isEqualTo(newProductWithId(id, NAME_1, PRICE_1));
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missing_id = new ObjectId().toString();
		assertThat(productRepository.findById(missing_id)).isNull();
	}

	private List<Product> readAllProductsFromDatabase() {
		return StreamSupport.stream(productCollection.find().spliterator(), false)
				.map(d -> newProductWithId(d.get("_id").toString(), d.getString("name"), d.getDouble("price")))
				.collect(Collectors.toList());
	}

	private void addTestProductToDatabase(String id, String name, double price) {
		productCollection.insertOne(toProductDocument(id, name, price));
	}

	private void addTestProductToDatabaseWithSession(ClientSession session, String id, String name, double price) {
		productCollection.insertOne(session, toProductDocument(id, name, price));
	}

	private Document toProductDocument(String id, String name, double price) {
		return new Document().append("_id", new ObjectId(id)).append("name", name).append("price", price);
	}

	private Product newProductWithId(String id, String name, double price) {
		Product product = new Product(name, price);
		product.setId(id);
		return product;
	}

}
