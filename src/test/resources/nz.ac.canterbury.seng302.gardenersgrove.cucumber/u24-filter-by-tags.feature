Feature: U24 - As Inaya, I want to be able to browse gardens by different tags so that I can browse for gardens that match my interests.

  Background:
    Given I am authenticated as "filterTester@gmail.com"

  @U24
  Scenario Outline: AC1 - I can filter by any number of tags
    Given I am on the browse garden form and I have the search set as "Test" and the Tags as <tag search>
    When I submit the filter form
    Then <num results> results are displayed
    Examples:
      | tag search       | num results |
      | ""               | 11          |
      | "Tag1"           | 3           |
      | "Tag3,Tag2"      | 4           |
      | "Tag1,Tag2,Tag3" | 5           |

  @U24
  Scenario: AC2 Autocomplete tag suggestions appear
    Given I want to browse for a tag "tag"
    When I start typing the tag
    Then tags matching my input are shown

#    AC3, AC4, AC5 are implemented in javascript so are tested manually

  @U24
  Scenario: AC6 Given I filter by multiple tags and a search, then only the gardens that meet the filter params are displayed
    Given I am on the browse garden form and I have the search set as "Test" and the Tags as "Tag1,Tag2"
    When I submit the filter form
    Then 4 results are displayed
