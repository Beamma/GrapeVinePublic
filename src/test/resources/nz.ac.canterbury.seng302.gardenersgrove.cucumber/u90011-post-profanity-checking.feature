Feature: U90011 - As Kaia, I want to make sure that posts and comments do not contain any inappropriate words so that the sensibilities of other gardeners are not offended.

    @U90011
    Scenario: AC1: Creating a post with an appropriate title and content
      Given The profanity filter service is up
      And I input a valid post title
      And I input a valid post content
      When I click create
      Then The post is successfully created

    @U90011
    Scenario: AC2: Creating a post with inappropriate title
      Given The profanity filter service is up
      And I input a profane word for my post title
      And I input a valid post content
      When I click create
      Then an error message tells me that the submitted post title is not appropriate
      And the post is not posted
      And the user’s count of inappropriate submissions is increased by 1.

    @U90011
    Scenario: AC3: Creating a post with inappropriate content
      Given The profanity filter service is up
      And I input a profane word for my post content
      When I click create
      Then an error message tells me that the submitted post content is not appropriate
      And the post is not posted
      And the user’s count of inappropriate submissions is increased by 1.

    @U90011
    Scenario: AC4: Creating a comment with inappropriate content
      Given I input a profane word for my comment
      And The profanity filter service is up
      When I click post comment
      Then I see an error message on my comment saying "The comment does not match the language standards of the app."
      And the comment is not posted
      And the user’s count of inappropriate submissions is increased by 1.

    @U90011
    Scenario: AC5: Invalid post title while profanity filter is down
      Given I input a profane word for my post title
      And I input a valid post content
      And The profanity filter service is down
      When I click create
      Then the post is not posted
      And I see an error message saying "Title could not be checked for profanity at this time. Please try again later"

    @U90011
    Scenario: AC6: Invalid post content while profanity filter is down
      Given I input a profane word for my post content
      And The profanity filter service is down
      When I click create
      Then the post is not posted
      And I see an error message saying "Content could not be checked for profanity at this time. Please try again later"

    @U90011
    Scenario: AC7: Invalid comment while profanity filter is down
      Given I input a profane word for my comment
      And The profanity filter service is down
      When I click post comment
      Then the comment is not posted
      And I see an error message on my comment saying "Comment could not be checked for profanity at this time. Please try again later"