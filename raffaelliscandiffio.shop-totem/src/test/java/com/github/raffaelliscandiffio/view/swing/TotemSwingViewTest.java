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

@DisplayName("Test Totem View")
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
	@DisplayName("Test the change of panels")
	class PanelShowTest {

		@Test
		@DisplayName("Method 'showShopping' should replace any panel with shopping panel")
		void testShowShoppingShouldChangePanelToShoppingPanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "welcome"));
			GuiActionRunner.execute(() -> totemSwingView.showShopping());
			assertThat(totemSwingView.getShoppingPane().isShowing()).isTrue();
			assertThat(totemSwingView.getWelcomePane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
		}

		@Test
		@DisplayName("Method 'showWelcome' should replace any panel with welcome panel")
		void testShowWelcomeShouldChangePanelToWelcomePanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
			GuiActionRunner.execute(() -> totemSwingView.showWelcome());
			assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
		}

		@Test
		@DisplayName("Method 'showCart' should replace any panel with cart panel")
		void testShowCartShouldChangePanelToCartPanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
			GuiActionRunner.execute(() -> totemSwingView.showOrder());
			assertThat(totemSwingView.getWelcomePane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
		}
	}

	@Nested
	@DisplayName("Test Welcome panel")
	class WelcomePanelTest {

		@Test
		@DisplayName("Welcome panel should be the only visible panel at start")
		void testWelcomePanelShouldBeInitialPanel() {
			assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
		}

		@Test
		@GUITest
		@DisplayName("Welcome panel initial state")
		void testWelcomePanelInitialState() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "welcome"));
			window.button(JButtonMatcher.withText("Start shopping")).requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Start Shopping' should notify totem controller to show all products")
		void testStartShoppingButtonShouldNotifyTotemControllerToStartShopping() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "welcome"));

			window.button(JButtonMatcher.withText("Start shopping")).click();
			verify(totemController).startShopping();
		}

	}

	@Nested
	@DisplayName("Test Shopping panel")
	class ShoppingTest {

		@BeforeEach
		void setup() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
		}

		@Test
		@GUITest
		@DisplayName("Shopping panel inital state")
		void testShoppingPanelInitialState() {
			window.button(JButtonMatcher.withName("shopBtnCancelShopping")).requireText("Cancel Shopping")
					.requireEnabled();
			window.button(JButtonMatcher.withText("Cart")).requireEnabled();
			window.label("messageLabel").requireText(" ");
			window.list("productList");
			window.label(JLabelMatcher.withText("Quantity:"));
			window.spinner("quantitySpinner").requireDisabled();
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Method 'ShowAllProducts' should add the received products to shopping panel")
		void testShowAllProductsShouldAddProductsFromTheListToShoppingPanel() {
			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);
			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));

			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €", "Product2 - Price: 3.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Button 'Cancel Shopping' should notify totem controller to cancel shopping")
		void testCancelShoppingButtonShouldNotifyTotemControllerToCloseShopping() {
			window.button(JButtonMatcher.withName("shopBtnCancelShopping")).click();
			verify(totemController).cancelShopping();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Open Cart' should notify totem controller to open the cart panel")
		void testOpenCartButtonShouldNotifyTotemControllerToShowCart() {
			window.button(JButtonMatcher.withText("Cart")).click();
			verify(totemController).openOrder();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' and quantity spinner should be enabled only when a Product is selected")
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
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is a negative number")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsNegative() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("-1");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is not a number")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsNaN() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("text");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is zero")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsZero() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("0");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is invalid and enabled when becomes valid")
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
		@DisplayName("Message label should display 'Invalid quantity' only when quantity inserted is invalid")
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
		@DisplayName("Message label should not display 'Invalid quantity' after deselection if quantity inserted was invalid before")
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
		@DisplayName("Button 'Add' should notify totem controller to buy selected Product with quantity")
		void testAddButtonShouldNotifyTotemControllerBuySpecifiedProductWithQuantity() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterTextAndCommit("3");

			window.button(JButtonMatcher.withText("Add")).click();

			verify(totemController).buyProduct(product, 3);
		}

		@Test
		@GUITest
		@DisplayName("Method 'showErrorMessage' should set message label with given message")
		void testShowErrorMessageShouldSetMessageLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showErrorMessage("Error message"));
			window.label("messageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showMessage' should set message label with given message")
		void testShowMessageShouldSetMessageLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showMessage("Message"));
			window.label("messageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showErrorProductNotFound' should set error message label and delete product from list")
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
		@DisplayName("Method 'showWarning' should set message label with given message")
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
	@DisplayName("Test Cart panel")
	class CartPanelTest {

		@BeforeEach
		void setup() {
			GuiActionRunner.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart"));
		}

		@Test
		@GUITest
		@DisplayName("Cart panel inital state")
		void testCartPanelInitialState() {
			window.button(JButtonMatcher.withText("Continue Shopping")).requireEnabled();
			window.button(JButtonMatcher.withName("cartBtnCancelShopping")).requireText("Cancel Shopping")
					.requireEnabled();
			window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			window.button(JButtonMatcher.withText("Remove selected")).requireDisabled();
			window.button(JButtonMatcher.withText("Checkout")).requireDisabled();
			window.label("cartMessageLabel").requireText(" ");
			window.label(JLabelMatcher.withText("Remove selected item"));
			window.label(JLabelMatcher.withText("Quantity"));
			window.spinner("cartReturnSpinner").requireDisabled();
			window.list("cartList");
		}

		@Test
		@GUITest
		@DisplayName("Button 'Continue Shopping' should notify totem controller to show the shopping panel")
		void testContinueShoppingButtonShouldNotifyTotemControllerToShowShoppingPanel() {
			window.button(JButtonMatcher.withText("Continue Shopping")).click();
			verify(totemController).openShopping();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Cancel Shopping' should notify totem controller to cancel shopping")
		void testCancelShoppingButtonShouldNotifyTotemControllerToCancelShopping() {
			window.button(JButtonMatcher.withName("cartBtnCancelShopping")).click();
			verify(totemController).cancelShopping();
		}

		@Test
		@GUITest
		@DisplayName("Method 'itemAdded' should add the received OrderItem element to the cart list")
		void testItemAddedShouldAddTheOrderItemToTheCartList() {
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
		@DisplayName("Method 'itemModified' should update the specified item in the cart list")
		void testItemModifiedShouldUpdateTheOldOrderItemWithTheNewOneInCartList() {
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product("Product1", 2), 5)));
			Product product2 = new Product("Product2", 3);
			OrderItem oldOrderItem = new OrderItem(product2, 4);
			GuiActionRunner
					.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(oldOrderItem));
			OrderItem newOrderItem = new OrderItem(product2, 5);
			GuiActionRunner.execute(() -> totemSwingView.itemModified(oldOrderItem, newOrderItem));
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 € - Quantity: 5",
					"Product2 - Price: 3.0 € - Quantity: 5");
		}

		@Test
		@GUITest
		@DisplayName("Method 'clearOrderList' should remove all items from the cart list")
		void testClearOrderListShouldClearTheCartList() {
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
