Feature: U21 - As Inaya, I want to be able to browse gardens by different user-specified tags so that I can browse for gardens that match my interests.

  #  AC1 (Frontend), this is tested by our manual/e2e testing

  Scenario: AC2 - When on a gardens page I'm able to see its tags
    Given  I am on the garden details page for a public garden
    Then  I can see a list of tags that the garden has been marked with by its owner

  Scenario: AC3 - When adding a tag a list of auto complete options are available
    Given I am typing a tag
    Then I should see autocomplete options for tags that already exist in the system

  # AC4 (Frontend), this is tested by our manual/e2e testing

  Scenario Outline: AC5 - When I submit a tag, this tag now displays in the auto-complete
    Given I have entered valid text for a <tag> that does not exist
    When I click the “+” button or press enter
    Then the tag is added to my garden
    And the textbox is cleared
    And the tag becomes a new user-defined tag on the system showing up in future auto-complete suggestions
    Examples:
      | tag       |
      | "Test"    |
      | "Test1"   |
      | "Test 1"  |
      | "Test-2"  |
      | "Test_3"  |
      | "Test'4"  |
      | "Test\"5" |

  Scenario Outline: AC6 - Submiting an invlid tag
    Given I have entered invalid text <Tag>
    When I click the “+” button or press enter
    Then an tagError message tells me "The tag name must only contain alphanumeric characters, spaces,  -, _, ', or ”"
    And no tag is added to my garden and no tag is added to the user defined tags the system knows
    Examples:
      | Tag     |
      | ""      |
      | "!"     |
      | "@"     |
      | "\t"    |
      | "#"     |
      | "$"     |
      | "%"     |
      | "^"     |
      | "&"     |
      | "*"     |
      | "("     |
      | ")"     |
      | "+"     |

  Scenario: AC7 - Tag too long
    Given I have entered a tag that is more than 25 characters
    When I click the “+” button or press enter
    Then an tagError message tells me "A tag cannot exceed 25 characters"
    And no tag is added to my garden and no tag is added to the user defined tags the system knows
