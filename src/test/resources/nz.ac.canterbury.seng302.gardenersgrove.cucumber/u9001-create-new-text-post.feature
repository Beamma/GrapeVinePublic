Feature: U9001 - As Lei, I want to be able to post updates about my gardens along with tips and advice for the wider community.

  @U9001
  Scenario: AC1
    Given I am on the post feed page,
    When I select the "New Text Post" button,
    Then I am taken to the new text post page.

  @U9001
  Scenario: AC2
    Given I am on the new text post page
    And I enter valid values (including emojis) for the content and optionally a Title
    When I click the "Post" button,
    Then a new text post is created and is set to public
    And I am taken to the post feed page.

  @U9001
  Scenario: AC3
    Given I am on the new text post page
    And I have not entered any details,
    When I click "Cancel",
    Then my post is discarded
    And I get taken to the post feed page.

  @U9001
  Scenario: AC4
    Given I am on the new text post page
    And I have entered values for the Title or Content,
    When I click the Cancel button
    Then a popup appears prompting me to confirm my action.

  @U9001
  Scenario: AC5
    Given I am on the new text post page
    And I enter an invalid (i.e. non-alphanumeric characters other than spaces, dots, commas, hyphens, or apostrophes) title
    When I click the "Post" button,
    Then an error message tells me "Post title must only include letters, numbers, spaces, dots, hyphens, or apostrophes".

  @U9001
  Scenario: AC6
    Given I am on the new text post page
    And I enter an invalid (i.e. non-alphanumeric characters other than spaces, dots, commas, hyphens, or apostrophes) content,
    When I click the "Post" button,
    Then an error message tells me "Post content must only include letters, numbers, spaces, dots, hyphens, or apostrophes".

  @U9001
  Scenario: AC7
    Given I am on the new text post page
    And I enter a title that is longer than 64 characters,
    When I click the "Post" button,
    Then an error message tells me “Post title must be 64 characters long or less”
    And the post is not created.

  @U9001
  Scenario: AC8
    Given I am on the new text post page
    And I enter a content that is longer than 512 characters,
    When I click the "Post" button,
    Then an error message tells me "Post content must be 512 characters long or less",
    And the post is not created.

  @U9001
  Scenario: AC9
    Given I enter Content that contains inappropriate words,
    When I click the "Post" button,
    Then an error message tells me that "The content does not match the language standards of the app."
    And the post is not created.

  @U9001
  Scenario: AC10
    Given I enter a Title that contains inappropriate words,
    When I click the "Post" button,
    Then an error message tells me that "The title does not match the language standards of the app."
    And the post is not created.


