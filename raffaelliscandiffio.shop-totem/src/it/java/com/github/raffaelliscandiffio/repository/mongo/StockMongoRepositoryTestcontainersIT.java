package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
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
import com.github.raffaelliscandiffio.model.Stock;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class StockMongoRepositoryTestcontainersIT {

	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final String PRODUCT_NAME_2 = "pasta";
	private static final double PRICE = 3.0;
	private static final int QUANTITY = 4;

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	private MongoClient client;
	private StockMongoRepository stockRepository;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;

	private Product product_1;
	private Product product_2;

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		stockRepository = new StockMongoRepository(client, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
				STOCK_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(DATABASE_NAME);
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		stockCollection = database.getCollection(STOCK_COLLECTION_NAME);
		product_1 = new Product(PRODUCT_NAME_1, PRICE);
		product_2 = new Product(PRODUCT_NAME_2, PRICE);
	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("Insert Stock in database with 'save'")
	void testSaveProduct() {
		Stock stock = new Stock(product_1, QUANTITY);
		addTestProductToDatabase(product_1);
		stockRepository.save(stock);
		String assignedId = stock.getId();
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllStocksFromDatabase())
				.containsExactly(createTestStockWithId(assignedId, product_1, QUANTITY));
		softly.assertAll();
	}

	@Test
	@DisplayName("Retrieve Stock by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String stockId = new ObjectId().toString();
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
		addTestStockToDatabase(stockId, product_1, QUANTITY);
		addTestStockToDatabase(new ObjectId().toString(), product_2, 10);
		assertThat(stockRepository.findById(stockId)).isEqualTo(createTestStockWithId(stockId, product_1, QUANTITY));
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missing_id = new ObjectId().toString();
		assertThat(stockRepository.findById(missing_id)).isNull();
	}

	@Test
	@DisplayName("Update Stock in database with 'update'")
	void testUpdateProduct() {
		String stockId = new ObjectId().toString();
		String stockId_2 = new ObjectId().toString();
		addTestProductToDatabase(product_1);
		addTestProductToDatabase(product_2);
		addTestStockToDatabase(stockId, product_1, QUANTITY);
		addTestStockToDatabase(stockId_2, product_2, QUANTITY);
		int modifiedQuantity = QUANTITY + 5;
		stockRepository.update(createTestStockWithId(stockId, product_1, modifiedQuantity));
		assertThat(readAllStocksFromDatabase()).containsExactlyInAnyOrder(
				createTestStockWithId(stockId, product_1, modifiedQuantity),
				createTestStockWithId(stockId_2, product_2, QUANTITY));
	}

	private List<Stock> readAllStocksFromDatabase() {
		return StreamSupport.stream(stockCollection.find().spliterator(), false).map(stockDocument -> {
			String productId = stockDocument.getString("product");
			Document productDocument = productCollection.find(eq("_id", new ObjectId(productId))).first();
			Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
			product.setId(productDocument.get("_id").toString());
			Stock stock = createTestStockWithId(stockDocument.get("_id").toString(), product,
					stockDocument.getInteger("quantity"));
			return stock;
		}).collect(Collectors.toList());
	}

	private Stock createTestStockWithId(String id, Product product, int quantity) {
		Stock stock = new Stock(product, quantity);
		stock.setId(id);
		return stock;
	}

	private void addTestProductToDatabase(Product product) {
		Document productDocument = new Document().append("name", product.getName()).append("price", product.getPrice());
		productCollection.insertOne(productDocument);
		product.setId(productDocument.get("_id").toString());
	}

	private void addTestStockToDatabase(String objectIdString, Product product, int quantity) {
		stockCollection.insertOne(new Document().append("_id", new ObjectId(objectIdString))
				.append("product", product.getId()).append("quantity", quantity));

	}

}
