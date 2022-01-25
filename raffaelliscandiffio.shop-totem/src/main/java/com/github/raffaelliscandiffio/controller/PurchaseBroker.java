package com.github.raffaelliscandiffio.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;


public class PurchaseBroker {
	
	private static final Logger LOGGER = LogManager.getLogger(PurchaseBroker.class);
	private ProductRepository productRepository;
	private StockRepository stockRepository;
	

	public PurchaseBroker(ProductRepository productRepository, StockRepository stockRepository) {
		this.productRepository = productRepository;
		this.stockRepository = stockRepository;
	}
	
	/**
	 * Constructs a new Product with the specified id, name and price and a Stock with the same id and quantity.
	 * @param id of the product and stock
	 * @param name of the product
	 * @param price of the product
	 * @param quantity of the product in stock
	 * @throws IllegalArgumentException if the specified name is empty or null, price is negative, quantity is negative
	 */
	public void saveNewProductInStock(long id, String name, double price, int quantity) {
		if (!(name != null && !name.trim().isEmpty())) {
			throw new IllegalArgumentException("Null or empty name is not allowed");
		}
		if (price < 0) {
			throw new IllegalArgumentException("Negative price: " + price);
		}
		if (quantity < 0) {
			throw new IllegalArgumentException("Negative quantity: " + quantity);
		}
		
		productRepository.save(new Product(id, name, price));
		stockRepository.save(new Stock(id, quantity));
	}

	public List<Product> retrieveProducts() {
		return productRepository.findAll();
	}

	public int takeAvailable(long productId, int quantity) {
		Stock stock;
		try {
			stock = stockRepository.findById(productId);
		}catch(NoSuchElementException e){
			LOGGER.log(Level.ERROR, String.format("Stock with id %d not found %n%s %n", productId, getReducedStackTrace(e)));
			return 0;
		}
		int stockAvailableQuantity = stock.getQuantity();
		
		if(stockAvailableQuantity == 0) {
			return 0;
		}else {
			int returnedQuantity =  Math.max(0, stockAvailableQuantity-quantity);
			stock.setQuantity(returnedQuantity);
			stockRepository.save(stock);
			if(returnedQuantity == 0) {
				return stockAvailableQuantity;
			}else {
				return quantity;
			}
		}
	}

	public void returnProduct(long productId, int quantity) {
		// TODO Auto-generated method stub
	}

	public boolean doesProductExist(long productId) {
		try {
			productRepository.findById(productId);
			return true;
		}catch(NoSuchElementException e){
			LOGGER.log(Level.ERROR, String.format("Product with id %d not found %n%s %n", productId, getReducedStackTrace(e)));
			return false;
		}
	}
	
	private String getReducedStackTrace(Exception e) {
		StackTraceElement el = e.getStackTrace()[0];
		return String.format("at %s.%s() - line %s", el.getClassName(), el.getMethodName(), el.getLineNumber());
	}

}
