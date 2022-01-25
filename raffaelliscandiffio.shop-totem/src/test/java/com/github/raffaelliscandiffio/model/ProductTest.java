package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


@DisplayName("Tests for Product")
class ProductTest {

	private static final double POSITIVE_PRICE = 3;
	private static final String NAME = "pasta";
	private static final long ID = 1;

	@Nested
	@DisplayName("Happy case")
	class HappyCase {

		private Product product;

		@Test
		@DisplayName("Product initialization with valid arguments")
		void testConstructorWhenNameNotNullOrEmptyAndPriceIsPositiveShouldBeAllowed() {

			product = new Product(ID, NAME, POSITIVE_PRICE);
			assertThat(product.getId()).isEqualTo(ID);
			assertThat(product.getName()).isEqualTo(NAME);
			assertThat(product.getPrice()).isEqualTo(POSITIVE_PRICE);
		}
	}
}