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
	private static final long ID=3;

	@Nested 
	@DisplayName("Test 'constructor'") 
	class ConstructorTest {
		
		
		@Nested 
		@DisplayName("Happy case") 
		class HappyCase {
			
			private Stock stock;
			
			@Test
			@DisplayName("Stock is initialized with ID and non negative quantity")
			void testConstructorWithIdAndNonNegativeQuantity() {
				stock = new Stock(ID, POSITIVE_QUANTITY);
				
				assertThat(stock.getId()).isEqualTo(ID);
				assertThat(stock.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
		}
	}
	
	@Nested 
	@DisplayName("Test 'setAvailableQuantity'") 
	class SetAvailableQuantityTest {
		
		private Stock stock;
		
		@BeforeEach
		void setup() {
			stock = new Stock();
			stock.initQuantity(INITIAL_QUANTITY);
		}
		
		@Test
		@DisplayName("Available quantity can't be set to negative number")
		void testSetAvailableQuantityWhenAvailableQuantityIsNegativeShouldThrow() {
			
			assertThatThrownBy(() -> stock.setQuantity(NEGATIVE_QUANTITY)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Negative quantity: "+NEGATIVE_QUANTITY);
			
			assertThat(stock.getQuantity()).isEqualTo(INITIAL_QUANTITY);
		}
		
		@Test
		@DisplayName("Available quantity can be set to zero")
		void testSetAvailableQuantityWhenAvailableQuantityIsZeroShouldBeAllowed() {
				
			stock.setQuantity(0);
			assertThat(stock.getQuantity()).isZero();
		}
		
		@Test
		@DisplayName("Available quantity can be set to positive number")
		void testSetAvailableQuantityWhenAvailableQuantityIsPositiveShouldBeAllowed() {
			
			stock.setQuantity(POSITIVE_QUANTITY);
			assertThat(stock.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
		}
	}
}