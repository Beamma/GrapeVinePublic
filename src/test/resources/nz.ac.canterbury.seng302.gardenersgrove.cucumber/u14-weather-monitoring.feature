Feature: U14 - As Kaia, I want to update the plants I have in my garden so that I can add more details about them as I know more about them.

  Scenario: AC1 - Garden details page displays the current weather
    Given I am on the garden details page for my own garden
    Then the current weather for my location "Christchurch" is shown


  Scenario: AC2 - Garden details page displays the future weather
    Given I am on the garden details page for my own garden
    Then the future weather for my location "Christchurch" the future 3 days is shown


  Scenario: AC3 - Invalid location gives error
    Given I am on the garden details page for my own garden
    And the garden has a location "Bad Location" that can’t be found
    Then a error message tells me "Location not found, please update your location to see the weather"

  Scenario: AC4 - If there hasn't been rain recently, garden details page displays message “There hasn’t been any rain recently, make sure to water your plants if
  they need it”
    Given I am on the garden details page for my own garden
    And the garden has not had rain in the past 2 days
    Then a message tells me "There hasn’t been any rain recently, make sure to water your plants if they need it"

  Scenario: AC5 - If the current weather is rainy, the garden details page displays message "Outdoor plants don't need any water today"
    Given I am on the garden details page for my own garden
    And it is currently raining
    Then a message tells me "Outdoor plants don’t need any water today"

  Scenario: AC6 - Closing the weather alert is true for the current day
    Given I am on the garden details page for my own garden
    And the garden has had rain in the past 2 days
    And I click the close button on the weather alert
    Then the weather alert is hidden
    And the weather alert will not show until the next day

