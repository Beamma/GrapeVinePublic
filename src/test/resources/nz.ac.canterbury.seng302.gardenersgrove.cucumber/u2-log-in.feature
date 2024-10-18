Feature: U2 - As Sarah, I want to log into the system so that I can have a personalized experience with it and enjoy its features
  Scenario: AC1 - On Home page there is a button labelled "Sign In"
    Given I connect to the system's main URL
    When I see the home page
    Then it includes a button labelled "Sign In"

  Scenario: AC2 - When I complete a valid login, then I am taken to the main page of the application
    Given I am on the login form
    And I enter and email address: "jane@email.com" and its corresponding password: "Password1!" for an account that exists on the system
    When I click the “Sign In” button
    Then I am taken to the main page of the application

  Scenario: AC3 - On login page there is a link saying “Not registered Create an account”
    Given I am on the login page
    Then  it contains the text "Not registered? Create an account" which is highlighted as a link.

  Scenario: AC4 - When i click the create an account link then I am redirected to registration page
    Given I am on the login page
    When I click the "Not registered? Create an account" link
    Then I am taken to the registration page.

  Scenario Outline: AC5 - Enter an invalid email address, then an error message pops up
    Given I am on the login form
    And I enter a malformed email <email> address or empty email address
    When I hit the login button
    Then an "email" error message tells me "Email address must be in the form 'jane@doe.nz'".
    Examples:
      | email                    |
      | "Fail"                   |
      | ""                       |
      | "test.com"               |
      | "test@c"                 |

  Scenario: AC6 - Enter an unknown email in login
    Given I am on the login form
    And I enter an email address that is unknown to the system "fake@email.com"
    When I click the “Sign In” button
    Then an "password" error message tells me "The email address is unknown, or the password is invalid".

  Scenario Outline: AC7 - Enter an unknown or wrong password for corresponding email address
    Given I am on the login form
    And I enter a <wrong password> for the corresponding email address <email>
    When I click the “Sign In” button
    Then an "password" error message tells me "The email address is unknown, or the password is invalid".
    Examples:
      | wrong password           | email            |
      | "Fail"                   | "jane@email.com" |
      | ""                       | "jane@email.com" |

  Scenario: AC8 - When I click the cancel button I am taken back to home page
    Given I am on the login page
    When I click the “Cancel” button
    Then I am taken back to the system’s home page
