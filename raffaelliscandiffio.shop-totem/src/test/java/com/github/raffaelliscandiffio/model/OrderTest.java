package com.github.raffaelliscandiffio.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("Test class Order")
@ExtendWith(MockitoExtension.class)
class OrderTest {

	private static final int QUANTITY = 3;
	private static final int GREATER_QUANTITY = 15;
	private static final double PRICE = 5.0;

	private Product product;
	private Order order;
	private List<OrderItem> items;

	@Mock
	private OrderItem item;

	@BeforeEach
	void setup() {
		items = new ArrayList<OrderItem>();
		order = new Order(items);
		product = new Product("name", PRICE, QUANTITY);
	}

	@Nested
	@DisplayName("Test 'insertItem'")
	class InsertItemTest {

		@Test
		@DisplayName("Construct a new OrderItem and add it to the list when the list is empty")
		void testInsertItemWhenListIsEmptyShouldInsertNewOrderItem() {
			SoftAssertions softly = new SoftAssertions();

			order.insertItem(product, QUANTITY);
			softly.assertThat(items).hasSize(1).extracting("product").contains(product);
			softly.assertThat(items).extracting("quantity").contains(QUANTITY);
			softly.assertAll();
		}

		@Test
		@DisplayName("Increase quantity with 'InsertItem' when an item with the same product is present")
		void testInsertItemWhenOneItemWithSameProductIsPresentShouldIncreaseQuantity() {
			when(item.getProduct()).thenReturn(product);
			items.add(item);

			order.insertItem(product, QUANTITY);
			assertThat(items).hasSize(1).extracting("product").contains(product);
			verify(item, times(1)).increaseQuantity(QUANTITY);
			verifyNoMoreInteractions(item);
		}

		@Test
		@DisplayName("Add new OrderItem to list when another item with different product is present")
		void testInsertItemWhenOneItemWithDifferentProductIsPresentShouldInsertNewOrderItem() {
			SoftAssertions softly = new SoftAssertions();
			when(item.getProduct()).thenReturn(new Product("name", PRICE, QUANTITY));
			items.add(item);

			order.insertItem(product, GREATER_QUANTITY);

			softly.assertThat(items).hasSize(2);
			softly.assertThat(items).extracting("product").contains(product);
			softly.assertThat(items).extracting("quantity")
				.doesNotContain(QUANTITY + GREATER_QUANTITY)
				.contains(GREATER_QUANTITY);
			softly.assertAll();
			verify(item, never()).increaseQuantity(anyInt());
		}

		@Test
		@DisplayName("Increase item quantity with 'InsertItem' when multiple items are present and the given product is already binded to one of them")
		void testInsertItemWhenMultipleItemsArePresentAndGivenProductIsFoundShouldIncreaseItemQuantity() {
			OrderItem otherItem = mock(OrderItem.class);
			when(item.getProduct()).thenReturn(product);
			when(otherItem.getProduct()).thenReturn(new Product("name", PRICE, QUANTITY));
			items.add(otherItem);
			items.add(item);

			order.insertItem(product, QUANTITY);
			assertThat(items).containsOnly(item, otherItem);
			verify(otherItem, never()).increaseQuantity(anyInt());
			verify(item, times(1)).increaseQuantity(QUANTITY);
			verifyNoMoreInteractions(item, otherItem);
		}

		// Documents the behaviour
		@Test
		@DisplayName("Insert new OrderItem 'InsertItem' when multiple items are present but the given product is not found")
		void testInsertItemWhenMultipleItemsArePresentAndGivenProductIsNotFoundShouldInsertNewOrderItem() {
			OrderItem otherItem = mock(OrderItem.class);
			when(item.getProduct()).thenReturn(new Product("name", PRICE, QUANTITY));
			when(otherItem.getProduct()).thenReturn(new Product("name_2", PRICE, QUANTITY));
			items.add(item);
			items.add(otherItem);

			order.insertItem(product, QUANTITY);
			assertThat(items).hasSize(3).extracting("product").contains(product);
			verify(item, never()).increaseQuantity(anyInt());
			verify(otherItem, never()).increaseQuantity(anyInt());
			verifyNoMoreInteractions(item, otherItem);

		}

	}

}
