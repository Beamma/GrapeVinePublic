Feature: U90019 - As Lily, I want to be able to start a live-stream, so that I can share garden updates and demonstrate my gardening skills to my community

#  AC1 Is front-end so will be tested manually
#  AC2 Is front-end so will be tested manually

  @U90019
  Scenario Outline: AC3 - Invalid title length or no text
    Given I am on the create live stream form
    And I enter the title <invalid title>
    When I select the create live stream button
    Then I see an error message that tells me that "Title must be 256 characters or less and contain some text" in the "title" field
    Examples:
      | invalid title |
      | ""            |
      | "#####!!"     |
      | "12345"       |
      | " "           |

  @U90019
  Scenario: AC3.1 - Too long title length
    Given I am on the create live stream form
    And I put a title of length 257
    When I select the create live stream button
    Then I see an error message that tells me that "Title must be 256 characters or less and contain some text" in the "title" field

  @U90019
  Scenario Outline: AC4 - Invalid title characters
    Given I am on the create live stream form
    And I enter the title <invalid title>
    When I select the create live stream button
    Then I see an error message that tells me that "Title must only include alphanumeric characters, spaces, emojis and #, ', \", :, !, ,, ., $, ?, -" in the "title" field
    Examples:
      | invalid title |
      | "/%[]"        |
      | "#####!!@"    |

  @U90019
  Scenario Outline: AC5 - Invalid description length or no text
    Given I am on the create live stream form
    And I enter the description <invalid description>
    When I select the create live stream button
    Then I see an error message that tells me that "Description must be 512 characters or less and contain some text" in the "description" field
    Examples:
      | invalid description |
      | ""            |
      | "#####!!"     |
      | "12345"       |
      | " "           |

  @U90019
  Scenario: AC5.1 - Too long description length
    Given I am on the create live stream form
    And I put a description of length 513
    When I select the create live stream button
    Then I see an error message that tells me that "Description must be 512 characters or less and contain some text" in the "description" field

  @U90019
  Scenario Outline: AC6 - Invalid description characters
    Given I am on the create live stream form
    And I enter the description <invalid description>
    When I select the create live stream button
    Then I see an error message that tells me that "Description must only include alphanumeric characters, spaces, emojis and #, ', \", :, !, ,, ., $, ?, -" in the "description" field
    Examples:
      | invalid description |
      | "/%[]"        |
      | "#####!!@"    |

#    TODO - Remaining AC's in T4/5

  #AC7 Tested manually

  @U90019
  Scenario: AC8 - Given I have selected a file of type png, jpg, or svg, that is 10MB or less, when I click the "Done" (or similar) button, then the modal closes and I can see on the create stream form that a thumbnail has been added.
    Given I have created a stream with a valid title and valid description
    And add the thumbnail "png_valid.png"
    When I click Start Stream
    Then the stream and the thumbnail are saved

  @U90019
  Scenario: AC9 - Given the upload media modal is open, when I try to upload a file that is not of type png, jpg or svg, then I am shown the message "Image must be of type png, jpg, jpeg or svg".
    Given I have created a stream with a valid title and valid description
    And add the thumbnail "gif_valid.gif"
    When I click Start Stream
    Then i am shown the error message "Image must be of type png, jpg, jpeg or svg"

  @U90019
  Scenario: AC10 - Given the upload media modal is open, when I upload an image that is more than 10MB, then I am shown the message "Image must be smaller than 10MB".
    Given I have created a stream with a valid title and valid description
    And add the thumbnail "jpg_too_big.jpg"
    When I click Start Stream
    Then i am shown the error message "Image must be smaller than 10MB"

  #11 Tested manually
  #12 Tested manually
  #13 Tested manually

  # AC14 Tested manually