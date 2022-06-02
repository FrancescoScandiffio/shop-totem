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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class ProductMongoRepositoryTestcontainersIT {

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;
	private static final String TOTEM_DB_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";

	private Product product_1;
	private Product product_2;

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		productRepository = new ProductMongoRepository(client, TOTEM_DB_NAME, PRODUCT_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(TOTEM_DB_NAME);
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		product_1 = new Product("pizza", 5.0);
		product_2 = new Product("pasta", 0.8);

	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("Insert product in database with 'save'")
	void testSaveProduct() {
		productRepository.save(product_1);
		String assignedId = product_1.getId();

		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllProductsFromDatabase()).containsExactly(product_1);
		softly.assertAll();
	}

	@Test
	@DisplayName("Retrieve all the products from the database with 'findAll'")
	void testFindAllWhenDatabaseIsNotEmpty() {
		product_1.setId(new ObjectId().toString());
		product_2.setId(new ObjectId().toString());
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
		assertThat(productRepository.findAll()).containsExactlyInAnyOrder(product_1, product_2);
	}

	@Test
	@DisplayName("Method 'findAll' should return an empty collection when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("Retrieve Product by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		product_1.setId(new ObjectId().toString());
		product_2.setId(new ObjectId().toString());
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
		assertThat(productRepository.findById(product_1.getId())).isEqualTo(product_1);
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missing_id = new ObjectId().toString();
		assertThat(productRepository.findById(missing_id)).isNull();
	}

	private List<Product> readAllProductsFromDatabase() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(d -> {
			Product p = new Product(d.getString("name"), d.getDouble("price"));
			p.setId(d.get("_id").toString());
			return p;
		}).collect(Collectors.toList());
	}

	private void addTestProductToDatabase(Product product) {
		Document productDocument = new Document().append("_id", new ObjectId(product.getId()))
				.append("name", product.getName()).append("price", product.getPrice());
		productCollection.insertOne(productDocument);
	}
}
