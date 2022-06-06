package com.github.raffaelliscandiffio.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class ProductMongoRepository implements ProductRepository {

	private static final String FIELD_ID = "_id";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_PRICE = "price";

	private ClientSession session;
	private MongoCollection<Document> productCollection;

	public ProductMongoRepository(MongoClient client, ClientSession session, String databaseName,
			String collectionName) {
		productCollection = client.getDatabase(databaseName).getCollection(collectionName);
		this.session = session;
	}

	@Override
	public void save(Product product) {
		Document productDocument = new Document().append(FIELD_NAME, product.getName()).append(FIELD_PRICE,
				product.getPrice());
		productCollection.insertOne(session, productDocument);
		product.setId(productDocument.get(FIELD_ID).toString());

	}

	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find(session).spliterator(), false)
				.map(this::fromDocumentToProduct).collect(Collectors.toList());
	}

	@Override
	public Product findById(String id) {
		Document d = productCollection.find(session, Filters.eq(FIELD_ID, new ObjectId(id))).first();
		if (d != null)
			return fromDocumentToProduct(d);
		else
			return null;
	}

	private Product fromDocumentToProduct(Document d) {
		Product p = new Product(d.getString(FIELD_NAME), d.getDouble(FIELD_PRICE));
		p.setId(d.get(FIELD_ID).toString());
		return p;
	}
}