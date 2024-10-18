Feature: U8 - As Kaia, I want to create a record of my Garden so that I can manage all my gardening tasks.

  Scenario: AC1 - Hitting add garden shows add garden form
    Given I am logged in
    When I hit the Create new garden button
    Then I see a form to create a new garden

  Scenario Outline: AC2 - I can create a valid garden
    Given I am on the create garden form
    When I enter inputs for name: <valid name> description <description> size: <valid size> city: <city> and country <country>
    And I submit the form
    Then I am redirected to garden details with the new garden
    Examples:
      | valid name               | description              | valid size | city   | country   |
      | "Spē-cï.àl Çh'ārâct3ërs" | "Spē-cï.àl Çh'ārâct3ërs" | "3"        | "city" | "Country" |
      | "Spē-cï.àl Çh'ārâct3ërs" | ""                       | "3.5"      | "city" | "Country" |
      | "Spē-cï.àl Çh'ārâct3ërs" | "Spē-cï.àl Çh'ārâct3ërs" | ""         | "city" | "Country" |


  Scenario Outline: AC3.1 - Empty names yield error messages
    Given I am on the create garden form
    When I enter inputs for name: <empty name> description <description> size: <valid size> city: <city> and country <country>
    And I submit the form
    Then I am shown an error in the "gardenName" field saying "Garden name cannot be empty"
    Examples:
      | empty name | description              | valid size | city   | country   |
      | ""         | "Spē-cï.àl Çh'ārâct3ërs" | "3"        | "city" | "Country" |
      | ""         | ""                       | "3.5"      | "city" | "Country" |
      | ""         | "Spē-cï.àl Çh'ārâct3ërs" | ""         | "city" | "Country" |

  Scenario Outline: AC3.2 - Invalid names yield error messages
    Given I am logged in
    And I am on the create garden form
    When I enter inputs for name: <empty name> description <description> size: <valid size> city: <city> and country <country>
    And I submit the form
    Then I am shown an error in the "gardenName" field saying "Garden name must only include letters, numbers, spaces, dots, hyphens or apostrophes"
    Examples:
      | empty name   | description              | valid size | city   | country   |
      | "@handle"    | "Spē-cï.àl Çh'ārâct3ërs" | "3"        | "city" | "Country" |
      | "<>{}"       | ""                       | "3.5"      | "city" | "Country" |
      | "$%^*@*(@(@" | "Spē-cï.àl Çh'ārâct3ërs" | ""         | "city" | "Country" |

  Scenario Outline: AC4.1 - Empty locations yield error messages
    Given I am on the create garden form
    When I enter inputs for name: <name> description <description> size: <size> city: <city> and country <country>
    And I submit the form
    Then I am shown an error in the "location" field saying "City and Country are required"
    Examples:
      | name   | description   | size   | city   | country   |
      | "name" | "description" | "size" | ""     | "country" |
      | "name" | "description" | "size" | "city" | ""        |

  Scenario Outline: AC4.2 - Invalid locations yield error messages
    Given I am on the create garden form
    And I am logged in
    When I enter inputs for name: <name> description <description> size: <size> city: <city> and country <country>
    And I submit the form
    Then I am shown an error in the <error field> field saying <error message>
    Examples:
      | name   | description   | size   | city   | country   | error field        | error message |
      | "name" | "description" | "size" | "@@@"  | "country" | "location.city"    | "City name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes" |
      | "name" | "description" | "size" | "city" | "@@@"     | "location.country" | "Country name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes" |


  Scenario Outline: AC5 - Invalid sizes yield error messages
    Given I am on the create garden form
    When I enter inputs for name: <name> description <description> size: <size> city: <city> and country <country>
    And I submit the form
    Then I am shown an error in the "size" field saying "Garden size must be a positive number"
    Examples:
      | name   | description   | size   | city   | country   |
      | "name" | "description" | "0"    | "city" | "country" |
      | "name" | "description" | "-1"   | "city" | "country" |
      | "name" | "description" | "-1.5" | "city" | "country" |
      | "name" | "description" | "1..5" | "city" | "country" |
      | "name" | "description" | "+5"   | "city" | "country" |

  Scenario Outline: AC6 - European decimals are accepted
    Given I am on the create garden form
    When I enter inputs for name: <name> description <description> size: <european size> city: <city> and country <country>
    And I submit the form
    Then I am redirected to garden details with the new garden
    Examples:
      | name   | description   | european size | city   | country   |
      | "name" | "description" | "3,5"         | "city" | "country" |
      | "name" | "description" | ",5"          | "city" | "country" |

#  Scenario Outline: AC6 - Cancel button takes me to the previous page
# As this is UI related, it is tested in the manual testing suite.

