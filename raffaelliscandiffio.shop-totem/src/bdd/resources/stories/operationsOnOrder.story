Narrative:
Stories to test The ShopTotem application

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


