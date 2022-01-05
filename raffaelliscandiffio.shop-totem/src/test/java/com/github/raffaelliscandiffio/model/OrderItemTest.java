package com.github.raffaelliscandiffio.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tests for OrderItem")
class OrderItemTest {

	private static final int ZERO = 0;
	private static final int NEGATIVE_QUANTITY = -5;
	private static final int POSITIVE_QUANTITY = 3;
	private static final int GREATER_POSITIVE_QUANTITY = 25;

	@Nested
	@DisplayName("Test constructor")
	class ConstructorTests {

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCases {

			@Test
			@DisplayName("New OrderItem with null Product")
			void testConstructorWhenProductIsNullShouldThrow() {
				assertThatThrownBy(() -> new OrderItem(null, POSITIVE_QUANTITY))
						.isInstanceOf(NullPointerException.class)
						.hasMessage("Null product");
			}

			@Test
			@DisplayName("New OrderItem with zero quantity")
			void testConstructorWhenQuantityIsZeroShouldThrow() {
				Product product = creasteTestProduct();
				
				assertThatThrownBy(() -> new OrderItem(product, ZERO))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", ZERO));
			}

			@Test
			@DisplayName("New OrderItem with negative quantity")
			void testConstructorWhenQuantityIsNegativeShouldThrow() {
				Product product = creasteTestProduct();

				assertThatThrownBy(() -> new OrderItem(product, NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", NEGATIVE_QUANTITY));
			}
		}

		@Nested
		@DisplayName("Happy case")
		class HappyCase {

			@Test
			@DisplayName("New OrderItem")
			void testConstructorWhenProductIsNotNullAndQuantityIsPositiveShouldInitialise() {
				Product product = creasteTestProduct();
				
				OrderItem item = new OrderItem(product, POSITIVE_QUANTITY);
				assertThat(item.getProduct()).isEqualTo(product);
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
		}

		/**
		 * Utility method for creating a Product for testing.
		 */
		private Product creasteTestProduct() {
			return new Product("name", 1, 1);
		}
	}

	@Nested
	@DisplayName("Test 'increaseQuantity' method")
	class IncreaseQuantityTest {

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCases {

			@Test
			@DisplayName("Don't increase quantity if given value is negative")
			void testIncreaseQuantityWhenQuantityIsNegativeShouldThrow() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);
				
				assertThatThrownBy(() -> item.increaseQuantity(NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", NEGATIVE_QUANTITY));
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}

			@Test
			@DisplayName("Don't increase quantity if given value is zero")
			void testIncreaseQuantityWhenQuantityIsZeroShouldThrow() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);

				assertThatThrownBy(() -> item.increaseQuantity(ZERO)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", ZERO));
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}

		}

		@Nested
		@DisplayName("Happy case")
		class HappyCase {
			
			@Test
			@DisplayName("Increase quantity")
			void testIncreaseQuantity() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);

				item.increaseQuantity(GREATER_POSITIVE_QUANTITY);
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY + GREATER_POSITIVE_QUANTITY);
			}
		}
	}

	@Nested
	@DisplayName("Test 'decreaseQuantity' method")
	class DecreaseQuantityTest {

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCases {
			
			@Test
			@DisplayName("Don't decrease quantity if given value is negative")
			void testDecreaseQuantityWhenQuantityIsNegativeShouldThrow() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);

				assertThatThrownBy(() -> item.decreaseQuantity(NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", NEGATIVE_QUANTITY));
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);

			}

			@Test
			@DisplayName("Don't decrease quantity if given value is zero")
			void testDecreaseQuantityWhenQuantityIsZeroShouldThrow() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);

				assertThatThrownBy(() -> item.decreaseQuantity(ZERO)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", ZERO));
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}

			@Test
			@DisplayName("Don't decrease quantity if given value is greater than available")
			void testDecreaseQuantityWhenNotEnoughAvailableShouldThrow() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);

				assertThatThrownBy(() -> item.decreaseQuantity(GREATER_POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Decrease quantity (%d) must be less than available quantity (%d)",
								GREATER_POSITIVE_QUANTITY, POSITIVE_QUANTITY));
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}

			@Test
			@DisplayName("Don't decrease quantity if given value is equal to available")
			void testDecreaseQuantityWhenEqualToAvailableShouldThrow() {
				OrderItem item = new OrderItem(POSITIVE_QUANTITY);

				assertThatThrownBy(() -> item.decreaseQuantity(POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Decrease quantity (%d) must be less than available quantity (%d)",
								POSITIVE_QUANTITY, POSITIVE_QUANTITY));
				assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			}
		}

		@Nested
		@DisplayName("Happy case")
		class HappyCase {

			@Test
			@DisplayName("Decrease quantity")
			void testDecreaseQuantity() {
				OrderItem item = new OrderItem(GREATER_POSITIVE_QUANTITY);

				item.decreaseQuantity(POSITIVE_QUANTITY);
				assertThat(item.getQuantity()).isEqualTo(GREATER_POSITIVE_QUANTITY - POSITIVE_QUANTITY);
			}
		}
	}

}
