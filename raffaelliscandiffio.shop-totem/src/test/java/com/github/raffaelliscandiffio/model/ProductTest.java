package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for Product")
class ProductTest {

	private static final int ZERO = 0;
	private static final double POSITIVE_PRICE = 3;
	private static final double NEGATIVE_PRICE = -3;
	private static final String NAME = "name";

	@Nested
	@DisplayName("Happy cases")
	class HappyCases {

		private Product product;

		@Test
		@DisplayName("An incremental id is automatically generated")
		void testIdsAreIncremental() {
			assertThat(new Product(NAME, POSITIVE_PRICE).getId()).isLessThan(new Product(NAME, POSITIVE_PRICE).getId());
		}

		@Test
		@DisplayName("A positive number is automatically assigned as id")
		void testIdIsAutomaticallyAssignedAsPositiveNumber() {
			product = new Product(NAME, POSITIVE_PRICE);
			assertThat(product.getId()).isPositive();
		}

		@Test
		@DisplayName("Product can be initialized with name not null nor empty and non negative price")
		void testConstructorWhenNameNotNullOrEmptyPricePositiveShouldBeAllowed() {

			product = new Product(NAME, POSITIVE_PRICE);
			assertThat(product.getName()).isEqualTo(NAME);
			assertThat(product.getPrice()).isEqualTo(POSITIVE_PRICE);
		}

		@Test
		@DisplayName("Price can be initialized to zero")
		void testConstructorWhenPriceIsZeroShouldBeAllowed() {

			product = new Product(NAME, ZERO);
			assertThat(product.getPrice()).isZero();
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
			assertThatThrownBy(() -> new Product(name, POSITIVE_PRICE)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Null or empty name is not allowed");
		}

		@Test
		@DisplayName("Price can't be set to negative number")
		void testConstructorWhenPriceIsNegativeShouldThrow() {

			assertThatThrownBy(() -> new Product(NAME, NEGATIVE_PRICE)).isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Negative price: " + NEGATIVE_PRICE);
		}
	}

}