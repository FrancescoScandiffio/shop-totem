package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseBrokerTest {
	
	@Mock
	private ProductRepository productRepository;
	
	@Mock
	private StockRepository stockRepository;
	
	@Mock
	private Stock stock;
	
	@InjectMocks
	private PurchaseBroker broker;
	
	private static final int QUANTITY = 3;
	private static final int GREATER_QUANTITY = 5;
	private static final long PRODUCT_ID=1;

	@Test
	@DisplayName("RetrieveProducts returns a list of products from the product repository")
	void testRetrieveProductsShouldReturnListOfProductsFromRepository() {
		List<Product> products = asList(new Product(PRODUCT_ID, "Pasta", 3));
		when(productRepository.findAll())
			.thenReturn(products);
		
		assertThat(broker.retrieveProducts()).isEqualTo(products);
	}
	

	@Test
	@DisplayName("TakeAvailable should return 0 when Stock is not found")
	void testTakeAvailableShouldReturnZeroWhenStockIsNotFound() {
		when(stockRepository.findById(PRODUCT_ID)).thenThrow(new NoSuchElementException("Stock with id "+ PRODUCT_ID+ " is not found"));
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isZero();
	}
	
	@Test
	@DisplayName("TakeAvailable should return requested quantity when more than requested is available and save the stock")
	void testTakeAvailableShouldReturnRequestedQuantityWhenMoreThanRequestedIsAvailableAndSave() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(GREATER_QUANTITY);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isEqualTo(QUANTITY);
		InOrder inOrder = inOrder(stock, stockRepository);
		inOrder.verify(stock).setAvailableQuantity(GREATER_QUANTITY-QUANTITY);
		inOrder.verify(stockRepository).save(stock);
	}
	
	@Test
	@DisplayName("TakeAvailable should return requested quantity when only requested is available and save the stock")
	void testTakeAvailableShouldReturnRequestedQuantityWhenOnlyRequestedIsAvailableAndSave() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(QUANTITY);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isEqualTo(QUANTITY);
		InOrder inOrder = inOrder(stock, stockRepository);
		inOrder.verify(stock).setAvailableQuantity(0);
		inOrder.verify(stockRepository).save(stock);
	}
	
	@Test
	@DisplayName("TakeAvailable should return available quantity when requested is more than available and save the stock")
	void testTakeAvailableShouldReturnAvailableQuantityWhenRequestedIsMoreThanAvailableAndSave() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(QUANTITY);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, GREATER_QUANTITY)).isEqualTo(QUANTITY);
		InOrder inOrder = inOrder(stock, stockRepository);
		inOrder.verify(stock).setAvailableQuantity(0);
		inOrder.verify(stockRepository).save(stock);
	}
	
	@Test
	@DisplayName("TakeAvailable should return zero when quantity in stock is zero")
	void testTakeAvailableShouldReturnZeroWhenQuantityAvailableIsZero() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(0);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isZero();
		verifyNoMoreInteractions(stockRepository);
	}
	
	@Test
	@DisplayName("DoesProductExist should return False when product is not found on repository")
	void testDoesProductExistShouldReturnFalseWhenProductIsNotFound() {
		
		when(productRepository.findById(PRODUCT_ID)).thenThrow(new NoSuchElementException("Product with id "+ PRODUCT_ID+ " is not found"));
		
		assertThat(broker.doesProductExist(PRODUCT_ID)).isFalse();
	}
	
	@Test
	@DisplayName("DoesProductExist should return True when product is found on repository")
	void testDoesProductExistShouldReturnTrueWhenProductIsFound() {
		
		when(productRepository.findById(PRODUCT_ID)).thenReturn(new Product(PRODUCT_ID, "Pasta", 3));
		
		assertThat(broker.doesProductExist(PRODUCT_ID)).isTrue();
	}
	
}
