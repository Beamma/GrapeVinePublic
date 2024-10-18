Feature: As Liam, I want to be able to add chat comments to a stream, so that I can interact with the streamer/community and ask questions/share knowledge.

  # AC1: manual tested

  @U90023
  Scenario Outline: AC2 - Submit a chat
    Given I am watching a livestream
    And I have typed a chat message <blue sky chat>
    When I submit the chat
    Then The chat is added to the database
    Examples:
      | blue sky chat             |
      | "chat"                    |
      | "Sprint Ono: Oh no! 😱🎬" |
      | "#chatmessage"            |
      | "Oh, YEAH!!!"             |

  @U90023
  Scenario Outline: AC3 - Invalid chars in chat
    Given I am watching a livestream
    And I have typed a chat message <invalid chat>
    When I submit the chat
    Then I see a chat error saying "Chat messages can only include emojis, alphanumeric characters or #, ', \", :, !, ,, ., $"
    And The chat is not added to the database
    Examples:
      | invalid chat             |
      | "@chat"                    |
      | "[]" |

  @U90023
  Scenario: AC4 - Too long chats
    Given I am watching a livestream
    And I have typed a chat message that is 256 chars long
    When I submit the chat
    Then I see a chat error saying "Chat messages must be less than 256 characters"
    And The chat is not added to the database

    # AC5-10: Manual tested