Feature: U15 - As Kaia, I want to specify an actual address for my different recorded gardens, so that I can get consistent information relevant to my area

  Scenario: AC1 - I can create full locations on the add garden form
    Given I am on the create new garden form
    When I add a location
    And I add a valid name
    Then I can specify a street address and number "Address and Number", suburb "Suburb", city "City", postcode "XXXX", and country "Country"

  Scenario: AC2 - I can create full locations on the edit garden form
    Given I am on the edit garden page
    When I add a location
    And I add a valid name
    Then I can specify an edited street address and number "Address and Number", suburb "Suburb", city "City", postcode "XXXX", and country "Country"

  Scenario Outline: AC3 and AC5 - Cities and gardens are required
    Given I am on the create garden form
    When I enter inputs for name: <name> description <description> size: <size> city: <city> and country <country>
    And I submit the form
    Then I am shown an error in the "location" field saying "City and Country are required"
    Examples:
      | name   | description   | size   | city   | country   |
      | "name" | "description" | "size" | ""     | "country" |
      | "name" | "description" | "size" | "city" | ""        |

  Scenario Outline: AC4 - Street address, suburb, postcode are optional
    Given I am on the create new garden form
    When I add a location
    And I add a valid name
    Then I can specify a street address and number <address>, suburb <suburb>, city "City", postcode <postcode>, and country "Country"
    Examples:
      | address   | suburb   | postcode   |
      | "Address" | "Suburb" | "Postcode" |
      | ""        | "Suburb" | "Postcode" |
      | "Address" | ""       | "Postcode" |
      | "Address" | "Suburb" | ""         |
      | "Address" | ""       | ""         |
      | ""        | "Suburb" | ""         |
      | ""        | ""       | "Postcode" |
      | ""        | ""       | ""         |


  # AC 6, 7, 8
  # As these tests are based on UI, these are covered in our manual tests


  Scenario: AC8 - Locations can be used even if they couldn't be found by the API
    Given I am on the edit garden page
    When I add a location
    And I add a valid name
    And The location can't be found by the location API
    Then I can specify an edited street address and number "Address and Number", suburb "Suburb", city "City", postcode "XXXX", and country "Country"
