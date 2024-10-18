Feature: U19 - As Inaya, I want to be able to make my garden public so that others can see what I’m growing.

  @U19
  Scenario: AC1 - Make a garden public
    Given I am on the garden details page for a garden I am the owner of
    When I click the check box marked "Make my garden public"
    Then my garden will be set to public

  @U19
  Scenario: AC2 - Create garden with valid description
    Given I am on the form to create a new garden
    When I fill in valid details of garden name "My Garden" city "Christchurch" country "New Zealand" size "2.0" and description of "Sunny garden"
    Then the garden is created with a description "Sunny garden"

 # AC3 was deleted from product backlog. Our backlog lets the given AC4 become AC3
  @U19
  Scenario: AC3 - Edit garden to add description
    Given I am on the form to edit a garden with id 4
    When I add a description "Added description"
    And I submit the edit garden form
    Then the garden is updated with the description "Added description"

  @U19
  Scenario: AC4 - Edit garden to remove description
    Given I am on the form to edit a garden with id 5
    When I remove the garden description
    And I submit the edit garden form
    Then the garden is updated without a description

  @U19
  Scenario: AC5.1 - Edit garden with invalid description - too long
    Given I am on the form to edit a garden with id 6
    When I enter a garden description that is longer than 512 characters
    And I submit the edit garden form
    Then A "description" message "Description must be 512 characters or less and contain some text" is displayed

  @U19
  Scenario Outline: AC5.2 - Edit garden with invalid description - no text
    Given I am on the form to edit a garden with id <id>
    When I add a description <description>
    And I submit the edit garden form
    Then A "description" message "Description must be 512 characters or less and contain some text" is displayed
    # and the description is not persisted
    Examples:
    | description   | id  |
    | "1"           | 7   |
    | "!@#$%^&*()"  | 8   |

  @U19
  Scenario: AC6 - Edit garden with inappropriate words
    Given I am on the form to edit a garden with id 9
    When I add a description "badWord"
    And I submit the edit garden form
    Then A "description" message "The description does not match the language standards of the app" is displayed
    # and the description is not persisted


#  Scenario: AC7 - Garden description length indicator
    # manual test this

  @U19
  Scenario: AC8.1 - Created description cannot be evaluated for appropriateness
    Given the profanity API is down
    And I have entered valid values to create a garden
    When I submit the create garden form
    Then the garden is not added
    And a modal pops up with the option to continue without updating description

  @U19
  Scenario: AC8.2 - Edited description cannot be evaluated for appropriateness
    Given the profanity API is down
    And I have entered valid values to edit a garden with id 12
    When I submit the edit garden form
    Then the garden is not edited
    And a modal pops up with the option to continue without updating description

  @U19
  Scenario: AC9.1 - Continue without description
    Given the profanity API is down
    And I have entered valid values to create a garden
    When I am on the modal
    And I click the Continue without description button
    Then the create form is resubmitted without the description
    And the garden is created without a description

  @U19
  Scenario: AC9.2 - Continue without updating description
    Given the profanity API is down
    And I have entered valid values to edit a garden with id 14
    When I am on the modal
    And I click the Continue without updating description button
    Then the edit form is resubmitted without changing the description
    And the garden is updated except the description

