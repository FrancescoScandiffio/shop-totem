package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Tests for Product")
class ProductTest {

	private static final double POSITIVE_PRICE = 3;
	private static final double NEGATIVE_PRICE = -3;
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

		@Test
		@DisplayName("Price can be initialized to zero")
		void testConstructorWhenPriceIsZeroShouldBeAllowed() {

			product = new Product(ID, NAME, 0);
			assertThat(product.getPrice()).isZero();
		}
	}

	@Nested
	@DisplayName("Exceptional cases")
	class ExceptionalCases {

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = { " ", "\t", "\n" })
		@DisplayName("Name can't be set as null or empty string")
		void testConstructorWhenNameNullOrEmptyShouldThrow(String name) {
			assertThatThrownBy(() -> new Product(ID, name, POSITIVE_PRICE)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Null or empty name is not allowed");
		}

		@Test
		@DisplayName("Price can't be set to negative number")
		void testConstructorWhenPriceIsNegativeShouldThrow() {

			assertThatThrownBy(() -> new Product(ID, NAME, NEGATIVE_PRICE)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Negative price: " + NEGATIVE_PRICE);
		}
	}

}