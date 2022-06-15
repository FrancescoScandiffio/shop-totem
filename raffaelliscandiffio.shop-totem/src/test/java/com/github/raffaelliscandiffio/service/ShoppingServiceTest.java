package com.github.raffaelliscandiffio.service;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.exception.RepositoryException;
import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.OrderItemRepository;
import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.github.raffaelliscandiffio.transaction.TransactionCode;
import com.github.raffaelliscandiffio.transaction.TransactionManager;

@ExtendWith(MockitoExtension.class)
class ShoppingServiceTest {

	private static final String PRODUCT_NAME = "product_name";

	private static final OrderStatus OPEN = OrderStatus.OPEN;

	private static final String ORDER_ID = "order_id";
	private static final String ITEM_ID_1 = "item_id_1";
	private static final String STOCK_ID_1 = "stock_id_1";
	private static final String PRODUCT_ID_1 = "product_id_1";

	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockRepository stockRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemRepository itemRepository;

	@Mock
	private TransactionManager transactionManager;

	@InjectMocks
	private ShoppingService shoppingService;

	@BeforeEach
	void setup() {
		when(transactionManager.runInTransaction(any())).thenAnswer(answer((TransactionCode<?> code) -> code
				.apply(productRepository, stockRepository, orderRepository, itemRepository)));
	}

	@Test
	@DisplayName("Initialise, store and return a new Order with status OPEN")
	void testOpenNewOrder() {
		doAnswer(answer((Order order) -> {
			order.setId(ORDER_ID);
			return null;
		})).when(orderRepository).save(new Order(OPEN));

		assertThat(shoppingService.openNewOrder()).isEqualTo(newTestOrderWithId(ORDER_ID, OPEN));
	}

	@Test
	@DisplayName("Return all Products from the repository")
	void testGetAllProducts() {
		List<Product> products = asList(new Product("product_1", 1.0), new Product("product_2", 2.0));
		when(productRepository.findAll()).thenReturn(products);

		assertThat(shoppingService.getAllProducts()).isEqualTo(products);
	}

	@Nested
	@DisplayName("Test cases for 'deleteOrder'")
	class DeleteOrderTests {

		private static final String ITEM_ID_2 = "item_id_2";
		private static final String STOCK_ID_2 = "stock_id_2";
		private static final String PRODUCT_ID_2 = "product_id_2";

		private static final int QUANTITY_1 = 1;
		private static final int QUANTITY_2 = 2;
		private static final int QUANTITY_3 = 3;
		private static final int QUANTITY_4 = 4;

		@Test
		@DisplayName("Delete Order when there are items that refer to it should restock the items")
		void testDeleteOrderWhenOrderIsFoundWithItemsShouldRestockTheItems() {
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			Product product_1 = newTestDefaultProductWithId(PRODUCT_ID_1);
			Product product_2 = newTestDefaultProductWithId(PRODUCT_ID_2);
			Stock stock_1 = newTestStockWithId(STOCK_ID_1, product_1, QUANTITY_1);
			Stock stock_2 = newTestStockWithId(STOCK_ID_2, product_2, QUANTITY_2);
			OrderItem item_1 = newTestOrderItemWithId(ITEM_ID_1, product_1, storedOrder, QUANTITY_3);
			OrderItem item_2 = newTestOrderItemWithId(ITEM_ID_2, product_2, storedOrder, QUANTITY_4);
			Stock toUpdate_1 = newTestStockWithId(STOCK_ID_1, product_1, QUANTITY_1 + QUANTITY_3);
			Stock toUpdate_2 = newTestStockWithId(STOCK_ID_2, product_2, QUANTITY_2 + QUANTITY_4);

			when(itemRepository.getListByOrderId(ORDER_ID)).thenReturn(asList(item_1, item_2));
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock_1);
			when(stockRepository.findByProductId(PRODUCT_ID_2)).thenReturn(stock_2);
			when(orderRepository.findById(ORDER_ID)).thenReturn(storedOrder);

			shoppingService.deleteOrder(ORDER_ID);

			InOrder inOrder = inOrder(stockRepository, orderRepository, itemRepository);
			inOrder.verify(stockRepository).update(toUpdate_1);
			inOrder.verify(itemRepository).delete(ITEM_ID_1);
			inOrder.verify(stockRepository).update(toUpdate_2);
			inOrder.verify(itemRepository).delete(ITEM_ID_2);
			inOrder.verify(orderRepository).delete(ORDER_ID);
			inOrder.verifyNoMoreInteractions();
			verify(transactionManager, times(1)).runInTransaction(any());

		}

		@Test
		@DisplayName("Delete Order when Order is not found should return without exception")
		void testDeleteOrderWhenIsNotFoundShouldReturnWithoutException() {
			when(orderRepository.findById(ORDER_ID)).thenReturn(null);

			shoppingService.deleteOrder(ORDER_ID);

			verify(orderRepository, never()).delete(any());
			verifyNoInteractions(stockRepository, itemRepository);
			verify(transactionManager, times(1)).runInTransaction(any());
		}

		@Test
		@DisplayName("Delete Order when Order is found and no OrderItem refers to it should skip the restock phase")
		void testDeleteOrderWhenOrderIsFoundAndNoOrderItemRefersToItShouldSkipTheRestockPhase() {
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			when(orderRepository.findById(ORDER_ID)).thenReturn(storedOrder);
			when(itemRepository.getListByOrderId(ORDER_ID)).thenReturn(asList());

			shoppingService.deleteOrder(ORDER_ID);

			verify(itemRepository, never()).delete(any());
			verify(orderRepository).delete(ORDER_ID);
			verifyNoInteractions(stockRepository);
			verify(transactionManager, times(1)).runInTransaction(any());

		}

		@Test
		@DisplayName("Delete Order when a Stock is not found should skip its restock phase")
		void testDeleteOrderWhenAStockIsNotFoundShouldSkipIt() {
			Product product_1 = newTestDefaultProductWithId(PRODUCT_ID_1);
			Product product_2 = newTestDefaultProductWithId(PRODUCT_ID_2);
			Stock stock_2 = newTestStockWithId(STOCK_ID_2, product_2, QUANTITY_2);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item_1 = newTestOrderItemWithId(ITEM_ID_1, product_1, storedOrder, QUANTITY_3);
			OrderItem item_2 = newTestOrderItemWithId(ITEM_ID_2, product_2, storedOrder, QUANTITY_4);
			Stock toUpdate_2 = newTestStockWithId(STOCK_ID_2, product_2, QUANTITY_2 + QUANTITY_4);

			when(orderRepository.findById(ORDER_ID)).thenReturn(storedOrder);
			when(itemRepository.getListByOrderId(ORDER_ID)).thenReturn(asList(item_1, item_2));
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(null);
			when(stockRepository.findByProductId(PRODUCT_ID_2)).thenReturn(stock_2);

			shoppingService.deleteOrder(ORDER_ID);

			InOrder inOrder = inOrder(stockRepository, orderRepository, itemRepository);
			inOrder.verify(itemRepository).delete(ITEM_ID_1);
			inOrder.verify(stockRepository).update(toUpdate_2);
			inOrder.verify(itemRepository).delete(ITEM_ID_2);
			inOrder.verify(orderRepository).delete(ORDER_ID);
			inOrder.verifyNoMoreInteractions();
			verify(transactionManager, times(1)).runInTransaction(any());

		}

	}

	@Nested
	@DisplayName("Test cases for 'closeOrder'")
	class CloseOrderTests {

		@Test
		@DisplayName("Close the Order when it exists")
		void testCloseOrderWhenIsFound() {
			when(orderRepository.findById(ORDER_ID)).thenReturn(newTestOrderWithId(ORDER_ID, OPEN));

			shoppingService.closeOrder(ORDER_ID);
			verify(orderRepository, times(1)).update(newTestOrderWithId(ORDER_ID, OrderStatus.CLOSED));
			verify(transactionManager, times(1)).runInTransaction(any());
		}

		@Test
		@DisplayName("In close Order, throw exception when the order does not exist")
		void testCloseOrderWhenTheOrderDoesNotExistShouldThrow() {
			assertThatThrownBy(() -> shoppingService.closeOrder(ORDER_ID)).isInstanceOf(RepositoryException.class)
					.hasMessage("Order not found: " + ORDER_ID);

			verify(orderRepository, never()).update(any());
		}

	}

	@Nested
	@DisplayName("Test cases for 'deleteItem'")
	class DeleteItemTests {

		private static final int QUANTITY_1 = 1;
		private static final int QUANTITY_2 = 2;
		private static final int QUANTITY_3 = 3;

		@Test
		@DisplayName("Delete OrderItem, when found by id, should restock the product and delete the item")
		void testDeleteItemWhenFound() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, QUANTITY_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, QUANTITY_2);
			Stock updatedStock = newTestStockWithId(STOCK_ID_1, product, QUANTITY_1 + QUANTITY_2);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(item);

			shoppingService.deleteItem(item);

			verify(stockRepository).update(updatedStock);
			verify(itemRepository).delete(ITEM_ID_1);
			verify(transactionManager, times(1)).runInTransaction(any());
		}

		@Test
		@DisplayName("Delete OrderItem when found with same id but different values, should restock the amount from repository and delete")
		void testDeleteItemWhenFoundButWithDifferentValuesShouldRestockTheCorrectAmount() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, QUANTITY_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem repositoryItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, QUANTITY_2);
			OrderItem viewItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, QUANTITY_3);
			Stock updatedStock = newTestStockWithId(STOCK_ID_1, product, QUANTITY_1 + QUANTITY_2);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(repositoryItem);

			shoppingService.deleteItem(viewItem);

			verify(stockRepository).update(updatedStock);
			verify(itemRepository).delete(ITEM_ID_1);
			verify(transactionManager, times(1)).runInTransaction(any());
		}

		@Test
		@DisplayName("Delete OrderItem when not found should throw exception")
		void testDeleteItemWhenNotFoundShouldThrowException() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, QUANTITY_1);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(null);

			assertThatThrownBy(() -> shoppingService.deleteItem(item)).isInstanceOf(RepositoryException.class)
					.hasMessage("Item not found: " + ITEM_ID_1);
			verify(transactionManager, times(1)).runInTransaction(any());
			verifyNoMoreInteractions(transactionManager, itemRepository);
			verifyNoInteractions(stockRepository);
		}

		@Test
		@DisplayName("Delete OrderItem when found but stock not found should skip the restock phase and do not throw exception")
		void testDeleteItemWhenFoundButReferenceStockNotFoundShouldSkipTheRestockAndReturnWithoutException() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, QUANTITY_1);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(null);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(item);

			shoppingService.deleteItem(item);

			verify(stockRepository, never()).update(any());
			verify(itemRepository).delete(ITEM_ID_1);
			verify(transactionManager, times(1)).runInTransaction(any());
		}

	}

	@Nested
	@DisplayName("Test cases for 'returnItem'")
	class ReturnItemTests {

		private static final int LOW_QUANTITY = 2;
		private static final int MID_QUANTITY = 5;
		private static final int GREAT_QUANTITY = 10;

		@Test
		@DisplayName("Restock the product linked to the given OrderItem")
		void testReturnItemShouldRestockTheProduct() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, LOW_QUANTITY);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, GREAT_QUANTITY);
			OrderItem updatedItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder,
					GREAT_QUANTITY - MID_QUANTITY);
			Stock updatedStock = newTestStockWithId(STOCK_ID_1, product, LOW_QUANTITY + MID_QUANTITY);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(item);

			assertThat(shoppingService.returnItem(item, MID_QUANTITY)).isEqualTo(updatedItem);
			verify(transactionManager).runInTransaction(any());
			verify(itemRepository).update(updatedItem);
			verify(stockRepository).update(updatedStock);
		}

		@Test
		@DisplayName("When the stock is not found should skip the restock phase and not throw")
		void testReturnItemWhenStockIsNotFoundShouldSkipAndReturn() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, MID_QUANTITY);
			OrderItem updateItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, MID_QUANTITY - LOW_QUANTITY);

			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(null);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(item);

			shoppingService.returnItem(item, LOW_QUANTITY);

			verify(itemRepository).update(updateItem);
			verify(stockRepository, never()).update(any());
		}

		@Test
		@DisplayName("When the given item is found by id but has different quantity than the retrieved one, should throw and not update")
		void testReturnItemWhenIsFoundButHasDifferentQuantityThanTheRetrievedOneShouldThrowException() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem repositoryItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, MID_QUANTITY);
			OrderItem viewItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, GREAT_QUANTITY);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(repositoryItem);

			assertThatThrownBy(() -> shoppingService.returnItem(viewItem, LOW_QUANTITY))
					.isInstanceOf(RepositoryException.class)
					.hasMessage("Stale data detected in OrderItem with id " + ITEM_ID_1);
			verify(itemRepository, never()).update(any());
			verifyNoInteractions(stockRepository);
		}

		@Test
		@DisplayName("When the given item is found by id but has different product than the retrieved one, should throw and not update")
		void testReturnItemWhenIsFoundButHasDifferentProductThanTheRetrievedOneShouldThrowException() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Product product_2 = newTestDefaultProductWithId(PRODUCT_ID_1 + "foo");
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem repositoryItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, MID_QUANTITY);
			OrderItem viewItem = newTestOrderItemWithId(ITEM_ID_1, product_2, storedOrder, MID_QUANTITY);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(repositoryItem);

			assertThatThrownBy(() -> shoppingService.returnItem(viewItem, LOW_QUANTITY))
					.isInstanceOf(RepositoryException.class)
					.hasMessage("Stale data detected in OrderItem with id " + ITEM_ID_1);
			verify(itemRepository, never()).update(any());
			verifyNoInteractions(stockRepository);
		}

		@Test
		@DisplayName("When the given item is found by id but has different order than the retrieved one, should throw and not update")
		void testReturnItemWhenIsFoundButHasDifferentOrderThanTheRetrievedOneShouldThrowException() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			Order storedOrder_2 = newTestOrderWithId(ORDER_ID + "foo", OPEN);
			OrderItem repositoryItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, MID_QUANTITY);
			OrderItem viewItem = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder_2, MID_QUANTITY);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(repositoryItem);

			assertThatThrownBy(() -> shoppingService.returnItem(viewItem, LOW_QUANTITY))
					.isInstanceOf(RepositoryException.class)
					.hasMessage("Stale data detected in OrderItem with id " + ITEM_ID_1);
			verify(itemRepository, never()).update(any());
			verifyNoInteractions(stockRepository);
		}

		@Test
		@DisplayName("When the item is not found should throw and not update")
		void testReturnItemWhenIsNotFoundShouldThrow() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, storedOrder, MID_QUANTITY);
			when(itemRepository.findById(ITEM_ID_1)).thenReturn(null);

			assertThatThrownBy(() -> shoppingService.returnItem(item, LOW_QUANTITY))
					.isInstanceOf(RepositoryException.class).hasMessage("Item not found: " + ITEM_ID_1);
			verify(itemRepository, never()).update(any());
			verifyNoInteractions(stockRepository);
		}

	}

	@Nested
	@DisplayName("Test cases for 'getOrderItems'")
	class GetOrderItemsTests {

		@Test
		@DisplayName("Retrieve the items that reference the specified order")
		void testGetOrderItems() {
			Product product_1 = new Product("name_1", 1.0);
			Product product_2 = new Product("name_2", 1.0);
			Order storedOrder = newTestOrderWithId(ORDER_ID, OPEN);
			List<OrderItem> items = asList(new OrderItem(product_1, storedOrder, 1),
					new OrderItem(product_2, storedOrder, 2));
			when(itemRepository.getListByOrderId(ORDER_ID)).thenReturn(items);
			when(orderRepository.findById(ORDER_ID)).thenReturn(storedOrder);

			assertThat(shoppingService.getOrderItems(ORDER_ID)).isEqualTo(items);
			verify(transactionManager, times(1)).runInTransaction(any());

		}

		@Test
		@DisplayName("Throw exception when Order with the given id is not found")
		void testGetOrderItemsWhenOrderIsNotFoundShouldThrow() {
			when(orderRepository.findById(ORDER_ID)).thenReturn(null);

			verifyNoInteractions(itemRepository);
			assertThatThrownBy(() -> shoppingService.getOrderItems(ORDER_ID)).isInstanceOf(RepositoryException.class)
					.hasMessage("Order with id " + ORDER_ID + " not found.");
		}

	}

	@Nested
	@DisplayName("Test cases for 'buyProduct'")
	class BuyProductTests {

		private static final int LOW_QUANTITY = 2;
		private static final int MID_QUANTITY = 5;
		private static final int GREAT_QUANTITY = 10;

		@Test
		@DisplayName("Buy Product and store in a new OrderItem")
		void testBuyProduct() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, GREAT_QUANTITY);
			Stock modifiedStock = newTestStockWithId(STOCK_ID_1, product, GREAT_QUANTITY - LOW_QUANTITY);
			Order order = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem newItem = newTestOrderItemWithId(ITEM_ID_1, product, order, LOW_QUANTITY);

			when(orderRepository.findById(ORDER_ID)).thenReturn(order);
			when(productRepository.findById(PRODUCT_ID_1)).thenReturn(product);
			doAnswer(answer((OrderItem item) -> {
				item.setId(ITEM_ID_1);
				return null;
			})).when(itemRepository).save(new OrderItem(product, order, LOW_QUANTITY));
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(itemRepository.findByProductAndOrderId(PRODUCT_ID_1, ORDER_ID)).thenReturn(null);

			assertThat(shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, LOW_QUANTITY)).isEqualTo(newItem);
			verify(transactionManager, times(1)).runInTransaction(any());
			verify(stockRepository, times(1)).update(modifiedStock);
			verifyNoMoreInteractions(itemRepository);

		}

		@Test
		@DisplayName("Buy Product when is already in Order should update the retrieved OrderItem")
		void testBuyProductWhenIsAlreadyStoredInAnOrderItemShouldUpdateIt() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, GREAT_QUANTITY);
			Stock modifiedStock = newTestStockWithId(STOCK_ID_1, product, GREAT_QUANTITY - MID_QUANTITY);
			Order order = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem repositoryItem = newTestOrderItemWithId(ITEM_ID_1, product, order, LOW_QUANTITY);
			OrderItem updatedItem = newTestOrderItemWithId(ITEM_ID_1, product, order, LOW_QUANTITY + MID_QUANTITY);

			when(orderRepository.findById(ORDER_ID)).thenReturn(order);
			when(productRepository.findById(PRODUCT_ID_1)).thenReturn(product);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(itemRepository.findByProductAndOrderId(PRODUCT_ID_1, ORDER_ID)).thenReturn(repositoryItem);

			assertThat(shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, MID_QUANTITY)).isEqualTo(updatedItem);
			verify(itemRepository, never()).save(any());
			verify(itemRepository, times(1)).update(updatedItem);
			verify(stockRepository, times(1)).update(modifiedStock);
			verify(transactionManager, times(1)).runInTransaction(any());

		}

		@Test
		@DisplayName("Buy product when the requested quantity is greater than the available quantity should throw exception")
		void testBuyProductWhenTheRequestedQuantityIsGreaterThanTheAvailableQuantityShouldThrow() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, LOW_QUANTITY);
			Order order = newTestOrderWithId(ORDER_ID, OPEN);
			when(productRepository.findById(PRODUCT_ID_1)).thenReturn(product);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(orderRepository.findById(ORDER_ID)).thenReturn(order);

			assertThatThrownBy(() -> shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, GREAT_QUANTITY))
					.isInstanceOf(RepositoryException.class)
					.hasMessage("Not enough quantity. Cannot buy product: " + PRODUCT_NAME);
			verify(stockRepository, never()).update(any());
			verify(transactionManager, times(1)).runInTransaction(any());
			verifyNoInteractions(itemRepository);
		}

		@Test
		@DisplayName("Buy Product when the requested quantity equals the available quantity")
		void testBuyProductWhenTheRequestedQuantityIsEqualToTheAvailableQuantityShouldBuy() {
			Product product = newTestDefaultProductWithId(PRODUCT_ID_1);
			Stock stock = newTestStockWithId(STOCK_ID_1, product, MID_QUANTITY);
			Stock modifiedStock = newTestStockWithId(STOCK_ID_1, product, MID_QUANTITY - MID_QUANTITY);
			Order order = newTestOrderWithId(ORDER_ID, OPEN);
			OrderItem item = newTestOrderItemWithId(ITEM_ID_1, product, order, LOW_QUANTITY);
			OrderItem modifiedItem = newTestOrderItemWithId(ITEM_ID_1, product, order, LOW_QUANTITY + MID_QUANTITY);

			when(orderRepository.findById(ORDER_ID)).thenReturn(order);
			when(productRepository.findById(PRODUCT_ID_1)).thenReturn(product);
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(stock);
			when(itemRepository.findByProductAndOrderId(PRODUCT_ID_1, ORDER_ID)).thenReturn(item);

			assertThat(shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, MID_QUANTITY)).isEqualTo(item);
			verify(stockRepository, times(1)).update(modifiedStock);
			verify(itemRepository, times(1)).update(modifiedItem);
			verify(transactionManager, times(1)).runInTransaction(any());

		}

		@Test
		@DisplayName("Buy Product when Order does not exist should throw exception")
		void testBuyProductWhenOrderDoesNotExistShouldThrow() {
			when(orderRepository.findById(ORDER_ID)).thenReturn(null);

			assertThatThrownBy(() -> shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, 3))
					.isInstanceOf(RepositoryException.class).hasMessage("Order not found: " + ORDER_ID);
			verifyNoInteractions(productRepository, stockRepository, itemRepository);
		}

		@Test
		@DisplayName("Buy Product when Product does not exist should throw exception")
		void testBuyProductWhenProductDoesNotExistShouldThrow() {
			when(orderRepository.findById(ORDER_ID)).thenReturn(new Order(OPEN));
			when(productRepository.findById(PRODUCT_ID_1)).thenReturn(null);

			assertThatThrownBy(() -> shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, 3))
					.isInstanceOf(RepositoryException.class).hasMessage("Product not found: " + PRODUCT_ID_1);
			verifyNoInteractions(stockRepository, itemRepository);
		}

		@Test
		@DisplayName("Buy Product when Stock does not exist should throw exception")
		void testBuyProductWhenStockDoesNotExistShouldThrow() {
			when(orderRepository.findById(ORDER_ID)).thenReturn(new Order(OPEN));
			when(productRepository.findById(PRODUCT_ID_1)).thenReturn(new Product("name", 1.0));
			when(stockRepository.findByProductId(PRODUCT_ID_1)).thenReturn(null);

			assertThatThrownBy(() -> shoppingService.buyProduct(ORDER_ID, PRODUCT_ID_1, 3))
					.isInstanceOf(RepositoryException.class)
					.hasMessage("Stock not found. Query by product: " + PRODUCT_ID_1);
			verifyNoMoreInteractions(stockRepository);
			verifyNoInteractions(itemRepository);
		}

	}

	@Nested
	@DisplayName("Test cases for 'saveNewProductAndStock'")
	class SaveProductAndStockTests {

		private static final double POSITIVE_PRICE = 3.0;
		private static final int POSITIVE_QUANTITY = 5;

		@Test
		@DisplayName("Save new product and new stock in the same transaction")
		void testSaveProductAndStock() {
			Product productToStore = new Product(PRODUCT_NAME, POSITIVE_PRICE);
			Stock stockToStore = new Stock(productToStore, POSITIVE_QUANTITY);

			shoppingService.saveProductAndStock(PRODUCT_NAME, POSITIVE_PRICE, POSITIVE_QUANTITY);

			InOrder inOrder = inOrder(productRepository, stockRepository);
			inOrder.verify(productRepository).save(productToStore);
			inOrder.verify(stockRepository).save(stockToStore);
			verify(transactionManager, times(1)).runInTransaction(any());
		}

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionTests {
			@Test
			@DisplayName("When the name is null, do not save and throw exception")
			void testSaveProductAndStockWhenNameIsNullShouldThrow() {

				assertThatThrownBy(() -> shoppingService.saveProductAndStock(null, POSITIVE_PRICE, POSITIVE_QUANTITY))
						.isInstanceOf(NullPointerException.class).hasMessage("The Product name cannot be null");
				verifyNoInteractions(productRepository, stockRepository);
			}

			@ParameterizedTest
			@ValueSource(doubles = { 0.0, -0.1 })
			@DisplayName("When the price is not positive, do not save and throw exception")
			void testSaveProductAndStockWhenPriceIsNotPositiveShouldThrow(double price) {

				assertThatThrownBy(() -> shoppingService.saveProductAndStock(PRODUCT_NAME, price, POSITIVE_QUANTITY))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Price must be positive. Received: " + price);
				verifyNoInteractions(productRepository, stockRepository);

			}

			@ParameterizedTest
			@ValueSource(ints = { 0, -1 })
			@DisplayName("When the quantity is not positive, do not save and throw exception")
			void testSaveProductAndStockWhenQuantityIsNotPositiveShouldThrow(int quantity) {

				assertThatThrownBy(() -> shoppingService.saveProductAndStock(PRODUCT_NAME, POSITIVE_PRICE, quantity))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessage("Quantity must be positive. Received: " + quantity);
				verifyNoInteractions(productRepository, stockRepository);

			}
		}

	}

	// Private utility methods

	// Private utility methods

	private Product newTestDefaultProductWithId(String id) {
		Product p = new Product(PRODUCT_NAME, 1.0);
		p.setId(id);
		return p;
	}

	private Order newTestOrderWithId(String id, OrderStatus status) {
		Order o = new Order(status);
		o.setId(id);
		return o;
	}

	private Stock newTestStockWithId(String id, Product product, int quantity) {
		Stock s = new Stock(product, quantity);
		s.setId(id);
		return s;
	}

	private OrderItem newTestOrderItemWithId(String id, Product product, Order order, int quantity) {
		OrderItem i = new OrderItem(product, order, quantity);
		i.setId(id);
		return i;
	}

}