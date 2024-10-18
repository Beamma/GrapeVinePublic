package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.dto.ChatMessageDTO;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ChatMessageValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

public class ChatMessageValidatorTest {
    ChatMessageDTO chatMessageDTO;
    ChatMessageValidator chatMessageValidator;

    @BeforeEach
    void setUp() {
        chatMessageDTO = new ChatMessageDTO("Title", 1L, "Name", LocalDateTime.now(), 1L);
        chatMessageValidator = new ChatMessageValidator();
    }

    @Test
    void allFieldsValid_ReturnsNoExceptions() {
        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertNull(chatMessageDTO.getError());
    }

    @Test
    void emptyMessage_ReturnsMessageLengthException() {
        String expectedError = "Chat messages must not be empty";

        chatMessageDTO.setMessage("");

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(chatMessageDTO.getError()),
                () -> Assertions.assertEquals(expectedError, chatMessageDTO.getError())
        );
    }

    @Test
    void justTooLongMessage_ReturnsMessageLengthException() {
        String expectedError = "Chat messages must be less than 256 characters";

        chatMessageDTO.setMessage("a".repeat(256));

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(chatMessageDTO.getError()),
                () -> Assertions.assertEquals(expectedError, chatMessageDTO.getError())
        );
    }

    @Test
    void tooLongMessage_ReturnsMessageLengthException() {
        String expectedError = "Chat messages must be less than 256 characters";

        chatMessageDTO.setMessage("a".repeat(300));

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(chatMessageDTO.getError()),
                () -> Assertions.assertEquals(expectedError, chatMessageDTO.getError())
        );
    }

    @Test
    void shortEnoughMessage_ReturnsNoException() {
        chatMessageDTO.setMessage("a".repeat(255));

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertNull(chatMessageDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\uD83D\uDE19", "\uD83D\uDE42", "hello\uD83E\uDE82"})
    void emojisOnlyMessage_ReturnsNoException(String message) {
        chatMessageDTO.setMessage(message);

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertNull(chatMessageDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#comment", "!, $", "..."})
    void validSpecialCharsMessage_ReturnsNoException(String message) {
        chatMessageDTO.setMessage(message);

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertNull(chatMessageDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]fdsf", ":)", "///"})
    void invalidSpecialCharsMessage_ReturnsException(String message) {
        String expectedError = "Chat messages can only include emojis, alphanumeric characters or #, ', \", :, !, ,, ., $";

        chatMessageDTO.setMessage(message);

        chatMessageValidator.validateChatMessage(chatMessageDTO);
        Assertions.assertAll(
                () -> Assertions.assertNotNull(chatMessageDTO.getError()),
                () -> Assertions.assertEquals(expectedError, chatMessageDTO.getError())
        );
    }

}
