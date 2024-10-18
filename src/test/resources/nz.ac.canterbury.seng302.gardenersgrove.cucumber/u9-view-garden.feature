Feature: U9 As Kaia,  I want to see information about my garden so that I can keep the details accurate.

  Scenario: AC1 - Clicking my garden's name redirects me to the garden information page
    Given I am logged into Gardeners Grove
    And I have a garden called "My Garden" at "20 Kirkwood Avenue", "Christchurch", "New Zealand"
    When I click the Garden's button
    Then I am redirected to the details page of garden 1

  Scenario: AC2 - Clicking My Gardens redirects me to the list of my gardens
    Given I am logged into Gardeners Grove
    And I have a garden called "My Garden" at "20 Kirkwood Avenue", "Christchurch", "New Zealand"
    When I click the My Gardens button
    Then I am redirected to the My Gardens page

  Scenario: AC3 - I cannot edit details on a garden details page
    Given I am logged into Gardeners Grove
    And I have a garden called "My Garden" at "20 Kirkwood Avenue", "Christchurch", "New Zealand"
    When I am on the garden details page of "My Garden"
    Then I cannot edit any of the "My Garden" details

  Scenario: AC4 - I cannot access any other users' gardens
    Given I am logged into Gardeners Grove
    When I try to access garden with id 4
    Then I am shown the error "Forbidden, you do not own this garden" in the "error" field


  Scenario: AC5 - I am not logged in and cannot access any users' gardens
    Given I am not logged in
    When I try to access garden with id 1
    Then I am redirected to the login page


