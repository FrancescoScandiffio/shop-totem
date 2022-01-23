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

	public List<Product> retrieveProducts() {
		return productRepository.findAll();
	}

	public int takeAvailable(long productId, int quantity) {
		Stock stock;
		try {
			stock = stockRepository.findById(productId);
		}catch(NoSuchElementException e){
			LOGGER.log(Level.ERROR, String.format("Stock with id %d not found", productId), e);
			return 0;
		}
		int stockAvailableQuantity = stock.getAvailableQuantity();
		
		if(stockAvailableQuantity == 0) {
			return 0;
		}else {
			int returnedQuantity =  Math.max(0, stockAvailableQuantity-quantity);
			stock.setAvailableQuantity(returnedQuantity);
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
			LOGGER.log(Level.WARN, String.format("Product with id %d not found", productId), e);
			return false;
		}
	}

}
