Feature: U22 -  As Kaia, I want to make sure that tags added to gardens do not contain any inappropriate words so that the sensibilities of other gardeners are not offended.

  Background:
    Given I am authenticated

  @U22
  Scenario Outline: AC1 - When I am creating a garden tag, it is checked for appropriateness
    Given I am adding a valid new garden tag named <tag name>
    And The profanity filter service is up
    When I confirm the tag
    Then the tag is checked for offensive or inappropriate words
    Examples:
      | tag name    |
      | "Evergreen" |
      | "Yellow"    |

  @U22
  Scenario Outline: AC2 - When adding a tag that contains profanity
    Given I am adding a valid new garden tag named <profane tag name>
    And The profanity filter service is up
    When I confirm the tag
    Then an error message tells me that "The submitted tag wasn't added, as it was flagged as inappropriate" in the "tagError" field
    And the tag is not added to the list of user-defined tags
    And my inappropriate tag count increases by one
    Examples:
      | profane tag name |
      | "shit"           |
      | "gr ass"         |

  @U22
  Scenario Outline: AC3 - If the profanity API is down, I see an error message
    Given I am adding a valid new garden tag named <tag name>
    And The profanity filter service is down
    When I confirm the tag
    Then an error message tells me that "The submitted tag wasn't added, as it could not be checked for profanity at this time" in the "tagError" field
    And the tag is not added to the list of user-defined tags
    Examples:
      | tag name    |
      | "Evergreen" |
      | "Yellow"    |

