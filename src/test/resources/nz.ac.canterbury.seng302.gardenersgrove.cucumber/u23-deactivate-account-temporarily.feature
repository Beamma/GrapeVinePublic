Feature: U23 - As Kaia, I want to make sure that users who repeatedly trying to add inappropriate tags are prevented to use the app for one week so that they can reflect on their behaviour.

  Background:
    Given I am authenticated
    And The profanity filter service is up

  @U23
  Scenario: AC1 - When I add 5 inappropriate tags, then I receive a warning
    Given I have added 4 inappropriate tags
    When I add another inappropriate tag
    Then I see a "fifthInappropriateSubmission" warning message telling me "You have added an inappropriate tag for the fifth time. If you add another inappropriate tag, your account will be blocked for one week.".
    And I receive an email warning me that I will be blocked if I add a sixth tag

  @U23
  Scenario: AC2 - When I add 6 inappropriate tags, then I am blocked from the system for 7 days.
    Given I have added 5 inappropriate tags
    When I add another inappropriate tag
    Then I’m unlogged from the system
    And I see a "blocked" warning message telling me "Account is blocked for 7 days".
    And I receive an email confirming me that my account is blocked for 7 days.

  @U23
  Scenario: AC3 - When I complete a valid login with a blocked account, then I receive a message stating my account is blocked for X days.
    Given I am on the login form
    And I enter and email address: "john@email.com" and its corresponding password: "Password1!" for an account that exists on the system
    And the account has been blocked for 7 days
    When I click the “Sign In” button
    Then an "password" error message tells me "Account is blocked for 6 days, 23 hours".

  @U23
  Scenario: AC4 - When my account has been banned for 7 days and it is the eighth day then I can log in again with valid credentials.
    Given I am on the login form
    And I enter and email address: "john@email.com" and its corresponding password: "Password1!" for an account that exists on the system
    And the account has been blocked for -1 days
    When I click the “Sign In” button
    Then I am taken to the main page of the application