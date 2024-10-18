Feature: U20 - As Inaya, I want to be able to browse gardens that other users have made public so that I can learn from what other gardeners are doing.

  Scenario: AC1 - Logged in users can see plants in public gardens
    Given a garden has been marked as public
    And I am logged in
    When I view the garden
    Then I can see the details of the plants in the garden

  Scenario: AC2 - Browse gardens page exists
    Given I am anywhere on the system
    When I click on the browse gardens link
    Then I am taken to the browse gardens page

  Scenario Outline: AC3 - Users can search with search button
    Given I fill the search field with <search>
    And Gardens exist with the word <search> in the title
    When I click on the search button
    Then I see a list of gardens with the word <search> in the title
    Examples:
      | search |
      | "roses"  |
      | "tulips" |
      | "lilies" |

  Scenario Outline: AC4 - User can search with enter key
    Given I fill the search field with <search>
    And Gardens exist with the word <search> in the title
    When I press the enter key
    Then I see a list of gardens with the word <search> in the title
    Examples:
      | search |
      | "roses"  |
      | "tulips" |
      | "lilies" |

  Scenario: AC5 - Search can return no results
    Given I fill the search field with "no results"
    And No gardens exist with that search term
    When I click on the search button
    Then I see a message saying "No gardens match your search"

  Scenario: AC6 - Search results are paginated
    Given I search for a term that returns more than 9 results
    When I click on the search button
    Then I see the first 9 results

  Scenario: AC7 - First page button exists
    Given I am on any page of results
    When I click on the first page button
    Then I am taken to the first page of results

  Scenario: AC8 - Last page button exists
    Given I am on any page of results
    When I click on the last page button
    Then I am taken to the last page of results

  Scenario: AC9 - User cannot go to a page that does not exist
    Given I am on the last page of results
    When I click on the next page button
    Then I am taken to the last page of results

  Scenario: AC10 - Two page numbers exist in both directions
    Given I am on any page of results
    Then I see two page numbers in both directions

  Scenario: AC11 - User can select a page of results
    Given I am on the first page of results
    When I click on the 2 page button
    Then I am taken to that page of results

  Scenario: AC12 - User can see the number of results
    Given I am on any page of results
    Then I see the number of results