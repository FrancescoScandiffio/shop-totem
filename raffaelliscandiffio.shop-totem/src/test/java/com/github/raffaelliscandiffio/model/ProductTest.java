package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for Product")
class ProductTest {
	
	private static final int ZERO = 0;
	private static final int POSITIVE_QUANTITY = 2;
	private static final int NEGATIVE_QUANTITY = -2;
	private static final double POSITIVE_PRICE = 3;
	private static final double NEGATIVE_PRICE = -3;
	private static final int INITIAL_QUANTITY = 7;
	private static final String NAME = "name";

	
	@Nested 
	@DisplayName("Constructor tests") 
	class ConstructorTests {
		
		@Nested 
		@DisplayName("Happy cases") 
		class HappyCases {
			
			
			private Product product;

			@Test
			@DisplayName("Product can be initialized with name not null nor empty, non negative price and quantity")
			void testConstructorWhenNameNotNullOrEmptyPricePositiveAvailableQuantityPositiveShouldBeAllowed() {
				
				product = new Product(NAME, POSITIVE_PRICE, POSITIVE_QUANTITY);
				assertThat(product.getName()).isEqualTo(NAME);
				assertThat(product.getPrice()).isEqualTo(POSITIVE_PRICE);
				assertThat(product.getAvailableQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
			
			@Test
			@DisplayName("Price can be initialized to zero")
			void testConstructorWhenPriceIsZeroShouldBeAllowed() {
				
				product = new Product(NAME, ZERO, POSITIVE_QUANTITY);
				assertThat(product.getPrice()).isZero();
			}
			
			
			@Test
			@DisplayName("Available quantity can be initialized to zero")
			void testConstructorWhenAvailableQuantityIsZeroShouldBeAllowed() {
				
				product = new Product("name", POSITIVE_PRICE, ZERO);
				assertThat(product.getAvailableQuantity()).isZero();
			}
			
		}
		
		@Nested 
		@DisplayName("Error cases") 
		class ErrorCases {
			
			
			@ParameterizedTest
			@NullAndEmptySource
			@ValueSource(strings = { " ", "\t", "\n" })
			@DisplayName("Name can't be set as null or empty string")
			void testConstructorWhenNameNullOrEmptyShouldThrow(String name) {
				assertThatThrownBy(() -> new Product(name, POSITIVE_PRICE, POSITIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Null or empty name is not allowed");
			}
			
			@Test
			@DisplayName("Price can't be set to negative number")
			void testConstructorWhenPriceIsNegativeShouldThrow() {

				assertThatThrownBy(() -> new Product(NAME, NEGATIVE_PRICE, POSITIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Negative price: "+NEGATIVE_PRICE);
			}
			
			@Test
			@DisplayName("Available quantity can't be set to negative number")
			void testConstructorWhenAvailableQuantityIsNegativeShouldThrow() {

				assertThatThrownBy(() -> new Product(NAME, POSITIVE_PRICE, NEGATIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Negative available quantity: "+NEGATIVE_QUANTITY);
			}
		}
	}
	
	@Nested 
	@DisplayName("Methods tests") 
	class MethodsTests {
		
		private Product product;
		
		@BeforeEach
		void setup() {
			product = new Product();
			product.initAvailableQuantity(INITIAL_QUANTITY);
		}
		
		@Nested 
		@DisplayName("setAvailableQuantity tests") 
		class SetAvailableQuantityTests {
			
			@Test
			@DisplayName("Available quantity can't be set to negative number")
			void testSetAvailableQuantityWhenAvailableQuantityIsNegativeShouldThrow() {
				
				assertThatThrownBy(() -> product.setAvailableQuantity(NEGATIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Negative available quantity: "+NEGATIVE_QUANTITY);
				
				assertThat(product.getAvailableQuantity()).isEqualTo(INITIAL_QUANTITY);
			}
			
			@Test
			@DisplayName("Available quantity can be set to zero")
			void testSetAvailableQuantityWhenAvailableQuantityIsZeroShouldBeAllowed() {
					
				product.setAvailableQuantity(ZERO);
				assertThat(product.getAvailableQuantity()).isZero();
			}
			
			@Test
			@DisplayName("Available quantity can be set to positive number")
			void testSetAvailableQuantityWhenAvailableQuantityIsPositiveShouldBeAllowed() {
				
				product.setAvailableQuantity(POSITIVE_QUANTITY);
				assertThat(product.getAvailableQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
		}
	}
}