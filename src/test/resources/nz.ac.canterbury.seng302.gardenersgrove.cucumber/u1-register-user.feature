Feature: U1 - As Sarah, I want to register on Gardener’s Grove so that I can use its awesome features.

  Scenario: AC1 - Main url shows home page with register button
    Given I connect to the system's main URL
    When I see the home page
    Then it includes a button labeled Register

  Scenario: AC2 - Clicking the sign up button with valid details redirects me to the user profile page.
    Given I am on the registration page
    When I fill in valid details of name "John" last name "smith" email address "joe@email.co.nz" password "Password1!" password repeat "Password1!" and DOB "2004-20-05"
    And I click the sign up button
    Then I am signed in
    And I am directed to the profile page

  Scenario: AC3 - User is signed up without last name when checking the no last name box
    Given I am on the registration page
    And I check the box for having no last name
    When I fill in valid details of name "John" email address "joe@email.co.nz" password "Password1!" password repeat "Password1!" and DOB "2004-20-05"
    And I click the sign up button
    Then The lastName field is disabled
    And I am registered without a lastName after I click the Sign up button

  Scenario Outline: AC4.1 - Entering Invalid first name gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <invalidFirstName> <lastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "First name cannot be empty and must only include letters, spaces, hyphens or apostrophes" in the "firstName" field
    Examples:
      | email       | invalidFirstName | lastName | DOB         | password     | passwordRepeat |
      | "jane@d.com"| "1111"           | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |
    | "jane@d.com"| "!!!!!!"           | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |
    | "john@doe.com"| ""           | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |
    | "john1@doe.com"| "John1"           | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |

  Scenario Outline: AC4.2 - Entering Invalid last name gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <invalidLastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes" in the "lastName" field
    Examples:
      | email       | firstName | invalidLastName | DOB         | password     | passwordRepeat |
      | "jane@d.com"| "Jane"    | "111111"        | "2002-12-12"| "Password1!" | "Password1!"   |
      | "jane@d.com"| "Jane"           | "!!!!!!"    | "2002-12-12"| "Password1!" | "Password1!"   |
      | "john@doe.com"| "Jane"           | "Doe1"    | "2002-12-12"| "Password1!" | "Password1!"   |
      | "john1@doe.com"| "John"           | ""    | "2002-12-12"| "Password1!" | "Password1!"   |


  Scenario Outline: AC5.1 - Entering an overly long first/last name gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <longFirstName> <lastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "First name must be 64 characters long or less" in the "firstName" field
    Examples:
      | email       | longFirstName | lastName | DOB         | password     | passwordRepeat |
      | "jane@d.com"|"JaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJane"    | "111111"        | "2002-12-12"| "Password1!" | "Password1!"   |

  Scenario Outline: AC5.2 - Entering an overly long first/last name gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <longLastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "Last name must be 64 characters long or less" in the "lastName" field
    Examples:
      | email       | firstName | longLastName | DOB         | password     | passwordRepeat |
      | "jane@d.com"| "Jane"    |"JaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJaneJane"        | "2002-12-12"| "Password1!" | "Password1!"   |

  Scenario Outline: AC6 - Entering a malformed email gives error message
    Given I am on the registration page
    When I enter invalid input for <invalidEmail> <firstName> <lastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "Email address must be in the form 'jane@doe.nz'" in the "email" field
    Examples:
      | invalidEmail | firstName | lastName | DOB         | password     | passwordRepeat |
      | "jane"   | "Jane"    | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |
    | "jane@"   | "Jane"    | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |
    | "jane@doe"   | "Jane"    | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |
    | "jane@doe."   | "Jane"    | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |


  Scenario Outline: AC7 - Entering an existing email address gives error message
    Given I am on the registration page
    When I enter invalid input for <invalidEmail> <firstName> <lastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "This email address is already in use" in the "email" field
    Examples:
      | invalidEmail | firstName | lastName | DOB         | password     | passwordRepeat |
      | "jane@doe.nz"   | "Jane"    | "Doe"    | "2002-12-12"| "Password1!" | "Password1!"   |

  Scenario Outline: AC8 - Entering an invalid DOB gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <lastName> <invalidDOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "Date is not in valid format, DD/MM/YYYY" in the "dob" field
    Examples:
      | email       | firstName | lastName | invalidDOB  | password     | passwordRepeat |
      | "jane@d.com"| "Jane"    | "Doe"    | "2002-31-04"| "Password1!" | "Password1!"   |
    | "jane2@d.com"| "Jane"    | "Doe"    | "2002-2-31"| "Password1!" | "Password1!"   |
    | "john@doe2.com" | "Jane"    | "Doe"    | "2002-13-13"| "Password1!" | "Password1!"   |

  Scenario Outline: AC9 - Entering a DOB of age < 13 gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <lastName> <invalidDOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "You must be 13 years or older to create an account" in the "dob" field
    Examples:
      | email       | firstName | lastName | invalidDOB  | password     | passwordRepeat |
      | "jane@d.com"| "Jane"    | "Doe"    | "2020-03-03"| "Password1!" | "Password1!"   |
    | "johnny@doe.com" | "Jane"    | "Doe"    | "2012-03-03"| "Password1!" | "Password1!"   |

  Scenario Outline: AC10 - Entering a DOB of age > 120 gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <lastName> <invalidDOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "The maximum age allowed is 120 years" in the "dob" field
    Examples:
      | email       | firstName | lastName | invalidDOB  | password     | passwordRepeat |
      | "jane@d.com"| "Jane"    | "Doe"    | "1900-03-03"| "Password1!" | "Password1!"   |
    | "cool@doe" | "Jane"    | "Doe"    | "1903-05-13"| "Password1!" | "Password1!"   |

  Scenario Outline: AC11 - Entering non-matching passwords gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <lastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "Passwords do not match" in the "passwordRepeat" field
    Examples:
      | email       | firstName | lastName | DOB         | password     | passwordRepeat |
      | "jane@d.com"| "Jane"    | "Doe"    | "2002-01-01"| "Password1!" | "Password2!"   |

  Scenario Outline: AC12 - Entering non-matching passwords gives error message
    Given I am on the registration page
    When I enter invalid input for <email> <firstName> <lastName> <DOB> <password> <passwordRepeat>
    And I click the "Sign up" button to submit the form
    Then I am shown an error "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character." in the "password" field
    Examples:
      | email       | firstName | lastName | DOB         | password   | passwordRepeat |
      | "jane@d.com"| "Jane"    | "Doe"    | "2002-01-01"| "password" | "Password2!"   |
    | "jane@com.doe"| "Jane"    | "Doe"    | "2002-01-01"| "Password" | "Password2!"   |
    | "jane@net.com"| "Jane"    | "Doe"    | "2002-01-01"| "Password1" | "Password2!"   |
    | "john@jane.doe"| "Jane"    | "Doe"    | "2002-01-01"| "Password!" | "Password2"   |
    | "john@man.com"| "Jane"    | "Doe"    | "2002-01-01"| "word" | "Password2"   |

  Scenario: AC13 - Cancelling the registration redirects to home page
    Given I am on the registration page
    When I click the Cancel Button
    Then I am redirected to the home page
