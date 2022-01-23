package com.github.raffaelliscandiffio.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Product;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

class ProductMongoRepositoryTest {

	private static MongoServer server;
	private static InetSocketAddress serverAddress;

	private MongoClient client;
	private ProductMongoRepository productRepository;
	private MongoCollection<Document> productCollection;

	private static final String TOTEM_DB_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";

	@BeforeAll
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		// bind on a random local port
		serverAddress = server.bind();
	}

	@AfterAll
	public static void shutdownServer() {
		server.shutdown();
	}

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(serverAddress));
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
	@DisplayName("'FindAll' when the database is empty")
	void testFindAllWhenDatabaseIsEmpty() {
		assertThat(productRepository.findAll()).isEmpty();
	}
	
	@Test
	@DisplayName("'FindAll' when the database is not empty")
	void testFindAllWhenDatabaseIsNotEmpty() {
		addTestProductToDatabase(1, "pizza", 5.5);
		addTestProductToDatabase(2, "pasta", 2.3);
		assertThat(productRepository.findAll())
			.containsExactly(
				new Product(1, "pizza", 5.5),
				new Product(2, "pasta", 2.3));
	}
	
	
	private void addTestProductToDatabase(long id, String name, double price) {
		productCollection.insertOne(
				new Document()
					.append("id", id)
					.append("name", name)
					.append("price", price));
	}

}
