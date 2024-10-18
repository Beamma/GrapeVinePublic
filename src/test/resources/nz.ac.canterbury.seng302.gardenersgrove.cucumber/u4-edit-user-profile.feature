Feature: U4 - As Sarah, I want to edit my user profile so that I can keep my details accurate

  Scenario: AC1 - the edit profile form is pre-populated
    Given I am on my profile page
    When I click the “Edit” button
    Then I see the edit profile form
    And all my details are prepopulated except the passwords.

  Scenario: AC2 - If I have no last name, the checkbox is ticked, and the input is disabled
    Given I am on the edit profile form
    And I have already indicated that I do not have a last name
    Then the last name field defaults to being disabled
    And the I have no surname checkbox is marked as checked

  Scenario: AC3 - Valid forms save inputs
    Given I am on the edit profile form
    And I enter valid values for my first name, last name, email address, and date of birth
    When I click the submit button
    Then my new details are saved
    And I am taken back to my profile page

  Scenario: AC4 - Ticking no last name clears last name
    Given I am on the edit profile form
    And I click the check box marked “I have no surname”,
    Then the last name text field is disabled
    And any surname that was filled in will be removed from my account details when I submit the form

  Scenario Outline: AC5 - Invalid name values yield errors
    Given I am on the edit profile form
    And I enter invalid values, first name <first name>, last name <last name>, and no last name being <ticked>
    When I click the submit button
    Then an error message for the field <error field> tells me the correct error message <message>
    Examples:
      | first name | last name | ticked  | message                                                                                    | error field |
      | ""         | "l as-'t" | "true"  | "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes" | "firstName" |
      | " f'ir-st" | ""        | "false" | "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes"  | "lastName"  |
      | "@#$"      | ""        | "false" | "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes" | "firstName" |
      | "first"    | "@#$"     | "false" | "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes"  | "lastName"  |

  Scenario Outline: AC6 - Too long name values yield errors
    Given I am on the edit profile form
    And I enter a <name type> name <name> that is more than 64 characters
    When I click the submit button
    Then an error message tells me <name type> "name must be 64 characters long or less"
    Examples:
      | name type | name                                                                |
      | "first"   | "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" |
      | "last"    | "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" |

  Scenario Outline: AC7 - Invalid emails yield errors
    Given I am on the edit profile form
    And I enter an empty or malformed email address <malformed email>
    When I click the submit button
    Then an error message for the field "email" tells me the correct error message "Email address must be in the form 'jane@doe.nz'"
    Examples:
      | malformed email   |
      | ""                |
      | "@email.com"      |
      | "name@email"      |
      | "name@.com"       |
      | "!$%*(@email.com" |

  Scenario: AC8 - Already used emails yield errors
    Given I am on the edit profile form
    And I enter an email address associated to an account that already exists
    When I click the submit button
    Then an error message for the field "email" tells me the correct error message "This email address is already in use"

  Scenario Outline: AC9 - Invalid dates yield errors
    Given I am on the edit profile form
    And I enter a date <date> that is not in the Aotearoa NZ format
    When I click the submit button
    Then an error message for the field "dob" tells me the correct error message "Date is not in valid format, DD/MM/YYYY"
    Examples:
      | date          |
      | "20000-03-12" |
      | "2000-15-12"  |
      | "200-15-12"   |
      | "12/03/2004"  |
      | "12.03.2004"  |

  Scenario: AC10 - Too-young users yield errors
    Given I am on the edit profile form
    And I enter a date of birth for someone younger than 13 years old
    When I click the submit button
    Then an error message for the field "dob" tells me the correct error message "You must be 13 years or older to create an account"

  Scenario: AC11 - Too-old users yield errors
    Given I am on the edit profile form
    And I enter a date of birth for someone older than 120 years old
    When I click the submit button
    Then an error message for the field "dob" tells me the correct error message "The maximum age allowed is 120 years"

  Scenario: AC12 - The cancel button does not save changes
    Given I am on the edit profile form
    When I click the cancel button
    Then I am taken to my profile page
    And no changes have been made to my profile


