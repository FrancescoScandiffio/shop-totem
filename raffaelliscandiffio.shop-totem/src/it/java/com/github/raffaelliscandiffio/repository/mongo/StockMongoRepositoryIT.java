package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.assertj.core.api.SoftAssertions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

class StockMongoRepositoryIT {

	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String PRODUCT_NAME_1 = "product_1";
	private static final String PRODUCT_NAME_2 = "product_2";
	private static final double PRICE = 3.0;
	private static final int QUANTITY_1 = 1;
	private static final int QUANTITY_2 = 2;

	private MongoClient client;
	private ClientSession session;
	private StockMongoRepository stockRepository;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;
	private MongoDatabase database;

	private Product product_1;
	private Product product_2;

	@BeforeEach
	public void setup() {
		String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
		client = MongoClients.create(uri);
		session = client.startSession();
		stockRepository = new StockMongoRepository(client, session, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
				STOCK_COLLECTION_NAME);
		database = client.getDatabase(DATABASE_NAME);
		database.drop();
		database.createCollection(PRODUCT_COLLECTION_NAME);
		database.createCollection(STOCK_COLLECTION_NAME);

		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		stockCollection = database.getCollection(STOCK_COLLECTION_NAME);
		product_1 = saveTestProductToDatabase(new Product(PRODUCT_NAME_1, PRICE));
		product_2 = saveTestProductToDatabase(new Product(PRODUCT_NAME_2, PRICE));
	}

	@AfterEach
	public void tearDown() {
		session.close();
		client.close();
	}

	@Test
	@DisplayName("Insert Stock in database with 'save'")
	void testSaveStock() {
		Stock stock = new Stock(product_1, QUANTITY_1);
		stockRepository.save(stock);
		String assignedId = stock.getId();
		Stock expectedResult = newStockWithId(assignedId, product_1, QUANTITY_1);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(assignedId).isNotNull();
		softly.assertThatCode(() -> new ObjectId(assignedId)).doesNotThrowAnyException();
		softly.assertThat(readAllStockFromDatabase()).containsExactly(expectedResult);
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should throw when the referenced Product does not exist")
	void testSaveStockWhenTheReferencedProductDoesNotExistShouldThrow() {
		productCollection.drop();
		Stock stock = new Stock(product_1, QUANTITY_1);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThatThrownBy(() -> stockRepository.save(stock)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Referenced Product with id " + product_1.getId() + " not found.");
		softly.assertThat(readAllStockFromDatabase()).isEmpty();
		softly.assertAll();
	}

	@Test
	@DisplayName("Method 'save' should be bound to the repository session")
	void testSaveStockShouldBeBoundToTheRepositorySession() {
		Stock stock = new Stock(product_1, QUANTITY_1);
		session.startTransaction();
		stockRepository.save(stock);
		assertThat(readAllStockFromDatabase()).isEmpty();
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve Stock by id with 'findById'")
	void testFindByIdWhenIdIsFound() {
		String idToFind = getNewStringId();
		Stock stock_1 = newStockWithId(getNewStringId(), product_1, QUANTITY_1);
		Stock stock_2 = newStockWithId(idToFind, product_2, QUANTITY_2);
		Stock expectedResult = newStockWithId(idToFind, product_2, QUANTITY_2);
		saveTestStockToDatabase(stock_1);
		saveTestStockToDatabase(stock_2);
		assertThat(stockRepository.findById(idToFind)).isEqualTo(expectedResult);
	}

	@Test
	@DisplayName("Method 'findById' should return null when the id is not found")
	void testFindByIdWhenIdIsNotFoundShouldReturnNull() {
		String missingId = getNewStringId();
		assertThat(stockRepository.findById(missingId)).isNull();
	}

	@Test
	@DisplayName("Method 'findById' should be bound to the repository session")
	void testFindByIdShouldBeBoundToTheRepositorySession() {
		dropAndCreateCollection(stockCollection);

		session.startTransaction();
		saveTestProductToDatabaseWithSession(session, product_1);
		String idToFind = getNewStringId();
		Stock stock_1 = newStockWithId(idToFind, product_1, QUANTITY_1);
		Stock expectedResult = newStockWithId(idToFind, product_1, QUANTITY_1);

		saveTestStockToDatabaseWithSession(session, stock_1);
		assertThat(stockRepository.findById(idToFind)).isEqualTo(expectedResult);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Update Stock with 'update'")
	void testUpdateStock() {
		String idToUpdate = getNewStringId();
		Stock stock_1 = newStockWithId(getNewStringId(), product_1, QUANTITY_1);
		Stock toUpdate = newStockWithId(idToUpdate, product_2, QUANTITY_2);
		saveTestStockToDatabase(stock_1);
		saveTestStockToDatabase(toUpdate);
		toUpdate.setQuantity(QUANTITY_1);
		Stock expectedResult = newStockWithId(idToUpdate, product_2, QUANTITY_1);
		stockRepository.update(toUpdate);
		assertThat(readAllStockFromDatabase()).containsExactlyInAnyOrder(stock_1, expectedResult);
	}

	@Test
	@DisplayName("Update Stock when stock does not exist should throw")
	void testUpdateStockWhenItDoesNotExistShouldThrow() {
		String missingId = getNewStringId();
		Stock missingStock = newStockWithId(missingId, product_1, QUANTITY_1);
		assertThatThrownBy(() -> stockRepository.update(missingStock)).isInstanceOf(NoSuchElementException.class)
				.hasMessage("Stock with id " + missingId + " not found.");

	}

	@Test
	@DisplayName("Method 'update' should be bound to the repository session")
	void testUpdateStockShouldBeBoundToTheRepositorySession() {
		String idToUpdate = getNewStringId();
		Stock toUpdate = newStockWithId(idToUpdate, product_1, QUANTITY_1);
		Stock notUpdated = newStockWithId(idToUpdate, product_1, QUANTITY_1);
		saveTestStockToDatabase(toUpdate);
		toUpdate.setQuantity(QUANTITY_2);
		session.startTransaction();
		stockRepository.update(toUpdate);
		assertThat(readAllStockFromDatabase()).containsExactly(notUpdated);
		session.commitTransaction();
	}

	@Test
	@DisplayName("Retrieve Stock by Product id")
	void testFindByProductId() {
		String productId = product_1.getId();
		Stock storedStock = newStockWithId(getNewStringId(), product_1, QUANTITY_1);
		saveTestStockToDatabase(storedStock);
		assertThat(stockRepository.findByProductId(productId)).isEqualTo(storedStock);
	}

	@Test
	@DisplayName("Method 'findByProductId' when not found should return null")
	void testFindByProductIdWhenNotFoundShouldReturnNull() {
		assertThat(stockRepository.findByProductId("missing_id")).isNull();
	}

	@Test
	@DisplayName("Method 'findByProductId' should be bound to the repository session")
	void testFindByProductIdShouldBeBoundToTheRepositorySession() {
		dropAndCreateCollection(productCollection);
		session.startTransaction();
		saveTestProductToDatabaseWithSession(session, product_1);
		String idToFind = getNewStringId();
		Stock stock_1 = newStockWithId(idToFind, product_1, QUANTITY_1);
		saveTestStockToDatabaseWithSession(session, stock_1);
		assertThat(stockRepository.findById(idToFind)).isEqualTo(stock_1);
		session.commitTransaction();
	}

	// Private utility methods

	private String getNewStringId() {
		return new ObjectId().toString();
	}

	private Stock newStockWithId(String id, Product product, int quantity) {
		Stock stock = new Stock(product, quantity);
		stock.setId(id);
		return stock;
	}

	private Document fromStockToDocument(Stock stockWithId) {
		return new Document().append("_id", new ObjectId(stockWithId.getId()))
				.append("product", stockWithId.getProduct().getId()).append("quantity", stockWithId.getQuantity());
	}

	private void saveTestStockToDatabase(Stock stockWithId) {
		stockCollection.insertOne(fromStockToDocument(stockWithId));
	}

	private void saveTestStockToDatabaseWithSession(ClientSession session, Stock stockWithId) {
		stockCollection.insertOne(session, fromStockToDocument(stockWithId));
	}

	private List<Stock> readAllStockFromDatabase() {
		return StreamSupport.stream(stockCollection.find().spliterator(), false).map(stockDocument -> {
			String productId = stockDocument.getString("product");
			Document productDocument = productCollection.find(eq("_id", new ObjectId(productId))).first();
			Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
			product.setId(productDocument.get("_id").toString());
			Stock stock = newStockWithId(stockDocument.get("_id").toString(), product,
					stockDocument.getInteger("quantity"));
			return stock;
		}).collect(Collectors.toList());
	}

	private Document fromProductToDocument(Product productWithoutId) {
		return new Document().append("name", productWithoutId.getName()).append("price", productWithoutId.getPrice());
	}

	private Product saveTestProductToDatabase(Product productWithoutId) {
		Document doc = fromProductToDocument(productWithoutId);
		productCollection.insertOne(doc);
		productWithoutId.setId(doc.get("_id").toString());
		return productWithoutId;
	}

	private Product saveTestProductToDatabaseWithSession(ClientSession session, Product productWithoutId) {
		Document doc = fromProductToDocument(productWithoutId);
		productCollection.insertOne(session, doc);
		productWithoutId.setId(doc.get("_id").toString());
		return productWithoutId;
	}

	private void dropAndCreateCollection(MongoCollection<Document> collection) {
		String name = collection.getNamespace().getCollectionName();
		collection.drop();
		database.createCollection(name);
	}

}
