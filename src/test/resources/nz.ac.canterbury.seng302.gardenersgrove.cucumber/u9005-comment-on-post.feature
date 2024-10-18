Feature: As Liam, I want to be able to comment on user’s posts and interact with the comments so that I can engage with the plant community

  Background:
    Given The profanity filter service is up

  @U9005
  Scenario Outline: AC1 - Submitting a comment
    Given I type a comment with the message <validMessage>
    When I submit the comment
    Then the comment has been added to the post
    Examples:
      | validMessage                                  |
      | "I love gardening"                            |
      | "My garden has 1000 flowers"                  |
      | "My garden is colourful #Orange #Pink #Green" |
      | "I love to water my plants with @Lily-Fields" |

  @U9005
  Scenario Outline: AC1.1/2 - Submitting a comment with errors
    Given I type a comment with the message <invalidMessage>
    When I submit the comment
    Then I can see an error message <errorMessage>
    Examples:
      | invalidMessage    | errorMessage                                                    |
      | ""                | "Comment must not be empty"                                 |
      | "shit"            | "The comment does not match the language standards of the app." |
      | "IloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplantsIloveplants" | "Comments must be 512 characters or less"|

  @U9005
    Scenario: AC2 - Profane comments increase the profanity counter for the user
    Given I have typed an inappropriate comment "fuck plants"
    When I submit the comment
    Then I am informed the comment is inappropriate "The comment does not match the language standards of the app." and my profanity count increases

  @U9005
  Scenario: AC3 - Like a comment
    Given I am on the view post page
    And I can see a comment from another user
    When I click the like button
    Then the like counter for the comment increases by one

    #@U9005 Scenario: AC4 - Comments I add are viewable underneath the post - MANUAL TESTED

  @U9005
  Scenario: AC5 - Clicking the load more comments button adds 10 more comments to the view
    Given I can see more than three comments on a post
    When I click the See more comments button
    Then I can see an additional 10 comments

#    @U9005 Scenario: AC6 - Comments are ordered by like count and recency - MANUAL TESTED
