Feature: As Lily, I want to be able to end my live-stream at any time, so that I can control when my live-stream finishes and ensure I stop streaming when I am ready.

  # Manual Test AC1

  # Manual Test AC2

  @U90022
  Scenario: AC3 - Ending Livestream
    Given I have created a livestream with a title "Stream1" and description "Sup"
    When  I click the end stream button and confirm on the modal to end the stream
    Then The stream is ended
    And I am taken back to the browse livestreams feed page

  # Manual Test AC4

  # Manual Test AC5

  # Manual Test AC6

  @U90022
  Scenario: AC7 - Create multiple livestream
    Given I have created a livestream with a title "Stream1" and description "Sup"
    When I create another livestream with a title "Stream1" and description "Sup"
    Then I receive an error message

  @U90022
  Scenario: AC8 - Audience tries to end stream
    Given I am on the browse live stream feed page and I have not started a livestream
    When I click the watch stream button for any streamer
    Then I cannot end their stream
  
  # Manual Test AC9

  @U90022
  Scenario: AC10 - Ended livestream doesn't appear on browse
    Given I have created a livestream with a title "Stream1" and description "Sup"
    When I click the end stream button and confirm on the modal to end the stream
    And I navigate to the browse livestream page
    Then it is no longer visible on the browse livestreams page
