Feature: As Liam, I want to be able to link a garden I own to my post so that other users can view the garden from the post.

  Background:
    Given a user has has public gardens named "Garden 1", "garden 2" "GARDEN3"

  @U9009
  Scenario: AC3 - Searching for public gardens
    Given the user has entered a search string of "garden"
    When the user submits the search
    Then I see all matching gardens shown
    And the gardens all have their name and city included and are ordered by recency

  @U9009
  Scenario: AC5 - Searching for public gardens with no matches
    Given the user has entered a search string of "no matches"
    When the user submits the search
    Then I see no gardens
    And I am told: None of your public gardens match your search

  @U9009
  Scenario Outline: AC6 - Invalid queries are rejected
    Given the user has entered a search string of <invalid string>
    When the user submits the search
    Then I am shown an error message saying "Invalid search. Queries may only contain alphanumeric characters, -, ‘, dots, commas and spaces"
    Examples:
      | invalid string |
      | "#garden"      |
      | "\""           |
      | "😭"           |

  @U9009
  Scenario: AC9 - I can link a garden to a post
    Given a user has selected a garden to link
    When I submit the add post form
    Then I am redirected to the feed page
    And I see the garden linked to the most recent post

  @U9009
  Scenario: AC10 - I can link a garden to a post
    Given I have created a post with a linked garden
    And I am on the post feed page
    When I click the linked garden element
    Then I am taken to the garden view page for that garden