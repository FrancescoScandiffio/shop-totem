package com.github.raffaelliscandiffio.controller;

import static org.mockito.Mockito.mock;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.raffaelliscandiffio.model.Order;
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


}
