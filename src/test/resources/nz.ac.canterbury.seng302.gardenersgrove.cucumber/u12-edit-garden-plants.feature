Feature: U12 - As Kaia, I want to update the plants I have in my garden so that I can add more details about them as I know more about them.

  Scenario Outline: AC1 - When on the garden details page, There is a list of all recorded plants
    Given I am on the garden details page for a garden I own
    Then There is a list of all plants I have recorded in the garden with their <name>, a <default image>, and <count> and <description>
    Examples:
      | name     | default image   | count | description |
      | "Plant1" | "Image1"        | 1     | "Desc1"     |

  Scenario: AC3 - When I click the edit plant for a garden, then that garden is populated with that gardens information
    Given I am on the garden details page for a garden I own
    When I click on a Edit button next to each plant
    Then I see the edit plant form with all details of the plant pre-populated

  Scenario Outline: AC4 - When I create a valid plant, then the plant is added and Iam taken back to the garden details page
    Given I am on the edit plant form
    And I enter valid values for the <name> and optionally a <count>, <description>, and a planted-on <date>
    When I click submit
    Then the plant record is updated, and I am taken back to the garden details page <name> <count> <description> <date>
    Examples:
      | name | count  | description | date         |
      | "T1" | "99"   | "T99"       | "2024-01-01" |

  Scenario Outline: AC5 - Entering an invalid plant on plant create form
    Given I am on the edit plant form
    And I enter an empty or invalid <plant name>
    When I click submit
    Then An "nameError" message tells me "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes"
    Examples:
      | plant name |
      | ""         |
      | " "        |
      | "#"        |
      | "!"        |
      | "@"        |
      | "("        |

  Scenario: AC6 - Enter a description that is too long
    Given I am on the edit plant form
    And I enter a description that is longer than 512 characters
    When I click submit
    Then An "descriptionError" message tells me "Description must be less than 512 characters"

  Scenario Outline: AC7 - Enter invalid count
    Given I am on the edit plant form
    And I enter an invalid <count>
    When I click submit
    Then An "countError" message tells me "Plant count must be a positive whole number"
    Examples:
      | count |
      | "-1"  |
      | "0"   |
      | "a"   |
      | "0,1" |
      | "!"   |
      | "1.1" |

  Scenario Outline: AC8 - Invalid date
    Given I am on the edit plant form
    And I enter a <date> that is not in the Aotearoa NZ format
    When I click submit
    Then An "dateError" message tells me "Date in not valid format, DD-MM-YYYY"
    Examples:
      | date         |
      | "a"          |
      | "2024/01/01" |
      | "01-01-2024" |
      | "24-01-01"   |
      | "2024-1-1"   |

  Scenario Outline: AC9 - Clicking cancel button takes user back to garden page, without submitting form
    Given I am on the edit plant page
    When I click the "Cancel" button
    Then I am taken back to the garden details page
    And There is a list of all plants I have recorded in the garden with their <name>, a <default image>, and <count> and <description>
    Examples:
      | name     | default image   | count | description |
      | "Plant1" | "Image1"        | 1     | "Desc1"     |
