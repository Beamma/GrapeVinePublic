Feature: U5 - As Sarah, I want to add a picture to my profile so that others in the system will be able to recognize me.
  Scenario: AC1: Clicking edit profile picture on profile page shows file picker
    Given I am on my user profile page
    When I click the Edit Profile Picture button
    Then a file picker is shown

  Scenario: AC2: Clicking edit profile picture on edit profile page shows file picker
    Given I am on the edit profile page
    When I click the Edit Profile Picture button
    Then a file picker is shown

  Scenario: AC3: Submitting a valid image updates the profile picture
    Given I choose a new valid profile picture
    When I submit the image
    Then my profile picture is updated

  Scenario: AC4: Submitting an image of invalid type does not update the profile picture
    Given I choose a new profile picture that is not png, jpg or svg
    When I submit the image
    Then an error message tells me "Image must be of type png, jpg or svg"

  Scenario: AC5: Submitting an image of invalid size does not update the profile picture
    Given I choose a new profile picture that is larger than 10MB
    When I submit the image
    Then an error message tells me "Image must be less than 10MB"

  Scenario: AC6: Profile picture is visible on profile page
    Given I am on my user profile page
    Then I see my profile picture

  Scenario: AC7: Default profile picture is visible on profile page
    Given I am on my user profile page
    And I have not uploaded a profile picture
    Then I see my profile picture