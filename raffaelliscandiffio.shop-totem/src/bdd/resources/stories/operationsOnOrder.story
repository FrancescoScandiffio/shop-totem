Narrative:
Stories to test The ShopTotem application


Scenario: Shopping list is initialized correctly
Given The View is shown
And The Database starts empty
When The Database contains few products and stocks
And The user clicks welcomeStartShopping button
Then The shopping list contains products


Scenario: Buy product
Given The View is shown
And The Database starts empty
And The Database contains few products and stocks
When The user clicks welcomeStartShopping button
And The user clicks on product
And The user enters a quantity to buy
And The user clicks addButton button
And The user clicks cartButton button
And The view cartPane is visible
Then Cart list contains new item


Scenario: Buy product when already in cart
Given The View is shown
And The Database starts empty
And The Database contains few products and stocks
When The user clicks welcomeStartShopping button
And The view shoppingPane is visible
And The user clicks on product
And The user enters a quantity to buy
And The user clicks addButton button
And The user clicks addButton button
And The user clicks cartButton button
And The view cartPane is visible
Then Cart list contains item with double quantity


Scenario: Remove product from cart
Given The View is shown
And The Database starts empty
And The Database contains few products and stocks
When The user clicks welcomeStartShopping button
And The view shoppingPane is visible
And The user clicks on product
And The user enters a quantity to buy
And The user clicks addButton button
And The user clicks cartButton button
And The view cartPane is visible
And The user clicks on cart item
And The user clicks removeSelectedButton button
Then Cart list is empty


Scenario: Remove a quantity of The product from cart
Given The View is shown
And The Database starts empty
And The Database contains few products and stocks
When The user clicks welcomeStartShopping button
And The view shoppingPane is visible
And The user clicks on product
And The user enters a quantity to buy
And The user clicks addButton button
And The user clicks cartButton button
And The view cartPane is visible
And The user clicks on cart item
And The user enters a quantity to remove
And The user clicks returnQuantityButton button
Then Cart list contains item with removed quantity


Scenario: Do not buy a product out of stock
Given The View is shown
And The Database starts empty
And The Database contains a product out of stock
When The user clicks welcomeStartShopping button
And The view shoppingPane is visible
And The user clicks on product
And The user enters a quantity to buy
And The user clicks addButton button
And The user clicks cartButton button
And The view cartPane is visible
Then Cart list is empty


Scenario: Cancel shopping in cartPanel
Given The View is shown
And The Database starts empty
And The Database contains few products and stocks
When The user clicks welcomeStartShopping button
And The view shoppingPane is visible
And The user clicks on product
And The user enters a quantity to buy
And The user clicks addButton button
And The user clicks cartButton button
And The view cartPane is visible
And The user clicks cartBtnCancelShopping button
Then The view welcomePane is visible
