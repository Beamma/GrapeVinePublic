Feature: As Lilly, I want to share images of my plants so that I can inspire and share knowledge with my community.

  Background:
    Given The profanity filter service is up

  @U90010
  Scenario: AC2 - Given I have selected a file of type png, jpg, or svg, that is 10MB or less, when I click the "Done" (or similar) button, then the modal closes and I can see on the post that a photo has been added.
    Given I have created a valid post
    And add the image "jpg_valid.jpg"
    When I click post
    Then the post and the image are saved

  @U90010
  Scenario: AC3 - Given the upload media modal is open, when I try to upload a file that is not of type png, jpg or svg, then I am shown the message "Image must be of type png, jpg or svg".
    Given I have created a valid post
    And add the image "gif_valid.gif"
    When I click post
    Then i am shown the message "Image must be of type png, jpg or svg"

  @U90010
  Scenario: AC4 - Given the upload media modal is open, when I upload an image that is more than 10MB, then I am shown the message "Image must be smaller than 10MB".
    Given I have created a valid post
    And add the image "jpg_too_big.jpg"
    When I click post
    Then i am shown the message "Image must be smaller than 10MB"