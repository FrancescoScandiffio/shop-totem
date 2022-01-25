package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
	@DisplayName("'retrieveProducts' returns a list of products from the product repository")
	void testRetrieveProductsShouldReturnListOfProductsFromRepository() {
		List<Product> products = asList(new Product(PRODUCT_ID, "Pasta", 3));
		when(productRepository.findAll())
			.thenReturn(products);
		
		assertThat(broker.retrieveProducts()).isEqualTo(products);
	}
	

	@Test
	@DisplayName("'takeAvailable' should return 0 when Stock is not found")
	void testTakeAvailableShouldReturnZeroWhenStockIsNotFound() {
		when(stockRepository.findById(PRODUCT_ID)).thenThrow(new NoSuchElementException("Stock with id "+ PRODUCT_ID+ " is not found"));
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isZero();
	}
	
	@Test
	@DisplayName("'takeAvailable' should return requested quantity when more than requested is available and save the stock")
	void testTakeAvailableShouldReturnRequestedQuantityWhenMoreThanRequestedIsAvailableAndSave() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(GREATER_QUANTITY);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isEqualTo(QUANTITY);
		InOrder inOrder = inOrder(stock, stockRepository);
		inOrder.verify(stock).setAvailableQuantity(GREATER_QUANTITY-QUANTITY);
		inOrder.verify(stockRepository).save(stock);
	}
	
	@Test
	@DisplayName("'takeAvailable' should return requested quantity when only requested is available and save the stock")
	void testTakeAvailableShouldReturnRequestedQuantityWhenOnlyRequestedIsAvailableAndSave() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(QUANTITY);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isEqualTo(QUANTITY);
		InOrder inOrder = inOrder(stock, stockRepository);
		inOrder.verify(stock).setAvailableQuantity(0);
		inOrder.verify(stockRepository).save(stock);
	}
	
	@Test
	@DisplayName("'takeAvailable' should return available quantity when requested is more than available and save the stock")
	void testTakeAvailableShouldReturnAvailableQuantityWhenRequestedIsMoreThanAvailableAndSave() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(QUANTITY);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, GREATER_QUANTITY)).isEqualTo(QUANTITY);
		InOrder inOrder = inOrder(stock, stockRepository);
		inOrder.verify(stock).setAvailableQuantity(0);
		inOrder.verify(stockRepository).save(stock);
	}
	
	@Test
	@DisplayName("'takeAvailable' should return zero when quantity in stock is zero")
	void testTakeAvailableShouldReturnZeroWhenQuantityAvailableIsZero() {

		when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);
		when(stock.getAvailableQuantity()).thenReturn(0);
		
		assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isZero();
		verifyNoMoreInteractions(stockRepository);
	}
	
	@Test
	@DisplayName("'doesProductExist' should return False when product is not found on repository")
	void testDoesProductExistShouldReturnFalseWhenProductIsNotFound() {
		
		when(productRepository.findById(PRODUCT_ID)).thenThrow(new NoSuchElementException("Product with id "+ PRODUCT_ID+ " is not found"));
		
		assertThat(broker.doesProductExist(PRODUCT_ID)).isFalse();
	}
	
	@Test
	@DisplayName("'doesProductExist' should return True when product is found on repository")
	void testDoesProductExistShouldReturnTrueWhenProductIsFound() {
		
		when(productRepository.findById(PRODUCT_ID)).thenReturn(new Product(PRODUCT_ID, "Pasta", 3));
		
		assertThat(broker.doesProductExist(PRODUCT_ID)).isTrue();
	}
	
	@Test
	@DisplayName("'saveNewProductInStock' should 'save' in product and stock repositories when name, price, quantity are valid")
	void testSaveNewProductInStockWhenNamePriceQuantityAreValidShouldSaveInProductAndStockRepositories() {
		
		broker.saveNewProductInStock(PRODUCT_ID, "Pasta", 3, 100);
		
		verify(productRepository).save(new Product(PRODUCT_ID, "Pasta", 3));
		verify(stockRepository).save(new Stock(PRODUCT_ID, 100));
	}
	
	
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	@DisplayName("'saveNewProductInStock' should throw when name is null or empty")
	void testSaveNewProductInStockWhenNameIsNotValidShouldThrow(String name) {
		assertThatThrownBy(() -> broker.saveNewProductInStock(PRODUCT_ID, name, 3, 100)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Null or empty name is not allowed");
		verifyNoMoreInteractions(stockRepository, productRepository);
	}
	
	@Test
	@DisplayName("'saveNewProductInStock' allows price to be zero")
	void testSaveNewProductInStockWhenPriceIsZeroShouldBeAllowed() {

		broker.saveNewProductInStock(PRODUCT_ID, "Pasta", 0, 100);
		verify(productRepository).save(new Product(PRODUCT_ID, "Pasta", 0));
		verify(stockRepository).save(new Stock(PRODUCT_ID, 100));
	}
	
	@Test
	@DisplayName("'saveNewProductInStock' throws when price is a negative number")
	void testSaveNewProductInStockWhenPriceIsNegativeShouldThrow() {

		assertThatThrownBy(() ->  broker.saveNewProductInStock(PRODUCT_ID, "Pasta", -2, 100)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Negative price: -2.0");
	}
	
	
}
