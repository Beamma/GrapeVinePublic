Feature: U6 - As Sarah, I want to confirm my account by email when I register so that may account is more secure.
  Scenario: AC1 - On register, a confirmation email is sent with a token
    Given I fill in a valid registration form with the name "Jane" "Doe", dob "2000-01-01", email "my@email.com" and password "Password1!"
    When I click the register button
    Then An email is sent to my address, "my@email.com"
    And The email includes the token generated for my account confirmation
    And I am redirected to "/auth/registration-confirm"

  Scenario: AC2: Using the token after expiry yields an error message
    Given A sign-up code has been generated for a user
    When The code has expired
    And I try to use the sign-up code
    Then An error message "Signup code invalid" is displayed

  Scenario: AC3: Users cannot login for the first time without a token
    Given A sign-up code has been generated for a user
    When I try to log in
    Then An error "Please confirm your email address" is displayed

  Scenario: AC4 - Using the code redirects me to the login page
    Given A sign-up code has been generated for a user
    And I try to use the sign-up code
    Then I am redirected to "/auth/login"
    And There is a message that says "Your account has been activated, please log in"

  Scenario: AC5 - I can log in without a validation code if I have already confirmed my account
    Given A sign-up code has been generated for a user
    And The account has been verified
    When I try to log in
    And I am redirected to "/user/home"

  Scenario: AC6 - Mis-delivered emails tell me they can be ignored
    Given A sign-up code has been generated for a user
    When An email is wrongly sent to my address, "test@email.com"
    Then The email contains the phrase "If this was not you, you can ignore this message and the account will be deleted after 10 minutes"

