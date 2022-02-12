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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Test class Order")
@ExtendWith(MockitoExtension.class)
class OrderTest {

	private static final int QUANTITY = 3;
	private static final int GREATER_QUANTITY = 15;
	private static final String ITEM_ID = "1";

	private Order order;
	private List<OrderItem> items;
	private SoftAssertions softly;

	@BeforeEach
	void setup() {
		softly = new SoftAssertions();
		items = new ArrayList<OrderItem>();
		order = new Order(items);
		product = getNewProduct();
	}

	@Nested
	@DisplayName("Test 'insertItem'")
	class InsertItemTest {

		@Test
		@DisplayName("A positive number is automatically assigned as id")
		void testIdIsAutomaticallyAssignedAsPositiveNumber() {
			OrderItem inserted = order.insertItem(product, QUANTITY);
			assertThat(Integer.valueOf(inserted.getId())).isPositive();
		}

		@Test
		@DisplayName("An incremental id is automatically assigned to OrderItem")
		void testIdsAreIncremental() {
			OrderItem insert1 = order.insertItem(new Product(1, "product1", 1), QUANTITY);
			OrderItem insert2 = order.insertItem(new Product(2, "product2", 1), QUANTITY);
			assertThat(Integer.valueOf(insert1.getId())).isLessThan(Integer.valueOf(insert2.getId()));
		}

		@Test
		@DisplayName("Construct a new OrderItem and add it to the list when the list is empty")
		void testInsertItemWhenListIsEmptyShouldInsertNewOrderItem() {
			OrderItem returned = order.insertItem(product, QUANTITY);
			softly.assertThat(returned).isNotNull();
			softly.assertThat(items).containsOnly(returned);
			softly.assertThat(items).extracting(OrderItem::getProduct).contains(product);
			softly.assertThat(items).extracting(OrderItem::getQuantity).contains(QUANTITY);
			softly.assertAll();
		}

		@Test
		@DisplayName("Increase item quantity when an item with the same product is present")
		void testInsertItemWhenOneItemWithSameProductIsPresentShouldIncreaseQuantity() {
			when(item.getProduct()).thenReturn(product);
			items.add(item);

			OrderItem returned = order.insertItem(product, QUANTITY);
			softly.assertThat(returned).isEqualTo(item);
			softly.assertThat(items).containsOnly(item);
			verify(item, times(1)).increaseQuantity(QUANTITY);
			verifyNoMoreInteractions(item);
			softly.assertAll();
		}

		@Test
		@DisplayName("Add new OrderItem to list when another item with different product is present")
		void testInsertItemWhenOneItemWithDifferentProductIsPresentShouldInsertNewOrderItem() {
			when(item.getProduct()).thenReturn(getNewProduct());
			items.add(item);

			OrderItem returned = order.insertItem(product, GREATER_QUANTITY);
			softly.assertThat(returned).isNotNull();
			softly.assertThat(returned).isNotSameAs(item);
			softly.assertThat(items).hasSize(2).contains(returned);
			softly.assertThat(items).extracting(OrderItem::getProduct).contains(product);
			softly.assertThat(items).extracting(OrderItem::getQuantity).contains(GREATER_QUANTITY);
			softly.assertAll();
			verify(item, never()).increaseQuantity(anyInt());
		}

		@Test
		@DisplayName("Increase item quantity when multiple items are present and the given product is already binded to one of them")
		void testInsertItemWhenMultipleItemsArePresentAndGivenProductIsFoundShouldIncreaseItemQuantity() {
			OrderItem otherItem = mock(OrderItem.class);
			when(item.getProduct()).thenReturn(product);
			when(otherItem.getProduct()).thenReturn(getNewProduct());
			items.add(otherItem);
			items.add(item);

			OrderItem returned = order.insertItem(product, QUANTITY);
			softly.assertThat(returned).isEqualTo(item);
			softly.assertThat(items).containsOnly(item, otherItem);
			softly.assertAll();
			verify(otherItem, never()).increaseQuantity(anyInt());
			verify(item, times(1)).increaseQuantity(QUANTITY);
			verifyNoMoreInteractions(item, otherItem);
		}

		// Documents the behaviour
		@Test
		@DisplayName("Insert new OrderItem when multiple items are present but the given product is not found")
		void testInsertItemWhenMultipleItemsArePresentAndGivenProductIsNotFoundShouldInsertNewOrderItem() {
			OrderItem otherItem = mock(OrderItem.class);
			when(item.getProduct()).thenReturn(getNewProduct());
			when(otherItem.getProduct()).thenReturn(getNewProduct());
			items.add(item);
			items.add(otherItem);

			OrderItem returned = order.insertItem(product, QUANTITY);
			verify(item, never()).increaseQuantity(anyInt());
			verify(otherItem, never()).increaseQuantity(anyInt());
			verifyNoMoreInteractions(item, otherItem);
			softly.assertThat(returned).isNotNull();
			softly.assertThat(returned).isNotSameAs(item);
			softly.assertThat(returned).isNotSameAs(otherItem);
			softly.assertThat(items).containsOnly(item, otherItem, returned);

			softly.assertThat(items).extracting(OrderItem::getProduct).contains(product);
			softly.assertThat(items).extracting(OrderItem::getQuantity).contains(QUANTITY);
			softly.assertAll();
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

			OrderItem returned = order.decreaseItem(ITEM_ID, QUANTITY);
			assertThat(returned).isEqualTo(item);
			verify(item, times(1)).decreaseQuantity(QUANTITY);
			verify(otherItem, never()).decreaseQuantity(anyInt());
			verifyNoMoreInteractions(item, otherItem);

		}

		@Test
		@DisplayName("Throw exception when the specified item does not exist")
		void testDecreaseItemWhenItemNotFoundShouldThrow() {

			assertThatThrownBy(() -> order.decreaseItem(ITEM_ID, QUANTITY)).isInstanceOf(NoSuchElementException.class)
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

	private long id = 0;

	// Utility method to construct new product for test
	private Product getNewProduct() {
		return new Product(++id, "name", 3.0);
	}

}
