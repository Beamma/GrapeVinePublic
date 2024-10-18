package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.dto.ChatMessageDTO;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.PostValidator.stripEmojis;

/**
 * Validator for checking that chat messages are valid
 */
public class ChatMessageValidator {
    private static final Integer MAX_MESSAGE_LENGTH = 255;
    private static final List<String> CHAT_VALID_CHARACTERS = List.of("#", "'", "\"", ":", "!", ",", ".", "$");
    private static final String ERROR_INCORRECT_LENGTH = "Chat messages must be less than 256 characters";
    private static final String ERROR_EMPTY_FIELD = "Chat messages must not be empty";
    private static final String ERROR_INVALID_CHARS = "Chat messages can only include emojis, alphanumeric characters or #, ', \", :, !, ,, ., $";

    public ChatMessageValidator() {};
    public void validateChatMessage(ChatMessageDTO chatMessageDTO) {
        String message = chatMessageDTO.getMessage();

        if (isMessageEmpty(message)) {
            chatMessageDTO.setError(ERROR_EMPTY_FIELD);
        } else if (!isMessageCorrectLength(message)) {
            chatMessageDTO.setError(ERROR_INCORRECT_LENGTH);
        }
        else if (!isMessageValid(message)) {
            chatMessageDTO.setError(ERROR_INVALID_CHARS);
        }
    }

    public boolean isMessageCorrectLength(String message) {
        return message.length() <= MAX_MESSAGE_LENGTH;
    }

    public boolean isMessageValid(String message) {
        String chatRegex = "^[\\p{L}0-9 " + String.join("", CHAT_VALID_CHARACTERS) + "]*$";
        return stripEmojis(message).matches(chatRegex);
    }

    public boolean isMessageEmpty(String message) {
        return message == null || message.isEmpty() || message.isBlank();
    }

}
