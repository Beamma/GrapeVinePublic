Feature: U11 - As Kaia, I want to record the different plants in my garden so I can keep track of the plants I have.

  Scenario: AC1 - I can access the add plan form from the garden page
    Given I am on a garden details page for a garden I own
    When I click add new plant button
    Then I see an add plant form

  Scenario Outline: AC2 - I can add valid plants
    Given I am on the add plant form
    And I enter the name <name>, count <count>, description <description>, and planted-on date <date>
    When I submit the add plant form
    Then a new plant record is added to the garden,
    And I am taken back to the garden details page.
    Examples:
      | name   | count | description         | date         |
      | "name" | ""    | ""                  | ""           |
      | "name" | "1"   | ""                  | ""           |
      | "name" | "1"   | "Plant description" | ""           |
      | "name" | "1"   | "Plant description" | "2023-11-18" |

  Scenario Outline: AC3 - I cannot enter invalid names
    Given I am on the add plant form
    And I enter the name <name>, count "1", description "description", and planted-on date "2023-11-18"
    When I submit the add plant form
    Then I see an error: "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes" for the field: "nameError"
    Examples:
      | name         |
      | "@@@"        |
      | "@handle"    |
      | "<>{}"       |
      | "$%^*@*(@(@" |


  Scenario: AC4 - I cannot enter descriptions longer than 512 characters
    Given I am on the add plant form
    And I enter the name "name", count "1", description "longDescription...", and planted-on date "2023-11-18"
    When I submit the add plant form
    Then I see an error: "Plant description must be less than 512 characters" for the field: "descriptionError"

  Scenario Outline: AC5 - I cannot enter invalid counts
    Given I am on the add plant form
    And I enter the name "Name", count <count>, description "description", and planted-on date "2023-11-18"
    When I submit the add plant form
    Then I see an error: "Plant count must be a positive whole number" for the field: "countError"
    Examples:
      | count         |
      | "@@@"        |
      | "number"    |
      | "six"       |
      | "1..5" |
      | "-3" |
      | "+3" |
      | "1.5" |
      | ".5" |
      | "0.5" |
      | "1,5" |
      | ",5" |
      | "0,5" |

  Scenario Outline: AC6 - I cannot enter invalid planted dates
    Given I am on the add plant form
    And I enter the name "Name", count "1", description "description", and planted-on date <date>
    When I submit the add plant form
    Then I see an error: "Date is not in valid format, DD/MM/YYYY" for the field: "dateError"
    Examples:
      | date |
      | "date" |
      | "2023/08/01" |
      | "2023-8-1" |
      | "1-1-2021" |
      | "1/1/2021" |
      | "01-01-2021" |
      | "01/01/2021" |

  Scenario: AC7 - I can add valid plants
    Given I am on the add plant form
    And I enter the name "Name", count "1", description "description", and planted-on date "2023-11-11"
    When I cancel the add plant form
    Then no new plant record is added to the garden,
    And I go back to the garden details page.