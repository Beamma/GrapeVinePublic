Feature: U10 - As Kaia, I want to edit information about my garden so I can keep it up to date.

  Scenario: AC1 - Clicking the edit button redirects me to the edit garden page with prepopulated details.
    Given I am on the garden details page for a garden I own
    When I click the edit garden button
    Then I see the edit garden form with all the details prepopulated.

  Scenario Outline: AC2 - Editing the garden with valid details redirects me to the Garden page.
    Given I am on the edit garden form
    When I enter <name>, <description>, <size>, <city> and <country>
    And I click “Submit” on the edit garden form
    Then the garden details are updated with <name>, <description>, <size>, <city> and <country>
    And I am taken back to the Garden page
    Examples:
      | name                     | city                 | size  | country              | description          |
      | "garden name"            | "My house"           | "3"   | "Country"            | "Spēcïàl Çhārâctërs" |
      | "Spē-cï.àl Çh'ārâct3ërs" | "Spēcïàl Çhārâctërs" | "4.3" | "Spēcïàl Çhārâctërs" | ""                   |
      | "Cabbage patch"          | "My backyard"        | ""    | "Country"            | "Description"        |

  Scenario Outline: AC3.1 - Editing the garden with empty garden name gives error message.
    Given I am on the edit garden form
    When I enter <empty name>, "description", "11.4", "Auckland" and "New Zealand"
    And I click “Submit” on the edit garden form
    Then There is an error in the "gardenName" field saying "Garden name cannot be empty"
    Examples:
      | empty name |
      | ""         |
      | "     "    |
      | "     "    |


  Scenario Outline: AC3.2 - Editing the garden with invalid garden name gives error message.
    Given I am on the edit garden form
    When I enter <invalid name>, "description", "11.4", "Auckland" and "New Zealand"
    And I click “Submit” on the edit garden form
    Then There is an error in the "gardenName" field saying "Garden name must only include letters, numbers, spaces, dots, hyphens or apostrophes"
    Examples:
      | invalid name |
      | "@handle"    |
      | "<>{}"       |
      | "$%^*@*(@(@" |

  Scenario Outline: AC4.1 - Editing the garden with empty city or country gives error message.
    Given I am on the edit garden form
    When I enter "The Patch", "description", "11.4", <city> and <country>
    And I click “Submit” on the edit garden form
    Then There is an error in the "location" field saying "City and Country are required"
    Examples:
      | city   | country   |
      | ""     | "country" |
      | "City" | ""        |

  Scenario Outline: AC4.2 - Editing the garden with invalid location gives error message.
    Given I am on the edit garden form
    When I enter "The Patch", "description", "11.4", <city> and <country>
    And I click “Submit” on the edit garden form
    Then There is an error in the <error field> field saying <error message>
    Examples:
      | city   | country   | error field        | error message                                                                                   |
      | "@@@"  | "country" | "location.city"    | "City name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"    |
      | "city" | "@@@"     | "location.country" | "Country name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes" |


  Scenario Outline: AC5 - Editing the garden with invalid size gives error message.
    Given I am on the edit garden form
    When I enter "The Patch", "description", <invalid size>, "Auckland" and "New Zealand"
    And I click “Submit” on the edit garden form
    Then There is an error in the "size" field saying "Garden size must be a positive number"
    Examples:
      | invalid size |
      | "0"          |
      | "-1"         |
      | "-1.51"      |
      | "-1..51"     |
      | "+5"         |


  Scenario Outline: AC6 - Editing the garden with valid size details in european format.
    Given I am on the edit garden form
    When I enter "The Patch", "description", <european size>, "Auckland" and "New Zealand"
    And I click “Submit” on the edit garden form
    Then the garden details are updated with "The Patch", "description", <european size>, "Auckland" and "New Zealand"
    And I am taken back to the Garden page
    Examples:
      | european size |
      | "3,6"         |
      | ",3"          |
