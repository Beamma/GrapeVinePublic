Feature: U17 - As Liam, I want to connect with my friends on Gardener’s Grove so that we can build a community on the app.

  Scenario: AC1 - When I click the Friends button I am navigated to the manage friends page
    Given I am anywhere in the app
    When I click the Friend button
    Then I am navigated to the manage friends page

  Scenario: AC2 - I can view and access my friends details on the manage friends page
    Given I am on the manage friends page
    Then I can see my friends details including their profile picture, names and a link to their gardens

  Scenario: AC3 - A search bar is brought up to add friends
    Given I am on the manage friends page
    When I click the add friend button
    Then I see a search bar

  Scenario Outline: AC4 - Searching a name brings up a list of users matching that name
    Given I am on the manage friends page
    When I click the add friend button
    And I enter <first name> and <last name>
    Then a list of users matching the <first name> and <last name> are provided to me
    Examples:
    | first name | last name |
    | "Jane" | "Doe" |
    | "Jane" | "" |
    | "" | "Doe" |

  Scenario Outline: AC5 - Searching an email brings up a list of users matching that email
    Given I am on the manage friends page
    When I click the add friend button
    And I enter a valid email address <email>
    Then a list of users matching the email <email> are provided to me
    Examples:
      | email |
      | "Jane@email.com" |
      | "JohnnySmith123@email.com" |

  Scenario: AC6 - Entering a search query with no matches displays a message
    Given I am on the manage friends page
    When I click the add friend button
    And I enter a search string with no matches
    Then A message saying "There is nobody with that name or email in Gardener’s Grove" appears

  Scenario: AC7 - User receives invite when invited to be a friend
    Given A friend search provides a result
    When I click the invite as friend button
    Then The receiving user sees an invite request on their manage friends page

  Scenario: AC8 - User is added to respectively users friends list when invite is accepted
    Given I am on the manage friends page
    And I have pending invites
    When I accept an invite from a user
    Then the user is added to my friends list and I can view their profile
    And I am added to the users friends list and they can view my profile

  Scenario: AC9 - Declining an invite disallows re-invites and does not add to the friend list
    Given I am on the manage friends page
    And I have pending invites
    When I decline an invite from a user
    Then the user is not added to my friends list
    And they cannot invite me anymore

  Scenario: AC10.1 - non responded invites are visible to the user
    Given A friend search provides a result
    When I click the invite as friend button
    Then I can see the status of the invite as pending

  Scenario: AC10.2 - declined invite status is visible to the user
    Given A friend search provides a result
    When I click the invite as friend button
    Then I can see the status of the invite as declined