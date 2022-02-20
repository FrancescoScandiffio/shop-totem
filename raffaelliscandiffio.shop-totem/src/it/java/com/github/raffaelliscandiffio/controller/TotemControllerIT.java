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

}
