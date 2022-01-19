package com.github.raffaelliscandiffio.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JSpinnerFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.utils.GUITestExtension;

@DisplayName("Totem View tests")
@ExtendWith(GUITestExtension.class)
@ExtendWith(MockitoExtension.class)
class TotemSwingViewTest {

	private FrameFixture window;

	private TotemSwingView totemSwingView;

	@Mock
	private TotemController totemController;
	
	@BeforeAll
	static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() {
		GuiActionRunner.execute(() -> {
			totemSwingView = new TotemSwingView();
			totemSwingView.setTotemController(totemController);
			return totemSwingView;
		});
		window = new FrameFixture(totemSwingView);
		window.show(); // shows the frame to test

	}

	@AfterEach
	void clean() throws Exception {
		window.cleanUp();
	}

	@Nested
	@DisplayName("Panel connection tests")
	class PanelConnectionTests {

		@Test
		@DisplayName("ShowShopping should replace any panel with shopping panel")
		void testShowShoppingShouldChangePanelToShoppingPanel() {

			GuiActionRunner.execute(() -> totemSwingView.showShopping());

			assertThat(totemSwingView.getWelcomePane().isShowing()).isFalse();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isTrue();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
		}

		@Test
		@GUITest
		@DisplayName("ShowWelcome should replace any panel with shopping panel")
		void testShowWelcomeShouldChangePanelToWelcomePanel() {

			GuiActionRunner.execute(() -> totemSwingView.showWelcome());

			assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
		}

		@Test
		@DisplayName("ShowCart should replace any panel with cart panel")
		void testShowCartShouldChangePanelToCartPanel() {

			GuiActionRunner.execute(() -> totemSwingView.showOrder());

			assertThat(totemSwingView.getWelcomePane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
		}
	}

	@Nested
	@DisplayName("Welcome panel tests")
	class WelcomePanelTests {

		@BeforeEach
		void setup() {

		}

		@Test
		@DisplayName("Welcome panel should be the visible panel at start")
		void testWelcomePanelShouldBeInitialPanel() {
			assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
		}

		@Test
		@GUITest
		@DisplayName("Welcome panel initial state")
		void testWelcomePanelInitialState() {

			GuiActionRunner.execute(() -> totemSwingView.showWelcome());
			window.button(JButtonMatcher.withText("Start shopping")).requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("Start Shopping button should delegate totem controller to show all products")
		void testStartShoppingButtonShouldDelegateToTotemControllerToStartShopping() {

			window.button(JButtonMatcher.withText("Start shopping")).click();
			verify(totemController).startShopping();
		}

	}

	@Nested
	@DisplayName("Shopping tests")
	class ShoppingTests {

		@BeforeEach
		void setup() {
			GuiActionRunner.execute(() -> totemSwingView.showShopping());
		}

		@Test
		@GUITest
		@DisplayName("Shopping panel inital state")
		void testShoppingPanelInitialState() {

			window.button(JButtonMatcher.withText("Cancel Shopping")).requireEnabled();
			window.button(JButtonMatcher.withText("Cart")).requireEnabled();
			window.label("messageLabel").requireText(" ");
			window.list("productList");
			window.label(JLabelMatcher.withText("Quantity:"));
			window.spinner("quantitySpinner").requireDisabled();
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("ShowAllProducts should add the products given to shopping panel")
		void testShowAllProductsShouldAddProductsFromTheListToShoppingPanel() {

			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);

			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));

			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €", "Product2 - Price: 3.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Cancel Shopping button should delegate totem controller to abort order")
		void testCancelShoppingButtonShouldDelegateToTotemControllerToCloseShopping() {

			window.button(JButtonMatcher.withText("Cancel Shopping")).click();

			verify(totemController).cancelShopping();
		}

		@Test
		@GUITest
		@DisplayName("Open Cart button should delegate totem controller to open the cart panel")
		void testOpenCartButtonShouldDelegateToTotemControllerToShowCart() {

			window.button(JButtonMatcher.withText("Cart")).click();

			verify(totemController).openOrder();
		}

		@Test
		@GUITest
		@DisplayName("Add button and quantity spinner should be enabled only when a Product is selected")
		void testAddButtonAndQuantitySpinnerShouldBeEnabledOnlyWhenAProductIsSelected() {

			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel()
					.addElement(new Product("Product1", 2)));

			window.list("productList").selectItem(0);
			JButtonFixture buttonAdd = window.button(JButtonMatcher.withText("Add"));
			JSpinnerFixture spinner = window.spinner("quantitySpinner");
			spinner.requireEnabled();
			buttonAdd.requireEnabled();

			window.list("productList").clearSelection();
			buttonAdd.requireDisabled();
			spinner.requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Add button should be disabled when quantity in spinner is a negative number")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsNegative() {

			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("-1");

			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Add button should be disabled when quantity in spinner is not a number")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsNaN() {

			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("text");

			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Add button should be disabled when quantity in spinner is zero")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsZero() {

			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("0");

			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Add button should be disabled when quantity in spinner is invalid and enabled when becomes valid")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsInvalidAndEnabledWhenTurnsValid() {

			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("text");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();

			window.spinner("quantitySpinner").enterText("2");
			window.button(JButtonMatcher.withText("Add")).requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("Quantity spinner shown value should be resetted to last valid value if an invalid value is inserted and the product is deselected")
		void testQuantitySpinnerShouldBeResettedToLastValidValueWhenQuantityIsInvalidAndProductIsDeselected() {

			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			// gets the spinner text field
			JFormattedTextField tf = ((JSpinner.DefaultEditor) window.spinner("quantitySpinner").target().getEditor())
					.getTextField();

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("2");
			window.spinner("quantitySpinner").enterText("text");

			window.list("productList").clearSelection();
			// asserts on the spinner current shown value
			assertThat(tf.getText()).isEqualTo("2");
		}

		@Test
		@GUITest
		@DisplayName("Message should display 'Invalid quantity' only when quantity inserted is invalid")
		void testMessageShouldDisplayInvalidQuantityErrorOnlyWhenQuantityIsInvalid() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("3");
			window.label("messageLabel").requireText(" ");

			window.spinner("quantitySpinner").enterText("-1");
			window.label("messageLabel").requireText("Invalid quantity");

			window.spinner("quantitySpinner").enterText("3");
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message should not display 'Invalid quantity' after deselection if quantity inserted was invalid before")
		void testMessageShouldNotDisplayInvalidQuantityWhenProductIsNotSelected() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);

			window.spinner("quantitySpinner").enterText("-1");
			window.label("messageLabel").requireText("Invalid quantity");

			window.list("productList").clearSelection();
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Add button should delegate totem controller to buy selected Product with quantity")
		void testAddButtonShouldDelegateToTotemControllerBuySpecifiedProductWithQuantity() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterTextAndCommit("3");

			window.button(JButtonMatcher.withText("Add")).click();

			verify(totemController).buyProduct(product, 3);
		}

		@Test
		@GUITest
		@DisplayName("ShowErrorMessage should set message label with given message")
		void testShowErrorMessageShouldSetMessageLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showErrorMessage("Error message"));

			window.label("messageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("ShowMessage should set message label with given message")
		void testShowMessageShouldSetMessageLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showMessage("Message"));

			window.label("messageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("ShowErrorProductNotFound should set error message label and delete product from list")
		void testShowErrorProductNotFoundShouldSetMessageLabelAndDeleteProductFromList() {

			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);
			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));

			GuiActionRunner.execute(() -> totemSwingView.showErrorProductNotFound("Error message", product2));

			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €");
			window.label("messageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("ShowWarning should set message label with given message")
		void testShowWarningShouldSetMessageLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showWarning("Message"));

			window.label("messageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be resetted to empty after Product deselection")
		void testMessageLabelShouldBeResettedWhenProductIsDeselected() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));

			window.list("productList").clearSelection();

			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be resetted to empty after Product change of selection")
		void testMessageLabelShouldBeResettedWhenProductIsChangedOfSelection() {

			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);
			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));

			window.list("productList").selectItem(1);

			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be resetted to empty after quantity change (to valid value)")
		void testMessageLabelShouldBeResettedWhenQuantityIsChangedToValidValue() {

			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));

			window.spinner("quantitySpinner").enterText("2");

			window.label("messageLabel").requireText(" ");
		}
	}

	@Nested
	@DisplayName("Order panel tests")
	class OrderPanelTests {

		@BeforeEach
		void setup() {
			GuiActionRunner.execute(() -> totemSwingView.showOrder());
		}

		@Test
		@GUITest
		@DisplayName("Cart panel inital state")
		void testCartPanelInitialState() {

			window.button(JButtonMatcher.withText("Go to Shopping")).requireEnabled();
			window.list("cartList");
		}
		
		@Test
		@GUITest
		@DisplayName("Go to Shopping button should delegate totem controller to open shopping panel")
		void testGoToShoppingButtonShouldDelegateToTotemControllerToShowShoppingPanel() {

			window.button(JButtonMatcher.withText("Go to Shopping")).click();

			verify(totemController).openShopping();
		}
		
		@Test
		@GUITest
		@DisplayName("ItemAdded adds the OrderItem element to the cart list")
		void testItemAddedAddsTheOrderItemToTheCartList() {
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product("Product1", 3), 5)));

			OrderItem newItem = new OrderItem(new Product("Product2", 2), 4);
			GuiActionRunner.execute(() -> totemSwingView.itemAdded(newItem));

			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 3.0 € - Quantity: 5",
					"Product2 - Price: 2.0 € - Quantity: 4");
		}

		@Test
		@GUITest
		@DisplayName("ItemModified swaps a given old order item with a new one to the cart list")
		void testItemModifiedSwapsAnOldOrderItemWithANewOneInCartList() {
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product("Product1", 2), 5)));

			Product product2 = new Product("Product2", 3);
			OrderItem oldOrderItem = new OrderItem(product2, 4);
			GuiActionRunner
					.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(oldOrderItem));

			OrderItem newOrderItem = new OrderItem(product2, 5);
			GuiActionRunner.execute(() -> totemSwingView.itemModified(oldOrderItem, newOrderItem));
			
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 € - Quantity: 5", "Product2 - Price: 3.0 € - Quantity: 5");
		}

		@Test
		@GUITest
		@DisplayName("Clear Order List removes all order items from cart list")
		void testClearOrderListEmptiesCartList() {

			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product("Product1", 2), 5)));
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product("Product2", 3), 4)));

			GuiActionRunner.execute(() -> totemSwingView.clearOrderList());
			
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).isEmpty();
		}

	}
}
