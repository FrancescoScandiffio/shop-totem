package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Test class Order")
class OrderTest {

	private static final int ZERO = 0;
	private static final int NEGATIVE_QUANTITY = -1;
	private static final int POSITIVE_QUANTITY = 3;
	private static final int GREATER_POSITIVE_QUANTITY = 10;
	private static final String ITEM_ID = "1";

	private Order order;
	private List<OrderItem> items;
	private SoftAssertions softly;

	@BeforeEach
	void setup() {
		softly = new SoftAssertions();
		items = new ArrayList<OrderItem>();
		order = new Order(items);
	}

	@Nested
	@DisplayName("Test method 'addNewProduct'")
	class AddNewProductTest {

		@Test
		@DisplayName("Construct a new OrderItem and add it to the list when the specified product is not found")
		void testAddNewProductWhenTheSpecifiedProductIsNotFoundShouldConstructANewOrderItemAndInsertItInList() {
			Product product = new Product(1, "product1", 2.0);
			OrderItem returned = order.addNewProduct(product, POSITIVE_QUANTITY);
			softly.assertThat(returned).isNotNull();
			softly.assertThat(returned.getProduct()).isEqualTo(product);
			softly.assertThat(returned.getQuantity()).isEqualTo(POSITIVE_QUANTITY);
			softly.assertThat(returned.getSubTotal()).isEqualTo(POSITIVE_QUANTITY * 2.0);
			softly.assertThat(items).containsOnly(returned);
			softly.assertAll();
		}

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCasesTest {

			@Test
			@DisplayName("Throw NullPointerException when the specified product is null")
			void testAddNewProductWhenProductIsNullShouldThrowNullPointerException() {
				assertThatThrownBy(() -> order.addNewProduct(null, POSITIVE_QUANTITY))
						.isInstanceOf(NullPointerException.class).hasMessage("Product cannot be null");
				assertThat(items).isEmpty();
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is zero")
			void testAddNewProductWhenSpecifiedQuantityIsZeroShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				assertThatThrownBy(() -> order.addNewProduct(product, ZERO))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: 0");
				assertThat(items).isEmpty();
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is negative")
			void testAddNewProductWhenSpecifiedQuantityIsNegativeShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				assertThatThrownBy(() -> order.addNewProduct(product, NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: " + NEGATIVE_QUANTITY);
				assertThat(items).isEmpty();
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified product is found")
			void testAddNewProductWhenSpecifiedProductIsFoundShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = new OrderItem(product, 5, 10.0);
				items.add(storedItem);
				assertThatThrownBy(() -> order.addNewProduct(product, POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Product with id 1 already exists in this Order");
				assertThat(items).containsOnly(storedItem);
			}

		}

	}

	@Nested
	@DisplayName("Test method 'increaseProductQuantity'")
	class IncreaseProductQuantityTest {

		@Test
		@DisplayName("Increase the product quantity in order when the specified product is found")
		void testIncreaseProductQuantityWhenTheSpecifiedProductIsFoundShouldIncreaseProductQuantityAndSubtotal() {
			Product product = new Product(1, "product1", 2.0);
			items.add(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
			OrderItem returned = order.increaseProductQuantity(product, POSITIVE_QUANTITY);
			softly.assertThat(returned.getQuantity()).isEqualTo(POSITIVE_QUANTITY * 2);
			softly.assertThat(returned.getSubTotal()).isEqualTo(POSITIVE_QUANTITY * 2 * 2.0);
			softly.assertThat(items).containsOnly(returned);
			softly.assertAll();
		}

		@Nested
		@DisplayName("Exceptional cases")
		class IncreaseProductQuantityExceptionalCasesTest {

			@Test
			@DisplayName("Throw NullPointerException when the specified product is null")
			void testIncreaseProductQuantityWhenProductIsNullShouldThrowNullPointerException() {
				Product product = new Product(1, "product1", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.increaseProductQuantity(null, POSITIVE_QUANTITY))
						.isInstanceOf(NullPointerException.class).hasMessage("Product cannot be null");
				verifyNoInteractions(storedItem);
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is zero")
			void testIncreaseProductQuantityWhenSpecifiedQuantityIsZeroShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.increaseProductQuantity(product, ZERO))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: 0");
				verifyNoInteractions(storedItem);
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is negative")
			void testIncreaseProductQuantityWhenSpecifiedQuantityIsNegativeShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.increaseProductQuantity(product, NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: " + NEGATIVE_QUANTITY);
				verifyNoInteractions(storedItem);
			}

			@Test
			@DisplayName("Throw NoSuchElementException when the specified product is not found")
			void testIncreaseProductQuantityWhenSpecifiedProductIsNotFoundShouldThrowNoSuchElementException() {
				Product product = new Product(1, "product", 2.0);
				assertThatThrownBy(() -> order.increaseProductQuantity(product, POSITIVE_QUANTITY))
						.isInstanceOf(NoSuchElementException.class)
						.hasMessage("Product with id 1 not found in this Order");
			}

		}

	}

	@Nested
	@DisplayName("Test method 'decreaseProductQuantity'")
	class DecreaseProductQuantityTest {

		@Test
		@DisplayName("Decrease product quantity when the specified product is found")
		void testDecreaseProductQuantityWhenTheSpecifiedProductIsFoundShouldDecreaseItemQuantityAndTheSubtotal() {
			Product product = new Product(1, "product1", 2.0);
			items.add(new OrderItem(product, GREATER_POSITIVE_QUANTITY, 2.0 * GREATER_POSITIVE_QUANTITY));
			OrderItem returned = order.decreaseProductQuantity(product, POSITIVE_QUANTITY);
			softly.assertThat(returned.getQuantity()).isEqualTo(GREATER_POSITIVE_QUANTITY - POSITIVE_QUANTITY);
			softly.assertThat(returned.getSubTotal()).isEqualTo(2.0 * (GREATER_POSITIVE_QUANTITY - POSITIVE_QUANTITY));
			softly.assertThat(items).containsOnly(returned);
			softly.assertAll();
		}

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCasesTest {

			@Test
			@DisplayName("Throw NullPointerException when the specified product is null")
			void testDecreaseProductQuantityWhenProductIsNullShouldThrowNullPointerException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.decreaseProductQuantity(null, POSITIVE_QUANTITY))
						.isInstanceOf(NullPointerException.class).hasMessage("Product cannot be null");
				verifyNoInteractions(storedItem);

			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is zero")
			void testDecreaseProductQuantityWhenSpecifiedQuantityIsZeroShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.decreaseProductQuantity(product, ZERO))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: 0");
				verifyNoInteractions(storedItem);
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is negative")
			void testDecreaseProductQuantityWhenSpecifiedQuantityIsNegativeShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.decreaseProductQuantity(product, NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: " + NEGATIVE_QUANTITY);
				verifyNoInteractions(storedItem);
			}

			@Test
			@DisplayName("Throw NoSuchElementException when the specified product is not found")
			void testDecreaseProductQuantityWhenSpecifiedProductIsNotFoundShouldThrowNoSuchElementException() {
				Product product = new Product(1, "product", 2.0);
				assertThatThrownBy(() -> order.decreaseProductQuantity(product, POSITIVE_QUANTITY))
						.isInstanceOf(NoSuchElementException.class)
						.hasMessage("Product with id 1 not found in this Order");
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is equal to the item quantity")
			void testDecreaseProductQuantityWhenSpecifiedQuantityIsEqualToItemQuantityShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.decreaseProductQuantity(product, POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class).hasMessage(
								"Quantity must be less than " + POSITIVE_QUANTITY + ". Received: " + POSITIVE_QUANTITY);
				verify(storedItem, never()).setQuantity(anyInt());
				verify(storedItem, never()).setSubTotal(anyDouble());
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is greater than the item quantity")
			void testDecreaseProductQuantityWhenSpecifiedQuantityIsGreaterThanItemQuantityShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				OrderItem storedItem = spy(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				items.add(storedItem);
				assertThatThrownBy(() -> order.decreaseProductQuantity(product, GREATER_POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class).hasMessage("Quantity must be less than "
								+ POSITIVE_QUANTITY + ". Received: " + GREATER_POSITIVE_QUANTITY);
				verify(storedItem, never()).setQuantity(anyInt());
				verify(storedItem, never()).setSubTotal(anyDouble());

			}

		}

	}

	@Nested
	@DisplayName("Test 'findItemByProductId'")
	class FindByProductTest {

		@Test
		@DisplayName("Return the item when the product is found")
		void testFindItemByProductIdWhenProductIsFound() {
			OrderItem otherItem = mock(OrderItem.class);
			when(otherItem.getProduct()).thenReturn(getNewProduct());
			when(item.getProduct()).thenReturn(product);
			items.add(otherItem);
			items.add(item);

			OrderItem itemReturned = order.findItemByProductId(product.getId());
			assertThat(itemReturned).isEqualTo(item);
		}

		@Test
		@DisplayName("Return null when the product is not found")
		void testFindItemByProductIdWhenProductIsNotFoundShouldReturnNull() {
			OrderItem itemReturned = order.findItemByProductId(product.getId());
			items.add(item);
			assertThat(itemReturned).isNull();

		}

	}

	@Nested
	@DisplayName("Test 'clear'")
	class ClearTest {

		@Test
		@DisplayName("Clear the list of items")
		void testClear() {
			items.add(item);
			order.clear();
			assertThat(items).isEmpty();
		}

	}

}
