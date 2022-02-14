package com.github.raffaelliscandiffio.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
	@DisplayName("Test item insertion with 'insertItem'")
	class InsertItemTest {

		@Nested
		@DisplayName("Good cases")
		class InsertGoodCasesTest {

			@Test
			@DisplayName("Increase item quantity when the specified product is found")
			void testInsertItemWhenTheSpecifiedProductIsFoundShouldIncreaseQuantity() {
				Product product = new Product(1, "product1", 2.0);
				items.add(new OrderItem(product, POSITIVE_QUANTITY, 2.0 * POSITIVE_QUANTITY));
				OrderItem returned = order.insertItem(product, POSITIVE_QUANTITY);
				softly.assertThat(returned.getQuantity()).isEqualTo(POSITIVE_QUANTITY * 2);
				softly.assertThat(returned.getSubTotal()).isEqualTo(POSITIVE_QUANTITY * 2 * 2.0);
				softly.assertThat(items).containsOnly(returned);
				softly.assertAll();
			}

		}

		@Nested
		@DisplayName("Test exceptional cases")
		class InsertionExceptionalCasesTest {

			@Test
			@DisplayName("Throw NullPointerException when the specified product is null")
			void testInsertItemWhenProductIsNullShouldThrowNullPointerException() {
				assertThatThrownBy(() -> order.insertItem(null, POSITIVE_QUANTITY))
						.isInstanceOf(NullPointerException.class).hasMessage("Product cannot be null");
				assertThat(items).isEmpty();
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is zero")
			void testInsertItemWhenSpecifiedQuantityIsZeroShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				assertThatThrownBy(() -> order.insertItem(product, ZERO)).isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: 0");
				assertThat(items).isEmpty();
			}

			@Test
			@DisplayName("Throw IllegalArgumentException when the specified quantity is negative")
			void testInsertItemWhenSpecifiedQuantityIsNegativeShouldThrowIllegalArgumentException() {
				Product product = new Product(1, "product", 2.0);
				assertThatThrownBy(() -> order.insertItem(product, NEGATIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: " + NEGATIVE_QUANTITY);
				assertThat(items).isEmpty();
			}

		}

	}

	@Nested
	@DisplayName("Test 'popItemById'")
	class PopItemByIdTest {

		@Test
		@DisplayName("Remove the specified item from the order")
		void testPopItemByIdWhenItemIsFoundShouldRemoveFromOrder() {
			OrderItem otherItem = mock(OrderItem.class);
			when(item.getId()).thenReturn(ITEM_ID);
			when(otherItem.getId()).thenReturn(ITEM_ID + 1);
			items.add(otherItem);
			items.add(item);

			OrderItem result = order.popItemById(ITEM_ID);
			softly.assertThat(result).isEqualTo(item);
			softly.assertThat(items).hasSize(1).containsOnly(otherItem);
			softly.assertAll();
		}

		@Test
		@DisplayName("Throw exception when the specified item does not exist")
		void testPopItemByIdWhenItemNotFoundShouldThrow() {

			assertThatThrownBy(() -> order.popItemById(ITEM_ID)).isInstanceOf(NoSuchElementException.class)
					.hasMessage(String.format("Item with id (%s) not found", ITEM_ID));

		}

		@Test
		@DisplayName("Throw exception when the specified item does not exist")
		void testPopItemByIdWhenItemNotFoundShouldThrow() {

			assertThatThrownBy(() -> order.popItemById(ITEM_ID)).isInstanceOf(NoSuchElementException.class)
					.hasMessage(String.format("Item with id (%s) not found", ITEM_ID));

		}
	}

	@Nested
	@DisplayName("Test 'decreaseItem'")
	class DecreaseItemTest {

		@Test
		@DisplayName("Decrease item quantity when item exists")
		void testDecreaseItemWhenItemIsFoundShouldDecreaseQuantity() {
			OrderItem otherItem = mock(OrderItem.class);
			when(item.getId()).thenReturn(ITEM_ID);
			when(otherItem.getId()).thenReturn(ITEM_ID + 1);
			items.add(otherItem);
			items.add(item);

			OrderItem returned = order.decreaseItem(ITEM_ID, POSITIVE_QUANTITY);
			assertThat(returned).isEqualTo(item);
			verify(item, times(1)).decreaseQuantity(POSITIVE_QUANTITY);
			verify(otherItem, never()).decreaseQuantity(anyInt());
			verifyNoMoreInteractions(item, otherItem);

		}

		@Test
		@DisplayName("Throw exception when the specified item does not exist")
		void testDecreaseItemWhenItemNotFoundShouldThrow() {

			assertThatThrownBy(() -> order.decreaseItem(ITEM_ID, POSITIVE_QUANTITY))
					.isInstanceOf(NoSuchElementException.class)
					.hasMessage(String.format("Item with id (%s) not found", ITEM_ID));
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
