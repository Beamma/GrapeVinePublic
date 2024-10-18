Feature: U9003 - As Lei, I want be able to Like posts I can see to support the account who made the post and to let the algorithm know to show me more posts like this.

  @U9003
  Scenario: AC1 - When I Like a post, i can see that I have now liked the post.
  Given I am able to like a post
  When I interact with the like element on a post I have not liked before
  Then I can see that I have liked the post

  @U9003
  Scenario: AC3 - Liking a post I already liked, removes my like
  Given I am able to unlike a post
  When I interact with the like element on a post I have already liked
  Then I can see that I no longer like the post

  @U9003
  Scenario: AC5 - I can see a posts like count
    Given I am able to see the like count
    When I am looking at a post
    Then I can see how many likes a post has