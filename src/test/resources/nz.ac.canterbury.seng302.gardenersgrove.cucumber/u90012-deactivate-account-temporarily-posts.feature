Feature: U90012 - As Kaia, I want to make sure that users who are repeatedly trying to add inappropriate submissions are
  prevented to use the app for one week so that they can reflect on their behaviour.

  Background:
    Given The profanity filter service is up

  @U90012
  Scenario: AC1 - Fifth inappropriate submission triggers a warning
    Given I have entered 4 inappropriate submissions
    When I enter another inappropriate submission
    Then I see a popup message that I have added five inappropriate submissions
    And I receive an email warning me that I may get blocked for one week

  @U90012
  Scenario: AC2 - Sixth inappropriate submission logs out and blocks user
    Given I have entered 5 inappropriate submissions
    When I enter a sixth inappropriate submission
    Then I am logged out of the system
    And I see a popup message that I have been blocked for one week
    And I receive an email that I have been blocked for one week

  # AC3 and 4 are tested in  U23 - Deactivate account temporarily



