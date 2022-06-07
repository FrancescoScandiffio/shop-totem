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
		session.close();
		client.close();
	}

	@Test
	@DisplayName("Insert Product in database with 'save'")
	void testSaveProduct() {
		Product product = new Product(NAME_1, PRICE_1);
		productRepository.save(product);
		String assignedId = product.getId();
		Product expectedResult = newProductWithId(assignedId, NAME_1, PRICE_1);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllProductFromDatabase()).containsExactly(expectedResult);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should be bound to the repository session")
	void testSaveProductShouldBeBoundToTheRepositorySession() {
		Product product = new Product(NAME_1, PRICE_1);
		session.startTransaction();
		productRepository.save(product);
		assertThat(readAllProductFromDatabase()).isEmpty();
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve Product by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String idToFind = getNewStringId();
		Product product_1 = newProductWithId(getNewStringId(), NAME_1, PRICE_1);
		Product product_2 = newProductWithId(idToFind, NAME_1, PRICE_1);
		Product expectedResult = newProductWithId(idToFind, NAME_1, PRICE_1);
		saveTestProductToDatabase(product_1);
		saveTestProductToDatabase(product_2);
		assertThat(productRepository.findById(idToFind)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missingId = getNewStringId();
		assertThat(productRepository.findById(missingId)).isNull();
	}

	@Test
	@DisplayName("Method 'findById' should be bound to the repository session")
	void testFindByIdShouldBeBoundToTheRepositorySession() {
		String idToFind = getNewStringId();
		Product product_1 = newProductWithId(idToFind, NAME_1, PRICE_1);
		Product expectedResult = newProductWithId(idToFind, NAME_1, PRICE_1);
		session.startTransaction();
		saveTestProductToDatabaseWithSession(session, product_1);
		assertThat(productRepository.findById(idToFind)).isEqualTo(expectedResult);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve all the products from the database with 'findAll'")
	void testFindAllWhenDatabaseIsNotEmpty() {
		String id_1 = new ObjectId().toString();
		String id_2 = new ObjectId().toString();
		Product product_1 = newProductWithId(id_1, NAME_1, PRICE_1);
		Product product_2 = newProductWithId(id_2, NAME_2, PRICE_2);
		saveTestProductToDatabase(product_1);
		saveTestProductToDatabase(product_2);
		Product expected_1 = newProductWithId(id_1, NAME_1, PRICE_1);
		Product expected_2 = newProductWithId(id_2, NAME_2, PRICE_2);
		assertThat(productRepository.findAll()).containsExactlyInAnyOrder(expected_1, expected_2);
	}

	@Test
	@DisplayName("Method 'findAll' should return an empty collection when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("Method 'findAll' should be bound to the repository session")
	void testFindAllShouldBeBoundToTheRepositorySession() {
		String id_1 = new ObjectId().toString();
		Product product_1 = newProductWithId(id_1, NAME_1, PRICE_1);
		session.startTransaction();
		saveTestProductToDatabaseWithSession(session, product_1);
		Product expected_1 = newProductWithId(id_1, NAME_1, PRICE_1);
		assertThat(productRepository.findAll()).containsExactly(expected_1);
		session.commitTransaction();
	}

	// Private utility methods

	private String getNewStringId() {
		return new ObjectId().toString();
	}

	private Product newProductWithId(String id, String name, double price) {
		Product product = new Product(name, price);
		product.setId(id);
		return product;
	}

	private Document fromProductToDocument(Product productWithId) {
		return new Document().append("_id", new ObjectId(productWithId.getId())).append("name", productWithId.getName())
				.append("price", productWithId.getPrice());
	}

	private void saveTestProductToDatabase(Product productWithId) {
		productCollection.insertOne(fromProductToDocument(productWithId));
	}

	private void saveTestProductToDatabaseWithSession(ClientSession session, Product productWithId) {
		productCollection.insertOne(session, fromProductToDocument(productWithId));
	}

	private List<Product> readAllProductFromDatabase() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(productDocument -> {
			Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
			product.setId(productDocument.get("_id").toString());
			return product;
		}).collect(Collectors.toList());
	}

}
