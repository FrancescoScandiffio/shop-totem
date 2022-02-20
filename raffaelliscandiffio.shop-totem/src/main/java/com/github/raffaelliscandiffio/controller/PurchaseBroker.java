package com.github.raffaelliscandiffio.controller;

import java.util.List;

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
	 * Constructs a new Product with the specified id, name and price and a Stock
	 * with the same id and quantity.
	 * 
	 * @param id       of the product and stock
	 * @param name     of the product
	 * @param price    of the product
	 * @param quantity of the product in stock
	 * @throws IllegalArgumentException if the specified name is empty or null,
	 *                                  price is negative, quantity is negative, id
	 *                                  is already in database
	 */
	public void saveNewProductInStock(long id, String name, double price, int quantity)
			throws IllegalArgumentException {
		if (!(name != null && !name.trim().isEmpty())) {
			throw new IllegalArgumentException("Null or empty name is not allowed");
		}
		if (price < 0) {
			throw new IllegalArgumentException("Negative price: " + price);
		}
		if (quantity < 0) {
			throw new IllegalArgumentException("Negative quantity: " + quantity);
		}
		if (productRepository.findById(id) != null) {
			throw new IllegalArgumentException("Product with id " + id + " already in database");
		}
		if (stockRepository.findById(id) != null) {
			throw new IllegalArgumentException("Stock with id " + id + " already in database");
		}

		productRepository.save(new Product(id, name, price));
		stockRepository.save(new Stock(id, quantity));
		LOGGER.log(Level.INFO, "New product with ID: {}, name: {}, price: {}", id, name, price);
		LOGGER.log(Level.INFO, "New stock with ID: {}, quantity: {}", id, quantity);
	}

	public List<Product> retrieveProducts() {
		return productRepository.findAll();
	}

	/**
	 * Fetches the stock with given id and saves it back after subtracting the given
	 * quantity. If current quantity in stock is not sufficient only the available
	 * quantity is taken.
	 * 
	 * @param id       of the product/stock
	 * @param quantity positive of the product that needs to be subtracted from
	 *                 current stock quantity
	 * @return the quantity available given the requested, in the following
	 *         conditions:
	 *         <ul>
	 *         <li>the whole quantity requested if available in stock</li>
	 *         <li>the whole quantity available if the requested was not entirely in
	 *         stock</li>
	 *         <li>zero if the product is out of stock</li>
	 *          <li> -1 if the product is not found</li>
	 *         </ul>
	 */
	public int takeAvailable(long id, int quantity) {
		Stock stock = stockRepository.findById(id);
		if (stock == null) {
			LOGGER.log(Level.ERROR, "Stock with id {} not found", id);
			return -1;
		}
		int stockAvailableQuantity = stock.getQuantity();
		if (stockAvailableQuantity == 0) {
			return 0;
		} else {
			int returnedQuantity = Math.max(0, stockAvailableQuantity - quantity);
			stock.setQuantity(returnedQuantity);
			stockRepository.update(stock);
			if (returnedQuantity == 0) {
				return stockAvailableQuantity;
			} else {
				return quantity;
			}
		}
	}

	public void returnProduct(long id, int quantity) {
		Stock stock = stockRepository.findById(id);
		if (stock == null) {
			LOGGER.log(Level.ERROR, "Stock with id {} not found", id);
			return;
		}
		stock.setQuantity(stock.getQuantity() + quantity);
		stockRepository.update(stock);
	}

	public boolean doesProductExist(long id) {
		if (productRepository.findById(id) != null) {
			return true;
		}
		LOGGER.log(Level.ERROR, "Product with id {} not found", id);
		return false;
	}

}
