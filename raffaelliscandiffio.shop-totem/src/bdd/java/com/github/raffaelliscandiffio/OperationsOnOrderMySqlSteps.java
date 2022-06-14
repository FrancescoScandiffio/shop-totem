package com.github.raffaelliscandiffio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.swing.JFrame;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import com.github.raffaelliscandiffio.app.swing.App;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;

public class OperationsOnOrderMySqlSteps {

	private static EntityManager entityManager;

	private FrameFixture window;

	private final int insertedQuantity = 10;
	private final int toRemoveQuantity = 3;
	private final String productName1 = "pizza";
	private final int productPrice1 = 4;
	private final int productQuantity1 = 100;
	private Product product1;
	private Stock stock1;

	@BeforeStories
	public void setUpStories() {
		Map<String, String> settings = new HashMap<>();
		settings.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/totem");
		settings.put("javax.persistence.jdbc.user", "root");
		settings.put("javax.persistence.jdbc.password", "");
		entityManager = App.getEntityManager(settings);
	}

	@BeforeScenario
	public void setUp() {
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.getTransaction().commit();

		product1 = new Product(productName1, productPrice1);
		stock1 = new Stock(product1, productQuantity1);
	}

	@AfterScenario
	public void tearDown() {
		if (window != null) {
			window.cleanUp();
		}
	}

	@AfterStories
	public void tearDownStories() {
		App.closeConnection();
	}

	@Given("The View is shown")
	public void givenViewIsShown() {
		String[] args = { "--database", "mysql" };
		App.main(args);
		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Totem".equals(frame.getTitle()) && frame.isShowing();
			}

		}).using(BasicRobot.robotWithCurrentAwtHierarchy());
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			window.requireVisible();
		});

	}

	@Given("The Database starts empty")
	public void givenDatabaseStartsEmpty() {
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Stock").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.getTransaction().commit();
	}

	@When("The Database contains few products and stocks")
	@Given("The Database contains few products and stocks")
	public void databaseContainsFewProductsAndStocks() {

		addProductToDataBase(product1);
		addStockToDataBase(stock1);
	}

	@When("The user clicks $buttonName button")
	public void whenTheUserClicksAButton(String buttonName) {
		window.button(buttonName).click();
	}

	@When("The user clicks on product")
	public void whenTheUserClicksOnProduct() {
		window.list("productList").selectItem(0);
	}

	@When("The view $viewName is visible")
	@Then("The view $viewName is visible")
	public void whenTheViewIsVisible(String viewName) {
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
			window.panel(viewName).requireVisible();
		});
	}

	@When("The user enters a quantity to buy")
	public void whenUserEntersAQuantityToBuy() {
		window.spinner("quantitySpinner").enterText(String.valueOf(insertedQuantity));
	}

	@Then("Cart list contains new item")
	public void thenCartListContainsNewItem() {

		String content = createRowForProducts(product1, insertedQuantity);
		assertThat(window.list("cartList").contents()).containsExactlyInAnyOrder(content);
	}

	@Then("Cart list contains item with double quantity")
	public void thenCartListContainsItemWithDoubleQuantity() {
		String content = createRowForProducts(product1, insertedQuantity * 2);
		assertThat(window.list("cartList").contents()).containsExactlyInAnyOrder(content);
	}

	@When("The user clicks on cart item")
	public void whenTheUserClicksOnCartItem() {
		window.list("cartList").selectItem(0);
	}

	@When("The user enters a quantity to remove")
	public void whenTheUserEntersAQuantityToRemove() {
		window.spinner("cartReturnSpinner").enterText(String.valueOf(toRemoveQuantity));
	}

	@Then("Cart list is empty")
	public void thenCartListIsEmpty() {
		assertThat(window.list("cartList").contents()).isEmpty();
	}

	@Then("Cart list contains item with removed quantity")
	public void thenCartListContainsItemWithRemovedQuantity() {
		String content = createRowForProducts(product1, insertedQuantity - toRemoveQuantity);
		assertThat(window.list("cartList").contents()).containsExactlyInAnyOrder(content);
	}
	
	@Then("The shopping list contains products")
	public void thenTheShoppingListContainsProducts() {
		String content = shoppingRow(product1);
		assertThat(window.list("productList").contents()).containsExactlyInAnyOrder(content);
	}

	@Given("The Database contains a product out of stock")
	public void thenDatabaseContainsAProductOutOfStock() {
		addProductToDataBase(product1);
		stock1.setQuantity(0);
		addStockToDataBase(stock1);

	}

	private void addProductToDataBase(Product product) {
		entityManager.getTransaction().begin();
		entityManager.persist(product);
		entityManager.getTransaction().commit();
	}

	private void addStockToDataBase(Stock stock) {
		entityManager.getTransaction().begin();
		entityManager.persist(stock);
		entityManager.getTransaction().commit();
	}

	private String createRowForProducts(Product product, int quantity) {
		return product1.getName() + " - Quantity: " + quantity + " - Price: " + product1.getPrice() + " € - Subtotal: "
				+ quantity * product1.getPrice() + " €";
	}
	
	private String shoppingRow(Product product) {
		return product.getName() + " - Price: " + product.getPrice() + " €";
	}

}
