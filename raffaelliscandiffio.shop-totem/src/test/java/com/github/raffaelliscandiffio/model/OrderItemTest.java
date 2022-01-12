package com.github.raffaelliscandiffio.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.byLessThan;

import org.assertj.core.api.SoftAssertions;

@DisplayName("Tests for OrderItem")
class OrderItemTest {

	private static final int ZERO = 0;
	private static final int NEGATIVE_QUANTITY = -5;
	private static final int POSITIVE_QUANTITY = 3;
	private static final int GREATER_POSITIVE_QUANTITY = 25;
	private static final double PRICE = 7.0;
	private static final double EPSILON = 0.001;

	private SoftAssertions softly;
	private Product product;

	@BeforeEach
	void setup() {
		softly = new SoftAssertions();
		product = new Product("name", PRICE);
	}
	
	
	@Nested
	@DisplayName("Test constructor")
	class ConstructorTests {		
			
		@Nested
		@DisplayName("Id generation")
		class IdGenerationTest{
			
			@Test
			@DisplayName("A positive number is automatically assigned as id")
			void testIdIsAutomaticallyAssignedAsPositiveNumber() {
				OrderItem item = new OrderItem(product, POSITIVE_QUANTITY);
				assertThat(item.getId()).isPositive();
			}
			
			@Test
			@DisplayName("An incremental id is automatically generated")
			void testIdsAreIncremental() {
				assertThat(new OrderItem(product, POSITIVE_QUANTITY).getId())
					.isLessThan(new OrderItem(product, POSITIVE_QUANTITY).getId());
			}
			

		}
		
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
				assertThatThrownBy(() -> new OrderItem(product, ZERO))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage(String.format("Non-positive quantity: (%d)", ZERO));
			}

			@Test
			@DisplayName("New OrderItem with negative quantity")
			void testConstructorWhenQuantityIsNegativeShouldThrow() {
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
				OrderItem item = new OrderItem(product, POSITIVE_QUANTITY);
				
				softly.assertThat(item.getProduct()).isEqualTo(product);
				softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
				softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
				softly.assertAll();
			}
		}
	}

	@Nested
	@DisplayName("Test methods that change the quantity")
	class ChangeQuantityMethods {

		private OrderItem item;
		
		@BeforeEach
		void setup() {
			item = new OrderItem();
			item.setProduct(product);
			item.setQuantity(POSITIVE_QUANTITY);
			item.setSubTotal(PRICE * POSITIVE_QUANTITY);
		}

		@Nested
		@DisplayName("Test 'increaseQuantity' method")
		class IncreaseQuantityTest {

			@Nested
			@DisplayName("Exceptional cases")
			class ExceptionalCases {

				@Test
				@DisplayName("Don't increase quantity if the given value is negative")
				void testIncreaseQuantityWhenAmountIsNegativeShouldThrow() {

					assertThatThrownBy(() -> item.increaseQuantity(NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", NEGATIVE_QUANTITY));
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}

				@Test
				@DisplayName("Don't increase quantity if the given value is zero")
				void testIncreaseQuantityWhenAmountIsZeroShouldThrow() {

					assertThatThrownBy(() -> item.increaseQuantity(ZERO))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", ZERO));
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}

			}

			@Nested
			@DisplayName("Happy case")
			class HappyCase {

				@Test
				@DisplayName("Increase quantity")
				void testIncreaseQuantity() {

					item.increaseQuantity(GREATER_POSITIVE_QUANTITY);
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY + GREATER_POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(
							(POSITIVE_QUANTITY + GREATER_POSITIVE_QUANTITY) * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
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
				@DisplayName("Don't decrease quantity if the given value is negative")
				void testDecreaseQuantityWhenAmountIsNegativeShouldThrow() {

					assertThatThrownBy(() -> item.decreaseQuantity(NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", NEGATIVE_QUANTITY));
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}

				@Test
				@DisplayName("Don't decrease quantity if the given value is zero")
				void testDecreaseQuantityWhenAmountIsZeroShouldThrow() {

					assertThatThrownBy(() -> item.decreaseQuantity(ZERO))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Non-positive quantity: (%d)", ZERO));
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}

				@Test
				@DisplayName("Don't decrease quantity if the given value is greater than the available quantity")
				void testDecreaseQuantityWhenAmountGreaterThanAvailableShouldThrow() {

					assertThatThrownBy(() -> item.decreaseQuantity(GREATER_POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage(String.format("Decrease quantity (%d) must be less than available quantity (%d)",
								GREATER_POSITIVE_QUANTITY, POSITIVE_QUANTITY));
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}

				@Test
				@DisplayName("Don't decrease quantity if the given value is equal to the available quantity")
				void testDecreaseQuantityWhenAmountEqualToAvailableShouldThrow() {

					assertThatThrownBy(() -> item.decreaseQuantity(POSITIVE_QUANTITY))
							.isInstanceOf(IllegalArgumentException.class)
							.hasMessage(String.format("Decrease quantity (%d) must be less than available quantity (%d)",
									POSITIVE_QUANTITY, POSITIVE_QUANTITY));
					softly.assertThat(item.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo(POSITIVE_QUANTITY * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}
			}

			@Nested
			@DisplayName("Happy case")
			class HappyCase {

				@Test
				@DisplayName("Decrease quantity")
				void testDecreaseQuantity() {
					item.setQuantity(GREATER_POSITIVE_QUANTITY);

					item.decreaseQuantity(POSITIVE_QUANTITY);
					softly.assertThat(item.getQuantity()).isEqualTo(GREATER_POSITIVE_QUANTITY - POSITIVE_QUANTITY);
					softly.assertThat(item.getSubTotal()).isCloseTo( (GREATER_POSITIVE_QUANTITY - POSITIVE_QUANTITY) * product.getPrice(), byLessThan(EPSILON));
					softly.assertAll();
				}
			}
		}
	}
}
