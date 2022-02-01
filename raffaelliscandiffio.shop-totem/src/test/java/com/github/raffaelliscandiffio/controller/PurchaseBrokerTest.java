package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

	@InjectMocks
	private PurchaseBroker broker;

	private static final int QUANTITY = 3;
	private static final int GREATER_QUANTITY = 5;
	private static final double PRICE = 2;
	private static final String NAME = "Pasta";
	private static final long PRODUCT_ID = 1;

	@Test
	@DisplayName("'retrieveProducts' returns a list of products from the product repository")
	void testRetrieveProductsShouldReturnListOfProductsFromRepository() {
		List<Product> products = asList(new Product(PRODUCT_ID, NAME, PRICE));
		when(productRepository.findAll()).thenReturn(products);

		assertThat(broker.retrieveProducts()).isEqualTo(products);
	}

	@Nested
	@DisplayName("Test 'takeAvailable'")
	class TakeAvailableTest {
		@Test
		@DisplayName("should return 0 when Stock is not found")
		void testTakeAvailableShouldReturnZeroWhenStockIsNotFound() {
			when(stockRepository.findById(PRODUCT_ID)).thenReturn(null);

			assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isZero();
			verifyNoMoreInteractions(stockRepository);
		}

		@Test
		@DisplayName("should return requested quantity when more than requested is available and update the stock with residual quantity")
		void testTakeAvailableShouldReturnRequestedQuantityWhenMoreThanRequestedIsAvailableAndUpdateResidualQuantity() {
			Stock stock = spy(new Stock(PRODUCT_ID, GREATER_QUANTITY));
			when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);

			assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isEqualTo(QUANTITY);
			InOrder inOrder = inOrder(stock, stockRepository);
			inOrder.verify(stock).setQuantity(GREATER_QUANTITY - QUANTITY);
			inOrder.verify(stockRepository).update(stock);
		}

		@Test
		@DisplayName("should return requested quantity when only requested is available and update the stock with zero quantity")
		void testTakeAvailableShouldReturnRequestedQuantityWhenOnlyRequestedIsAvailableAndUpdateWithZeroQuantity() {
			Stock stock = spy(new Stock(PRODUCT_ID, QUANTITY));
			when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);

			assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isEqualTo(QUANTITY);
			InOrder inOrder = inOrder(stock, stockRepository);
			inOrder.verify(stock).setQuantity(0);
			inOrder.verify(stockRepository).update(stock);
		}

		@Test
		@DisplayName("should return available quantity when requested is more than available and update the stock with zero quantity")
		void testTakeAvailableShouldReturnAvailableQuantityWhenRequestedIsMoreThanAvailableAndUpdateWithZeroQuantity() {
			Stock stock = spy(new Stock(PRODUCT_ID, QUANTITY));
			when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);

			assertThat(broker.takeAvailable(PRODUCT_ID, GREATER_QUANTITY)).isEqualTo(QUANTITY);
			InOrder inOrder = inOrder(stock, stockRepository);
			inOrder.verify(stock).setQuantity(0);
			inOrder.verify(stockRepository).update(stock);
		}

		@Test
		@DisplayName("should return zero when quantity in stock is zero")
		void testTakeAvailableShouldReturnZeroWhenQuantityAvailableIsZero() {
			Stock stock = new Stock(PRODUCT_ID, 0);
			when(stockRepository.findById(PRODUCT_ID)).thenReturn(stock);

			assertThat(broker.takeAvailable(PRODUCT_ID, QUANTITY)).isZero();
			verifyNoMoreInteractions(stockRepository);
		}

	}

	@Nested
	@DisplayName("Test 'doesProductExist'")
	class DoesProductExistTest {
		@Test
		@DisplayName("should return False when product is not found on repository")
		void testDoesProductExistShouldReturnFalseWhenProductIsNotFound() {

			when(productRepository.findById(PRODUCT_ID)).thenReturn(null);

			assertThat(broker.doesProductExist(PRODUCT_ID)).isFalse();
		}

		@Test
		@DisplayName("should return True when product is found on repository")
		void testDoesProductExistShouldReturnTrueWhenProductIsFound() {

			when(productRepository.findById(PRODUCT_ID)).thenReturn(new Product(PRODUCT_ID, NAME, PRICE));

			assertThat(broker.doesProductExist(PRODUCT_ID)).isTrue();
		}
	}

	@Nested
	@DisplayName("Test 'saveNewProductInStock'")
	class SaveNewProductInStockTest {

		@Test
		@DisplayName("should 'save' new product and stock in repositories when id, name, price, quantity are valid")
		void testSaveNewProductInStockWhenIdNamePriceQuantityAreValidShouldSaveProductAndStockInRepositories() {
			broker.saveNewProductInStock(PRODUCT_ID, NAME, PRICE, QUANTITY);

			verify(productRepository).save(new Product(PRODUCT_ID, NAME, PRICE));
			verify(stockRepository).save(new Stock(PRODUCT_ID, QUANTITY));
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { " ", "\t", "\n" })
		@DisplayName("should throw when name is null or empty")
		void testSaveNewProductInStockWhenNameIsNotValidShouldThrow(String name) {
			assertThatThrownBy(() -> broker.saveNewProductInStock(PRODUCT_ID, name, PRICE, QUANTITY))
					.isInstanceOf(IllegalArgumentException.class).hasMessage("Null or empty name is not allowed");
			verifyNoMoreInteractions(stockRepository, productRepository);
		}

		@Test
		@DisplayName("allows price to be zero")
		void testSaveNewProductInStockWhenPriceIsZeroShouldBeAllowed() {

			broker.saveNewProductInStock(PRODUCT_ID, NAME, 0, QUANTITY);
			verify(productRepository).save(new Product(PRODUCT_ID, NAME, 0));
			verify(stockRepository).save(new Stock(PRODUCT_ID, QUANTITY));
		}

		@Test
		@DisplayName("throws when price is a negative number")
		void testSaveNewProductInStockWhenPriceIsNegativeShouldThrow() {

			assertThatThrownBy(() -> broker.saveNewProductInStock(PRODUCT_ID, NAME, -PRICE, QUANTITY))
					.isInstanceOf(IllegalArgumentException.class).hasMessage("Negative price: " + -PRICE);
			verifyNoMoreInteractions(stockRepository, productRepository);
		}

		@Test
		@DisplayName("throws when quantity is a negative number")
		void testSaveNewProductInStockWhenQuantityIsNegativeShouldThrow() {

			assertThatThrownBy(() -> broker.saveNewProductInStock(PRODUCT_ID, NAME, PRICE, -QUANTITY))
					.isInstanceOf(IllegalArgumentException.class).hasMessage("Negative quantity: " + -QUANTITY);
			verifyNoMoreInteractions(stockRepository, productRepository);
		}

		@Test
		@DisplayName("allows quantity to be zero")
		void testSaveNewProductInStockWhenQuantityIsZeroShouldBeAllowed() {
			broker.saveNewProductInStock(PRODUCT_ID, NAME, PRICE, 0);
			verify(productRepository).save(new Product(PRODUCT_ID, NAME, PRICE));
			verify(stockRepository).save(new Stock(PRODUCT_ID, 0));
		}
		
		@Test
		@DisplayName("throws when product with id is already in database")
		void testSaveNewProductInStockWhenProductIsAlreadyExistingShouldThrow() {
			when(productRepository.findById(PRODUCT_ID)).thenReturn(new Product(PRODUCT_ID, NAME, QUANTITY));
			
			assertThatThrownBy(() -> broker.saveNewProductInStock(PRODUCT_ID, NAME, PRICE, QUANTITY))
					.isInstanceOf(IllegalArgumentException.class).hasMessage("Product with id "+ PRODUCT_ID +" already in database");
			verifyNoMoreInteractions(stockRepository, productRepository);
		}
	}
}
