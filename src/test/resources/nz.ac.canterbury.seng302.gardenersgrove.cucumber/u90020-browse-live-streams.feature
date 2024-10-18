Feature: As Kaia, I want to be able to browse live-streams that other users have started, so that I can learn from what other gardeners are doing.

  @U90020
  Scenario: AC1 - I can navigate to the browse livestreams page
    Given I am logged in and anywhere on the system
    When I hit the browse livestreams button
    Then I am taken to the browse livestreams page

  @U90020
  Scenario: AC2 - I can see information about active livestreams
    Given I am logged in and anywhere on the system
    When I hit the browse livestreams button and a streamer is active
    Then I am taken to the browse livestreams page

  @U90020
  Scenario: AC3 - More than 10 live streams
    Given There are 15 current live-streams
    When I got to the browse livestreams page
    Then I only see the first 10 livestreams

  # AC4 Manual Test

  # AC5 Manual Test

  @U90020
  Scenario Outline: AC6.1 - Invalid page number
    Given There are 15 current live-streams
    When I hit enter on the URL with a page number <page number>
    Then I am taken to the first page
    Examples:
      | page number |
      | ""          |
      | "0"         |
      | "-1"        |
      | "a"         |
      | " "         |
      | "!"         |

  @U90020
  Scenario: AC6.2 - page number too high
    Given There are 15 current live-streams
    When I hit enter on the URL with a page number "3"
    Then I am taken to the last page

  # AC7 Manual Test

  @U90020
  Scenario: AC8 Navigate to a page
    Given There are 15 current live-streams
    When I hit enter on the URL with a page number "2"
    Then I see 5 livestreams
    And it says I am on page 2

  @U90020
  Scenario: AC9 Shows results X of Y
    Given There are 15 current live-streams
    When I hit enter on the URL with a page number "2"
    Then I see the text saying Showing results 11 to 15 of 15

