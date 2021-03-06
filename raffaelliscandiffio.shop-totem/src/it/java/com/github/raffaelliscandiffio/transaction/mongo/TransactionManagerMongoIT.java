package com.github.raffaelliscandiffio.transaction.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.api.SoftAssertions;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.exception.TransactionException;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.mongo.OrderItemMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.OrderMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.ProductMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.StockMongoRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


class TransactionManagerMongoIT {

	private static final String DB_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDER_ITEM_COLLECTION_NAME = "orderItem";

	private static final String PRODUCT_ID = "1";
	private static final String PRODUCT_NAME = "product";
	private static final double PRODUCT_PRICE = 1.0;


	private MongoClient client;
	private MongoCollection<Document> productCollection;
	private TransactionManagerMongo transactionManager;

	@BeforeEach
	public void setup() {
		String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
		client = MongoClients.create(uri);
		
		MongoDatabase database = client.getDatabase(DB_NAME);
		database.drop();
		database.createCollection(PRODUCT_COLLECTION_NAME);

		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		transactionManager = new TransactionManagerMongo(client, DB_NAME, PRODUCT_COLLECTION_NAME,
				STOCK_COLLECTION_NAME, ORDER_COLLECTION_NAME, ORDER_ITEM_COLLECTION_NAME);
	}

	@AfterEach
	public void tearDown() {
		client.close();
	}

	@Test
	@DisplayName("Method 'runInTransaction' should execute code in transaction and construct mongo repositories")
	void testRunInTransaction() {
		SoftAssertions softly = new SoftAssertions();
		Product product = newProductWithId(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		Product expectedResult = newProductWithId(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		Product result = transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, orderItemRepository) -> {
					ClientSession session = transactionManager.getSession();
					softly.assertThat(session).isNotNull();
					softly.assertThat(session.hasActiveTransaction()).isTrue();
					// field by field comparison
					softly.assertThat(productRepository).usingRecursiveComparison()
							.isEqualTo(new ProductMongoRepository(client, session, DB_NAME, PRODUCT_COLLECTION_NAME));
					softly.assertThat(stockRepository).usingRecursiveComparison().isEqualTo(new StockMongoRepository(
							client, session, DB_NAME, PRODUCT_COLLECTION_NAME, STOCK_COLLECTION_NAME));
					softly.assertThat(orderRepository).usingRecursiveComparison().isEqualTo(new OrderMongoRepository(
							client, session, DB_NAME, ORDER_COLLECTION_NAME, ORDER_ITEM_COLLECTION_NAME));
					softly.assertThat(orderItemRepository).usingRecursiveComparison()
							.isEqualTo(new OrderItemMongoRepository(client, session, DB_NAME, PRODUCT_COLLECTION_NAME,
									ORDER_COLLECTION_NAME, ORDER_ITEM_COLLECTION_NAME));
					productCollection.insertOne(session, productToDocument(product));
					return product;
				});
		ClientSession session = transactionManager.getSession();
		softly.assertThat(result).isEqualTo(expectedResult);
		softly.assertThat(session.hasActiveTransaction()).isFalse();
		softly.assertThat(readAllProductsFromDatabase()).containsExactly(expectedResult);
		// assert that the client session is close. It doesn't have an 'isClose' getter.
		softly.assertThatThrownBy(() -> session.startTransaction()).isInstanceOf(IllegalStateException.class);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'runInTransaction' should rollback and throw a new exception when an Exception occurs")
	void testRunInTransactionWhenExceptionOccursShouldRollbackAndThrowNew() {
		String message = "Exception message";
		SoftAssertions softly = new SoftAssertions();
		Product product = newProductWithId(PRODUCT_ID, PRODUCT_NAME, PRODUCT_PRICE);
		softly.assertThatThrownBy(() -> transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, orderItemRepository) -> {
					ClientSession session = transactionManager.getSession();
					productCollection.insertOne(session, productToDocument((product)));
					throw new RuntimeException(message);
				})).isInstanceOf(TransactionException.class).hasMessage(message);
		softly.assertThat(readAllProductsFromDatabase()).isEmpty();
		// assert that the client session is close. It doesn't have an 'isClose' getter.
		softly.assertThatThrownBy(() -> transactionManager.getSession().startTransaction())
				.isInstanceOf(IllegalStateException.class);
		softly.assertAll();
	}

	private Document productToDocument(Product productWithId) {
		return new Document().append("_id", productWithId.getId()).append("name", productWithId.getName())
				.append("price", productWithId.getPrice());
	}

	private List<Product> readAllProductsFromDatabase() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(d -> {
			Product product = new Product(d.getString("name"), d.getDouble("price"));
			product.setId(d.get("_id").toString());
			return product;
		}).collect(Collectors.toList());
	}

	private Product newProductWithId(String id, String name, double price) {
		Product p = new Product(name, price);
		p.setId(id);
		return p;
	}
}
