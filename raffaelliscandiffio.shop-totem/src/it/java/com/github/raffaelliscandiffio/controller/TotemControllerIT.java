package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.assertj.core.api.SoftAssertions;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionException;
import com.github.raffaelliscandiffio.utils.GUITestExtension;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;

@ExtendWith(GUITestExtension.class)
class TotemControllerIT {

	private TotemController totemController;

	private ShoppingService shoppingService;

	private TotemSwingView totemView;

	private FrameFixture window;

	private SoftAssertions softly;

	@BeforeAll
	static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() {

		softly = new SoftAssertions();

		shoppingService = mock(ShoppingService.class);

		GuiActionRunner.execute(() -> {
			totemView = new TotemSwingView();
			totemController = new TotemController(shoppingService, totemView);
			totemView.setTotemController(totemController);
			return totemView;
		});

		window = new FrameFixture(totemView);
		window.show(); // shows the frame to test
	}

	@AfterEach
	void clean() throws Exception {
		window.cleanUp();
	}

	@Test
	@GUITest
	@DisplayName("'Start Shopping' button in welcome view should resetView, set orderId, open shopping panel and show products")
	void testStartShoppingButtonInWelcome() {
		String orderId = "1";
		Order order = new Order(OrderStatus.OPEN);
		order.setId(orderId);

		when(shoppingService.saveOrder(any(Order.class))).thenReturn(order);
		when(shoppingService.getAllProducts()).thenReturn(Arrays.asList(new Product("Pasta", 2.5)));

		window.button("welcomeStartShopping").click();
		
		window.panel("shoppingPane").requireVisible();
		softly.assertThat(totemView.getOrderId()).isEqualTo(orderId);
		softly.assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.5 €");
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'welcomeStartShopping' button should not show products and should show error when saveOrder throws")
	void testStartShoppingButtonInWelcomeShouldNotShowProductsAndShouldShowErrorWhenSaveOrderThrows() {

		String errorMessage = "Error message";
		doThrow(new TransactionException(errorMessage)).when(shoppingService).saveOrder(any(Order.class));

		window.button("welcomeStartShopping").click();

		window.panel("shoppingPane").requireVisible();
		softly.assertThat(window.list("productList").contents()).isEmpty();
		window.label("messageLabel").requireText(errorMessage);
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'continueShopping' button should open shopping panel and show products")
	void testContinueShoppingButtonInCart() {
		GuiActionRunner.execute(() -> totemView.showOrder());

		when(shoppingService.getAllProducts()).thenReturn(Arrays.asList(new Product("Pasta", 2.5)));

		window.button(JButtonMatcher.withText("Continue Shopping")).click();

		assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.5 €");
		window.panel("shoppingPane").requireVisible();
	}

	@Test
	@GUITest
	@DisplayName("'continueShopping' button should not show products and display error when getAllProducts throws")
	void testContinueShoppingButtonInCartShouldShowErrorAndNotShowProductsWhenGetAllProductsThrows() {
		GuiActionRunner.execute(() -> totemView.showOrder());

		String errorMessage = "Error message";
		doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();

		window.button(JButtonMatcher.withText("Continue Shopping")).click();

		assertThat(window.list("productList").contents()).isEmpty();
		window.label("messageLabel").requireText(errorMessage);
		window.panel("shoppingPane").requireVisible();
	}

	@Test
	@GUITest
	@DisplayName("'Cancel Shopping' button should call deleteOrder, resetView, set order id to null and show welcome")
	void testCancelShoppingInCart() {
		String orderId = "1";
		Product product = new Product("Pasta", 2.5);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), 4);

		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});
		totemView.setOrderId(orderId);

		window.button("cartBtnCancelShopping").click();

		window.panel("welcomePane").requireVisible();
		softly.assertThat(totemView.getOrderId()).isEqualTo(null);
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).isEmpty();
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Cancel Shopping' button should show error when cancel shopping is not possible")
	void testCancelShoppingInCartShowsError() {
		String orderId = "1";
		double price = 2.5;
		int quantity = 4;
		Product product = new Product("Pasta", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantity);
		String errorMessage = "Error message";
		doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteOrder(orderId);

		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});
		totemView.setOrderId(orderId);

		window.button("cartBtnCancelShopping").click();
		window.label("cartMessageLabel").requireText(errorMessage);
		window.panel("cartPane").requireVisible();
		softly.assertThat(totemView.getOrderId()).isEqualTo(orderId);
		GuiActionRunner.execute(() -> totemView.showShopping());
		assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.5 €");
		GuiActionRunner.execute(() -> totemView.showOrder());
		assertThat(window.list("cartList").contents())
				.containsExactly("Pasta - Quantity: 4 - Price: 2.5 € - Subtotal: 10.0 €");

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Cart' button should open cart panel")
	void testCartButtonInShopping() {
		GuiActionRunner.execute(() -> totemView.showShopping());

		window.button(JButtonMatcher.withText("Cart")).click();

		window.panel("cartPane").requireVisible();
	}

	@Test
	@GUITest
	@DisplayName("'Add' button should add product with quantity to cart")
	void testAddButtonInShopping() {
		String orderId = "5";
		int quantity = 3;
		totemView.setOrderId(orderId);

		GuiActionRunner.execute(() -> totemView.showShopping());
		Product product = new Product("Pasta", 2);
		GuiActionRunner.execute(() -> totemView.showAllProducts(Arrays.asList(product)));

		when(shoppingService.buyProduct(orderId, product.getId(), quantity))
				.thenReturn(new OrderItem(product, new Order(OrderStatus.OPEN), quantity));

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(quantity));
		window.button(JButtonMatcher.withText("Add")).click();

		window.label("messageLabel").requireText("Added 3 Pasta");

		GuiActionRunner.execute(() -> totemView.showOrder());
		assertThat(window.list("cartList").contents())
				.containsExactly("Pasta - Quantity: 3 - Price: 2.0 € - Subtotal: 6.0 €");
	}

	@Test
	@GUITest
	@DisplayName("'Add' button should show error when buy was not possible")
	void testAddButtonInShoppingShowsError() {
		String orderId = "5";
		int quantity = 3;
		totemView.setOrderId(orderId);
		String errorMessage = "Error message";
		Product product = new Product("Pasta", 2);

		GuiActionRunner.execute(() -> totemView.showShopping());
		GuiActionRunner.execute(() -> totemView.showAllProducts(Arrays.asList(product)));

		doThrow(new TransactionException(errorMessage)).when(shoppingService).buyProduct(orderId, product.getId(),
				quantity);

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(quantity));
		window.button(JButtonMatcher.withText("Add")).click();

		window.label("messageLabel").requireText(errorMessage);

		GuiActionRunner.execute(() -> totemView.showOrder());
		assertThat(window.list("cartList").contents()).isEmpty();
	}

	@Test
	@GUITest
	@DisplayName("'Remove selected' button in Cart should remove from cart")
	void testRemoveSelectedButtonCart() {
		Product product = new Product("Pasta", 2);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), 3);

		GuiActionRunner.execute(() -> totemView.showOrder());
		GuiActionRunner.execute(() -> totemView.showAllOrderItems(Arrays.asList(orderItem)));

		window.list("cartList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove selected")).click();

		window.label("cartMessageLabel").requireText("Removed all Pasta");
		assertThat(window.list("cartList").contents()).isEmpty();
	}

	@Test
	@GUITest
	@DisplayName("'Remove selected' button in Cart should show error when remove is not possibile")
	void testRemoveSelectedButtonCartShowsError() {
		int quantity = 3;
		int price = 2;
		String orderId = "1";
		Product product = new Product("Pasta", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantity);
		String errorMessage = "Error message";

		doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteItem(any(OrderItem.class));
		when(shoppingService.getAllProducts()).thenReturn(Arrays.asList(product));
		when(shoppingService.getOrderItems(orderId)).thenReturn(Arrays.asList(orderItem));

		totemView.setOrderId(orderId);
		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});

		window.list("cartList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove selected")).click();

		window.label("cartMessageLabel").requireText(errorMessage);
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.0 €");
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents())
				.containsExactly("Pasta - Quantity: 3 - Price: 2.0 € - Subtotal: 6.0 €");

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Remove selected' button in Cart should show error and not reload view if there are other errors")
	void testRemoveSelectedButtonCartShowsErrorAndThereAreOtherErrors() {
		int quantity = 3;
		int price = 2;
		String orderId = "1";
		Product product = new Product("Pasta", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantity);
		String errorMessage = "Error message";

		doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteItem(any(OrderItem.class));
		doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();

		totemView.setOrderId(orderId);
		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});

		window.list("cartList").selectItem(0);
		window.button(JButtonMatcher.withText("Remove selected")).click();

		window.label("cartMessageLabel").requireText(errorMessage);
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).isEmpty();
		;
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Return quantity' button in Cart should return a quantity of item")
	void testReturnQuantityButtonCart() {
		int quantityInserted = 3;
		int quantityToRemove = 2;
		Product product = new Product("Pasta", 2);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantityInserted);
		OrderItem returnedItem = new OrderItem(product, new Order(OrderStatus.OPEN),
				quantityInserted - quantityToRemove);
		when(shoppingService.returnItem(orderItem, quantityToRemove)).thenReturn(returnedItem);

		GuiActionRunner.execute(() -> totemView.showOrder());
		GuiActionRunner.execute(() -> totemView.showAllOrderItems(Arrays.asList(orderItem)));

		window.list("cartList").selectItem(0);
		window.spinner("cartReturnSpinner").enterText(String.valueOf(quantityToRemove));
		window.button(JButtonMatcher.withText("Return quantity")).click();

		window.label("cartMessageLabel").requireText("Removed 2 Pasta");

		assertThat(window.list("cartList").contents())
				.containsExactly("Pasta - Quantity: 1 - Price: 2.0 € - Subtotal: 2.0 €");
	}

	@Test
	@GUITest
	@DisplayName("'Return quantity' button in Cart should show error and reload view if return is not possible")
	void testReturnQuantityButtonCartShowsError() {
		int quantityInserted = 3;
		int quantityToRemove = 2;
		int price = 2;
		String orderId = "1";
		Product product = new Product("Pasta", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantityInserted);
		String errorMessage = "Error message";

		doThrow(new TransactionException(errorMessage)).when(shoppingService).returnItem(any(OrderItem.class),
				anyInt());
		when(shoppingService.getAllProducts()).thenReturn(Arrays.asList(product));
		when(shoppingService.getOrderItems(orderId)).thenReturn(Arrays.asList(orderItem));

		totemView.setOrderId(orderId);
		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});

		window.list("cartList").selectItem(0);
		window.spinner("cartReturnSpinner").enterText(String.valueOf(quantityToRemove));
		window.button(JButtonMatcher.withText("Return quantity")).click();

		window.label("cartMessageLabel").requireText(errorMessage);
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.0 €");
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents())
				.containsExactly("Pasta - Quantity: 3 - Price: 2.0 € - Subtotal: 6.0 €");

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Return quantity' button in Cart should show error and not reload view if there are other errors")
	void testReturnQuantityButtonCartShowsErrorAndThereAreOtherErrors() {
		int quantityInserted = 3;
		int quantityToRemove = 2;
		int price = 2;
		String orderId = "1";
		Product product = new Product("Pasta", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantityInserted);
		String errorMessage = "Error message";

		doThrow(new TransactionException(errorMessage)).when(shoppingService).returnItem(any(OrderItem.class),
				anyInt());
		doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();

		totemView.setOrderId(orderId);
		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});

		window.list("cartList").selectItem(0);
		window.spinner("cartReturnSpinner").enterText(String.valueOf(quantityToRemove));
		window.button(JButtonMatcher.withText("Return quantity")).click();

		window.label("cartMessageLabel").requireText(errorMessage);
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).isEmpty();
		;
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Checkout' button in cart should reset view and show goodbye")
	void testCheckoutButtonInCart() {
		GuiActionRunner.execute(() -> totemView.showOrder());

		String orderId = "1";
		Product product = new Product("Pasta", 2);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), 4);
		totemView.setOrderId(orderId);
		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});

		window.button(JButtonMatcher.withText("Checkout")).click();

		window.panel("byePane").requireVisible();
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).isEmpty();
		;
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();
		softly.assertThat(totemView.getOrderId()).isNull();
		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("'Checkout' button in cart should show error if it is not possible to checkout")
	void testCheckoutButtonInCartShowsError() {
		GuiActionRunner.execute(() -> totemView.showOrder());

		String orderId = "1";
		int price = 2;
		int quantity = 3;
		Product product = new Product("Pasta", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantity);
		String errorMessage = "Error Message";
		doThrow(new TransactionException(errorMessage)).when(shoppingService).closeOrder(orderId, asList(orderItem));

		totemView.setOrderId(orderId);
		GuiActionRunner.execute(() -> {
			totemView.showOrder();
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});

		window.button(JButtonMatcher.withText("Checkout")).click();

		window.panel("cartPane").requireVisible();
		GuiActionRunner.execute(() -> totemView.showShopping());
		softly.assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.0 €");
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents())
				.containsExactly("Pasta - Quantity: 3 - Price: 2.0 € - Subtotal: 6.0 €");
		softly.assertThat(totemView.getOrderId()).isEqualTo(orderId);
		softly.assertAll();
	}
	
	@Test
	@GUITest
	@DisplayName("'Start Shopping' button in goodbye should set orderId, open shopping panel, show products and reset orderItems")
	void testStartShoppingButtonInGoodbye() {
		GuiActionRunner.execute(() -> totemView.showGoodbye());
		String orderId = "1";
		double price = 2;
		int quantity = 3;
		Order order = new Order(OrderStatus.OPEN);
		order.setId(orderId);
		
		Product product = new Product("Pizza", price);
		OrderItem orderItem = new OrderItem(product, new Order(OrderStatus.OPEN), quantity);
		GuiActionRunner.execute(() -> {
			totemView.showAllProducts(asList(product));
			totemView.showAllOrderItems(asList(orderItem));
		});
		when(shoppingService.saveOrder(any(Order.class))).thenReturn(order);
		when(shoppingService.getAllProducts()).thenReturn(Arrays.asList(new Product("Pasta", 2.5)));

		window.button("goodbyeStartShopping").click();

		window.panel("shoppingPane").requireVisible();
		softly.assertThat(totemView.getOrderId()).isEqualTo(orderId);
		softly.assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.5 €");
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}

	@Test
	@GUITest
	@DisplayName("''Start Shopping' button in goodbye should not show products and order items and should show error when saveOrder throws")
	void testStartShoppingButtonInGoodbyeShouldNotShowProductsAndShouldShowErrorWhenSaveOrderThrows() {
		GuiActionRunner.execute(() -> totemView.showGoodbye());

		String errorMessage = "Error message";
		doThrow(new TransactionException(errorMessage)).when(shoppingService).saveOrder(any(Order.class));

		window.button("goodbyeStartShopping").click();

		softly.assertThat(window.list("productList").contents()).isEmpty();
		window.label("messageLabel").requireText(errorMessage);
		window.panel("shoppingPane").requireVisible();
		GuiActionRunner.execute(() -> totemView.showOrder());
		softly.assertThat(window.list("cartList").contents()).isEmpty();

		softly.assertAll();
	}
}
