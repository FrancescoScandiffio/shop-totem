package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for Stock")
class StockTest {
	
	private static final int POSITIVE_QUANTITY = 2;
	private static final int NEGATIVE_QUANTITY = -2;
	private static final int INITIAL_QUANTITY = 7;

	@Nested 
	@DisplayName("Constructor test") 
	class ConstructorTest {
		
		private Product product;
		
		@BeforeEach
		void setup() {
			product = new Product(1, "pasta", 2);
		}
		
		@Nested 
		@DisplayName("Happy cases") 
		class HappyCases {
			
			private Stock stock;
			
			@Test
			@DisplayName("Stock can be initialized with product non null and non negative quantity")
			void testConstructorWhenProductIsNotNullAndAvailableQuantityPositiveShouldBeAllowed() {
				stock = new Stock(product, POSITIVE_QUANTITY);
				
				assertThat(stock.getProduct()).isEqualTo(product);
				assertThat(stock.getAvailableQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
			
			@Test
			@DisplayName("Available quantity can be initialized to zero")
			void testConstructorWhenAvailableQuantityIsZeroShouldBeAllowed() {
				stock = new Stock(product, 0);
				
				assertThat(stock.getAvailableQuantity()).isZero();
			}
		}
		
		@Nested 
		@DisplayName("Error cases") 
		class ErrorCases {
			
			@Test
			@DisplayName("Product can't be null")
			void testConstructorWhenProductIsNullShouldThrow() {

				assertThatThrownBy(() -> new Stock(null, POSITIVE_QUANTITY)).isInstanceOf(NullPointerException.class)
						.hasMessage("Null product");
			}
			
			@Test
			@DisplayName("Available quantity can't be a negative number")
			void testConstructorWhenAvailableQuantityIsNegativeShouldThrow() {

				assertThatThrownBy(() -> new Stock(product, NEGATIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Negative available quantity: "+NEGATIVE_QUANTITY);
			}
		}
	}
	
	@Nested 
	@DisplayName("Methods tests") 
	class MethodsTest {
		
		private Stock stock;
		
		@BeforeEach
		void setup() {
			stock = new Stock();
			stock.initAvailableQuantity(INITIAL_QUANTITY);
		}
		
		@Nested 
		@DisplayName("setAvailableQuantity test") 
		class SetAvailableQuantityTest {
			
			@Test
			@DisplayName("Available quantity can't be set to negative number")
			void testSetAvailableQuantityWhenAvailableQuantityIsNegativeShouldThrow() {
				
				assertThatThrownBy(() -> stock.setAvailableQuantity(NEGATIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Negative available quantity: "+NEGATIVE_QUANTITY);
				
				assertThat(stock.getAvailableQuantity()).isEqualTo(INITIAL_QUANTITY);
			}
			
			@Test
			@DisplayName("Available quantity can be set to zero")
			void testSetAvailableQuantityWhenAvailableQuantityIsZeroShouldBeAllowed() {
					
				stock.setAvailableQuantity(0);
				assertThat(stock.getAvailableQuantity()).isZero();
			}
			
			@Test
			@DisplayName("Available quantity can be set to positive number")
			void testSetAvailableQuantityWhenAvailableQuantityIsPositiveShouldBeAllowed() {
				
				stock.setAvailableQuantity(POSITIVE_QUANTITY);
				assertThat(stock.getAvailableQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
		}
	}
}