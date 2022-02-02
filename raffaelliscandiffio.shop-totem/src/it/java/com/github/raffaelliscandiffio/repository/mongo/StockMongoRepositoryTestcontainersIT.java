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

class StockMongoRepositoryTestcontainersIT {

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
	@DisplayName("'findById' when the id is not found should return null")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		assertThat(stockRepository.findById(1)).isNull();
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
		assertThat(readAllStocksFromDatabase()).containsExactly(stock);
	}

	@Test
	@DisplayName("'save' stock to repository should not save if stock id is already existing")
	void testSaveStockIfIdAlreadyExistingShouldNotSave() {
		addTestStockToDatabase(1, 20);
		Stock stock = new Stock(1, 50);

		stockRepository.save(stock);

		assertThat(readAllStocksFromDatabase()).containsExactly(new Stock(1, 20));
	}

	@Test
	@DisplayName("'update' should update stock in repository")
	void testUpdateShouldUpdateStockInRepository() {
		addTestStockToDatabase(1, 50);
		addTestStockToDatabase(2, 55);
		Stock stock = new Stock(1, 100);

		stockRepository.update(stock);

		assertThat(readAllStocksFromDatabase()).containsExactly(new Stock[] { stock, new Stock(2, 55) });
	}

	@Test
	@DisplayName("'update' should not update stock in repository when stock id is not found in repository")
	void testUpdateShouldNotUpdateStockInRepositoryIfIdNotExisting() {
		addTestStockToDatabase(1, 50);
		Stock stock = new Stock(2, 100);

		assertThatThrownBy(() -> stockRepository.update(stock)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Stock with id 2 cannot be updated because not found in database");

		assertThat(readAllStocksFromDatabase()).containsExactly(new Stock(1, 50));
	}

	private void addTestStockToDatabase(long id, int quantity) {
		stockCollection.insertOne(new Document().append("_id", id).append("quantity", quantity));
	}

	private List<Stock> readAllStocksFromDatabase() {
		return StreamSupport.stream(stockCollection.find().spliterator(), false)
				.map(d -> new Stock(Long.valueOf("" + d.get("_id")), Integer.valueOf("" + d.get("quantity"))))
				.collect(Collectors.toList());
	}

}
