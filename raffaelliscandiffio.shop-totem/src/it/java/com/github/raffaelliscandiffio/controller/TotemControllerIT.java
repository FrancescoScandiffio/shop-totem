package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.github.raffaelliscandiffio.utils.GUITestExtension;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;

@ExtendWith(GUITestExtension.class)
class TotemControllerIT {

	private ProductRepository productRepository;

	private StockRepository stockRepository;

	private PurchaseBroker broker;

	private TotemController totemController;

	private TotemSwingView totemView;

	private FrameFixture window;

	private Order order;

	@BeforeAll
	static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() {

		productRepository = mock(ProductRepository.class);
		stockRepository = mock(StockRepository.class);
		broker = new PurchaseBroker(productRepository, stockRepository);
		order = mock(Order.class);
		GuiActionRunner.execute(() -> {
			totemView = new TotemSwingView();
			totemController = new TotemController(broker, totemView, null);
			totemController.setOrder(order);
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
	@DisplayName("'welcomeStartShopping' button should open shopping panel and show products")
	void testStartShoppingButtonInWelcome() {
		when(productRepository.findAll()).thenReturn(asList(new Product(1, "Pasta", 2.5)));

		window.button("welcomeStartShopping").click();

		assertThat(window.list("productList").contents()).containsExactly("Pasta - Price: 2.5 €");
		window.panel("shoppingPane").requireVisible();
	}

	@Test
	@GUITest
	@DisplayName("'Add' button product null not found error")
	void testAddErrorProductNullNotFound() {
		int requestedQuantity = 20;
		GuiActionRunner.execute(() -> totemView.showShopping());

		GuiActionRunner.execute(() -> totemController.buyProduct(null, requestedQuantity));

		window.label("messageLabel").requireText("Product not found");
	}

	@Test
	@GUITest
	@DisplayName("'Add' button negative quantity error")
	void testAddErrorNegativeQuantity() {
		int requestedQuantity = -1;
		Product product = new Product(1, "Pasta", 2.5);
		GuiActionRunner.execute(() -> totemView.showShopping());

		GuiActionRunner.execute(() -> totemController.buyProduct(product, requestedQuantity));

		window.label("messageLabel").requireText("Buy quantity must be positive: received " + requestedQuantity);
	}

	@Test
	@GUITest
	@DisplayName("'Add' button product not found error")
	void testAddErrorProductNotFound() {
		int requestedQuantity = 20;
		double price = 2.5;
		Product product1 = new Product(1, "Pasta", price);
		Product product2 = new Product(2, "Pizza", price);

		GuiActionRunner.execute(() -> {
			totemView.showShopping();
			totemView.showAllProducts(asList(product1, product2));
		});
		when(productRepository.findById(1)).thenReturn(null);

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(requestedQuantity));
		window.button(JButtonMatcher.withText("Add")).click();

		window.label("messageLabel").requireText("Product not found");
		assertThat(window.list("productList").contents()).containsExactly("Pizza - Price: 2.5 €");
	}

	@Test
	@GUITest
	@DisplayName("'Add' button product out of stock error when stock not found")
	void testAddErrorProductNotFoundIsOutOfStock() {
		int requestedQuantity = 20;
		double price = 2.5;
		Product product1 = new Product(1, "Pasta", price);
		Product product2 = new Product(2, "Pizza", price);

		GuiActionRunner.execute(() -> {
			totemView.showShopping();
			totemView.showAllProducts(asList(product1, product2));
		});
		when(productRepository.findById(1)).thenReturn(product1);
		when(stockRepository.findById(1)).thenReturn(null);

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(requestedQuantity));
		window.button(JButtonMatcher.withText("Add")).click();

		window.label("messageLabel").requireText("Item out of stock");
		assertThat(window.list("productList").contents()).containsExactly("Pizza - Price: 2.5 €");
	}

	@Test
	@GUITest
	@DisplayName("'Add' button product out of stock warning")
	void testAddWarningProductOutOfStock() {
		int requestedQuantity = 20;
		double price = 2.5;
		Product product = new Product(1, "Pasta", price);

		GuiActionRunner.execute(() -> {
			totemView.showShopping();
			totemView.showAllProducts(asList(product));
		});
		when(productRepository.findById(1)).thenReturn(product);
		when(stockRepository.findById(1)).thenReturn(new Stock(1, 0));

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(requestedQuantity));
		window.button(JButtonMatcher.withText("Add")).click();

		window.label("messageLabel").requireText("Item out of stock: Pasta");
	}

	@Test
	@GUITest
	@DisplayName("'Add' button when product is not already in order")
	void testAddWhenProductNotInOrder() {
		int requestedQuantity = 20;
		int availableQuantity = 100;
		double price = 2.5;
		Product product = new Product(1, "Pasta", price);

		GuiActionRunner.execute(() -> {
			totemView.showShopping();
			totemView.showAllProducts(asList(product));
		});
		when(productRepository.findById(1)).thenReturn(product);
		when(stockRepository.findById(1)).thenReturn(new Stock(1, availableQuantity));
		when(order.findItemByProduct(product)).thenReturn(null);
		OrderItem orderItem = new OrderItem(product, requestedQuantity, requestedQuantity * price);
		when(order.addNewProduct(product, requestedQuantity)).thenReturn(orderItem);

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(requestedQuantity));
		window.button(JButtonMatcher.withText("Add")).click();

		GuiActionRunner.execute(() -> totemView.showOrder());
		assertThat(window.list("cartList").contents()).containsExactly("Pasta - Quantity: " + requestedQuantity
				+ " - Price: " + price + " € - Subtotal: " + requestedQuantity * price + " €");
	}

	@Test
	@GUITest
	@DisplayName("'Add' button when product is in order")
	void testAddWhenProductIsInOrder() {
		int currentOrderQuantity = 30;
		int requestedQuantity = 20;
		int newQuantity = currentOrderQuantity + requestedQuantity;
		int availableQuantity = 100;
		double price = 2.5;
		Product product = new Product(1, "Pasta", price);
		OrderItem storedItem = new OrderItem(product, currentOrderQuantity, currentOrderQuantity * price);
		OrderItem modifiedItem = new OrderItem(product, newQuantity, newQuantity * price);
		GuiActionRunner.execute(() -> {
			totemView.showShopping();
			totemView.showAllProducts(asList(product));
			totemView.itemAdded(storedItem);
		});
		when(productRepository.findById(1)).thenReturn(product);
		when(stockRepository.findById(1)).thenReturn(new Stock(1, availableQuantity));
		when(order.findItemByProduct(product)).thenReturn(storedItem);
		when(order.increaseProductQuantity(product, requestedQuantity)).thenReturn(modifiedItem);

		window.list("productList").selectItem(0);
		window.spinner("quantitySpinner").enterText(String.valueOf(requestedQuantity));
		window.button(JButtonMatcher.withText("Add")).click();

		GuiActionRunner.execute(() -> totemView.showOrder());
		assertThat(window.list("cartList").contents()).containsExactly("Pasta - Quantity: " + newQuantity + " - Price: "
				+ price + " € - Subtotal: " + newQuantity * price + " €");
	}

}
