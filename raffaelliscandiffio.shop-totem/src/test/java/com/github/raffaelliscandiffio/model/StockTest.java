package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for Stock")
class StockTest {
	
	private static final int POSITIVE_QUANTITY = 2;
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
	
}