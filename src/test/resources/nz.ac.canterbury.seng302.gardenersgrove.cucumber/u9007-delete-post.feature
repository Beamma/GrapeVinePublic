Feature: As Liam, I made a really embarrassing post and I want to delete it.

  Background:
    Given The profanity filter service is up

  @U9007
  Scenario: AC4 - Given I see the confirmation prompt, when I click "Delete", then the post is deleted and no user can see it on their feed.
    Given I have created a post with comments
    When I click delete
    Then the post and it's comments have been deleted