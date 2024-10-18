Feature: U9002 - As Inaya, I want to see a feed of recent posts so I can see what my community is doing

  @U9002
  Scenario: AC1 - I can navigate to the view feed page
    Given I am logged in to the system
    When I hit the view feed button
    Then I am taken to the post feed page

  @U9002
  Scenario: AC2 - I see information about the post
    Given I am viewing the Feed on page "1"
    When the page loads
    Then I can see information about posts

  @U9002
  Scenario: AC3 - I see posts ordered from newest to oldest
    Given I am viewing the Feed on page "1"
    When the page loads
    Then posts are displayed newest to oldest

  @U9002
  Scenario: AC4 - Feed page is paginated
    Given I am viewing the Feed on page "1"
    When there are more than 10 posts
    And the page loads
    Then the results are paginated with 10 posts per page

  @U9002
  Scenario: AC5 - First page exists
    Given I am viewing the Feed on page "2"
    When there are more than 20 posts
    And the page loads
    And I click the "first" page button underneath the results
    And the page loads
    Then I am taken to the "first" page

  @U9002
  Scenario: AC6 - Last page exists
    Given I am viewing the Feed on page "1"
    When there are more than 20 posts
    And the page loads
    And I click the "last" page button underneath the results
    And the page loads
    Then I am taken to the "last" page

  @U9002
  Scenario Outline: AC7 - User cannot go to a page that does not exist
    Given I am viewing the Feed on page "<page number>"
    When there are more than 20 posts
    And the page loads
    Then I am taken to the <page> page
    Examples:
      | page       | page number |
      | "first"    | -100        |
      | "last"     | 1000        |
      | "first"    | 0           |
      | "last"     | 3           |