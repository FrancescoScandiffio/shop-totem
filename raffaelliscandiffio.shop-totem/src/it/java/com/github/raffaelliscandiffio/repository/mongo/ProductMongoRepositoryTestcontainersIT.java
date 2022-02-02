package com.github.raffaelliscandiffio.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
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

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		productRepository = new ProductMongoRepository(client, TOTEM_DB_NAME, PRODUCT_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(TOTEM_DB_NAME);
		// make sure we always start with a clean database
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("'findAll' when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("'findAll' when the database is not empty")
	void testFindAllWhenDatabaseIsNotEmpty() {
		addTestProductToDatabase(1, "pizza", 5.5);
		addTestProductToDatabase(2, "pasta", 2.3);
		assertThat(productRepository.findAll()).containsExactly(new Product(1, "pizza", 5.5),
				new Product(2, "pasta", 2.3));
	}

	@Test
	@DisplayName("'findById' when the id is not found should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(productRepository.findById(1)).isNull();
	}

	@Test
	@DisplayName("'findById' when the id is found")
	void testFindByIdWhenIdIsFound() {
		addTestProductToDatabase(1, "pizza", 5.5);
		addTestProductToDatabase(2, "pasta", 2.3);
		assertThat(productRepository.findById(2)).isEqualTo(new Product(2, "pasta", 2.3));
	}

	@Test
	@DisplayName("'save' product to repository")
	void testSaveProduct() {
		Product product = new Product(1, "pizza", 5.5);
		productRepository.save(product);
		assertThat(readAllProductsFromDatabase()).containsExactly(product);
	}

	private void addTestProductToDatabase(long id, String name, double price) {
		productCollection.insertOne(new Document().append("_id", id).append("name", name).append("price", price));
	}

	private List<Product> readAllProductsFromDatabase() {
		return StreamSupport.stream(productCollection.find().spliterator(), false)
				.map(d -> new Product(Long.valueOf("" + d.get("_id")), "" + d.get("name"),
						Double.valueOf("" + d.get("price"))))
				.collect(Collectors.toList());
	}
}
