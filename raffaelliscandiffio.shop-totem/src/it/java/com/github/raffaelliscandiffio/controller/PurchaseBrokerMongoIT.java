package com.github.raffaelliscandiffio.controller;

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
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.github.raffaelliscandiffio.repository.mongo.ProductMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.StockMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Testcontainers(disabledWithoutDocker = true)
class PurchaseBrokerMongoIT {

	private ProductRepository productRepository;
	private StockRepository stockRepository;
	private PurchaseBroker broker;

	@Container
	public static final MongoDBContainer mongo = new MongoDBContainer("mongo:5.0.6");

	private MongoClient client;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;

	private static final String TOTEM_DB_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";

	@BeforeEach
	public void setup() {
		client = new MongoClient(new ServerAddress(mongo.getContainerIpAddress(), mongo.getFirstMappedPort()));
		productRepository = new ProductMongoRepository(client, TOTEM_DB_NAME, PRODUCT_COLLECTION_NAME);
		stockRepository = new StockMongoRepository(client, TOTEM_DB_NAME, STOCK_COLLECTION_NAME);
		MongoDatabase database = client.getDatabase(TOTEM_DB_NAME);
		// make sure we always start with a clean database
		database.drop();
		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		stockCollection = database.getCollection(STOCK_COLLECTION_NAME);
		broker = new PurchaseBroker(productRepository, stockRepository);
	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@DisplayName("'saveNewProductInStock' should save product and stock in db")
	@Test
	void testsaveNewProductInStock() {
		broker.saveNewProductInStock(1, "pizza", 5.5, 100);
		assertThat(readAllProductsFromDatabase()).containsExactly(new Product(1, "pizza", 5.5));
		assertThat(readAllStocksFromDatabase()).containsExactly(new Stock(1, 100));
	}

	@DisplayName("'retrieveProducts' should return products in db")
	@Test
	void testRetrieveProducts() {
		Product product1 = new Product(1, "pasta", 2.3);
		Product product2 = new Product(2, "pizza", 5.5);
		productRepository.save(product1);
		productRepository.save(product2);
		assertThat(broker.retrieveProducts()).containsExactly(product1, product2);
	}

	private List<Product> readAllProductsFromDatabase() {
		return StreamSupport.stream(productCollection.find().spliterator(), false)
				.map(d -> new Product(Long.valueOf("" + d.get("_id")), "" + d.get("name"),
						Double.valueOf("" + d.get("price"))))
				.collect(Collectors.toList());
	}

	private List<Stock> readAllStocksFromDatabase() {
		return StreamSupport.stream(stockCollection.find().spliterator(), false)
				.map(d -> new Stock(Long.valueOf("" + d.get("_id")), Integer.valueOf("" + d.get("quantity"))))
				.collect(Collectors.toList());
	}

}
