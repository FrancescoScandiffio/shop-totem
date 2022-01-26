package com.github.raffaelliscandiffio.repository.mongo;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Stock;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

class StockMongoRepositoryTest {
	
	private static MongoServer server;
	private static InetSocketAddress serverAddress;

	private MongoClient client;
	private StockMongoRepository stockRepository;
	private MongoCollection<Document> stockCollection;

	private static final String TOTEM_DB_NAME = "totem";
	private static final String STOCK_COLLECTION_NAME = "stock";

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
		stockRepository = new StockMongoRepository(client, TOTEM_DB_NAME, STOCK_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(TOTEM_DB_NAME);
		// make sure we always start with a clean database
		database.drop();
		stockCollection = database.getCollection(STOCK_COLLECTION_NAME);
	}

	@AfterEach
	public void tearDown() {
		client.close();
	}
	
	@Test
	@DisplayName("'findById' when the id is not found should throw NoSuchElementException")
	void testFindByIdWhenIdIsNotFoundShouldThrowNoSuchElementException() {
		assertThatThrownBy(() -> stockRepository.findById(1)).isInstanceOf(NoSuchElementException.class)
		.hasMessage("Stock with id 1 not found");
	}
	
	@Test
	@DisplayName("'findById' when the id is found")
	void testFindByIdWhenIdIsFound() {
		addTestStockToDatabase(1, 50);
		addTestStockToDatabase(2, 100);
		assertThat(stockRepository.findById(2)).isEqualTo(new Stock(2, 100));
	}
	
	@Test
	@DisplayName("'save' stock to repository")
	void testSaveStock() {
		Stock stock = new Stock(1, 50);
		stockRepository.save(stock);
		assertThat(readAllStocksFromDatabase())
			.containsExactly(stock);
	}
	
	
	private void addTestStockToDatabase(long id, int quantity) {
		stockCollection.insertOne(new Document().append("_id", id).append("quantity", quantity));
	}
	
	private List<Stock> readAllStocksFromDatabase() {
		return StreamSupport.
			stream(stockCollection.find().spliterator(), false)
				.map(d -> new Stock(Long.valueOf("" + d.get("_id")), Integer.valueOf("" + d.get("quantity"))))
				.collect(Collectors.toList());
	}

}
