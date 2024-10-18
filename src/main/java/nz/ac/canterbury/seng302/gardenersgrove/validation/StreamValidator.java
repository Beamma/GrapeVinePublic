package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.PostValidator.stripEmojis;

/**
 * Validates a stream creation form.
 * Doesn't check for profanity, per ACs
 */
public class StreamValidator {

    private static final String TITLE_FIELD_NAME = "title";
    private static final String DESCRIPTION_FIELD_NAME = "description";
    private static final String IMAGE_FIELD_NAME = "image";
    private static final Integer MAX_TITLE_LENGTH = 256;
    private static final Integer MAX_DESCRIPTION_LENGTH = 512;
    private static final Integer IMAGE_MAX_SIZE_MB = 10;
    private static final Long BYTES_IN_MB = 1_000_000L;
    private static final List<String> TITLE_VALID_CHARACTERS = List.of("#", "'", "\"", ":", "!", ",", ".", "$", "?", "-");
    private static final List<String> DESCRIPTION_VALID_CHARACTERS = List.of("#", "'", "\"", ":", "!", ",", ".", "$", "?", "-");
    private static final List<String> IMAGE_VALID_FILETYPES = List.of("png", "jpg", "jpeg", "svg");
    private static final String ERROR_TITLE_INVALID_LENGTH = "Title must be " + MAX_TITLE_LENGTH + " characters or less and contain some text";
    private static final String ERROR_TITLE_INVALID_CHARACTERS = "Title must only include alphanumeric characters, spaces, emojis and " + String.join(", ", TITLE_VALID_CHARACTERS);
    private static final String ERROR_DESCRIPTION_INVALID_LENGTH = "Description must be " + MAX_DESCRIPTION_LENGTH + " characters or less and contain some text";
    private static final String ERROR_DESCRIPTION_INVALID_CHARACTERS = "Description must only include alphanumeric characters, spaces, emojis and " + String.join(", ", DESCRIPTION_VALID_CHARACTERS);
    private static final String ERROR_IMAGE_INVALID_FILETYPE = "Image must be of type png, jpg, jpeg or svg";
    private static final String ERROR_IMAGE_INVALID_SIZE = "Image must be smaller than " + IMAGE_MAX_SIZE_MB + "MB";


    /**
     * Validates a stream creation form (DTO)
     * @param streamDTO the inputs for creating the stream
     * @return a list of ValidationExceptionDTOs representing up to one error per field
     */
    public List<ValidationExceptionDTO> validateStream(LiveStreamDTO streamDTO) {
        List<ValidationExceptionDTO> exceptions = new ArrayList<>();

        exceptions.add(validateStreamTitle(streamDTO.getTitle()));
        exceptions.add(validateStreamDescription(streamDTO.getDescription()));
        exceptions.add(validateStreamImage(streamDTO.getImage()));

        exceptions.removeIf(Objects::isNull);   // If any of the fields have no errors (error is null) remove them
        return exceptions;
    }

    /**
     * Validates a stream's title
     * @param title the title of the stream to be validated
     * @return a ValidationExceptionDTO representing the error in the title if the title is invalid, null otherwise
     */
    private ValidationExceptionDTO validateStreamTitle(String title) {
        ValidationExceptionDTO titleException = null;
        if (!isTitleCorrectLength(title)) {
            titleException = new ValidationExceptionDTO(TITLE_FIELD_NAME, 401, ERROR_TITLE_INVALID_LENGTH);
        } else if (!isTitleValid(title)) {
            titleException = new ValidationExceptionDTO(TITLE_FIELD_NAME, 401, ERROR_TITLE_INVALID_CHARACTERS);
        } else if (!containsSomeText(title)) {
            titleException = new ValidationExceptionDTO(TITLE_FIELD_NAME, 401, ERROR_TITLE_INVALID_LENGTH);
        }
        return titleException;
    }

    /**
     * Validates a stream's description
     * @param description the description of the stream to be validated
     * @return a ValidationExceptionDTO representing the error in the description if the description is invalid, null otherwise
     */
    private ValidationExceptionDTO validateStreamDescription(String description) {
        ValidationExceptionDTO descriptionException = null;
        if (!isDescriptionCorrectLength(description)) {
            descriptionException = new ValidationExceptionDTO(DESCRIPTION_FIELD_NAME, 401, ERROR_DESCRIPTION_INVALID_LENGTH);
        } else if (!isDescriptionValid(description)) {
            descriptionException = new ValidationExceptionDTO(DESCRIPTION_FIELD_NAME, 401, ERROR_DESCRIPTION_INVALID_CHARACTERS);
        } else if (!containsSomeText(description)) {
            descriptionException = new ValidationExceptionDTO(DESCRIPTION_FIELD_NAME, 401, ERROR_DESCRIPTION_INVALID_LENGTH);
        }
        return descriptionException;
    }

    /**
     * Validates a stream's image
     * @param image the image of the stream to be validated
     * @return a ValidationExceptionDTO representing the error in the image if the image is invalid, null otherwise
     */
    private ValidationExceptionDTO validateStreamImage(MultipartFile image) {
        // Stop if no image
        if (image == null || image.isEmpty()) {
            return null;
        }

        if (!isImageCorrectSize(image)) {
            return new ValidationExceptionDTO(IMAGE_FIELD_NAME, 401, ERROR_IMAGE_INVALID_SIZE);
        } else if (!isImageValidFiletype(image)) {
            return new ValidationExceptionDTO(IMAGE_FIELD_NAME, 401, ERROR_IMAGE_INVALID_FILETYPE);
        }
        return null;
    }

    /**
     * Checks the length of the title
     * @param title the title of the stream
     * @return true if the title is shorter than the maximum allowed length and not empty
     */
    private boolean isTitleCorrectLength(String title) {
        return title.length() <= MAX_TITLE_LENGTH && !title.isBlank();
    }

    /**
     * Checks if the title contains only valid characters. Strips emojis, then checks other characters to allow emojis
     * and other characters to be checked separately
     * @param title the title of the stream
     * @return true if the title contains only valid characters, false otherwise
     */
    private boolean isTitleValid(String title) {
        String titleRegex = "^[\\p{L}0-9 " + String.join("", TITLE_VALID_CHARACTERS) + "]*$";
        return stripEmojis(title).matches(titleRegex);
    }

    /**
     * Check that the title contains some text. Title cannot be special chars, numbers or emojis alone
     * @param title the title of the stream
     * @return true if the title contains at least one valid text character
     */
    private boolean containsSomeText(String title) {
        Pattern textCharacter = Pattern.compile("\\p{L}");
        Matcher matcher = textCharacter.matcher(title);
        return matcher.find();
    }

    /**
     * Checks the length of the description
     * @param description the description of the stream
     * @return true if the description is shorter than the maximum allowed length and not empty
     */
    private boolean isDescriptionCorrectLength(String description) {
        return description.length() <= MAX_DESCRIPTION_LENGTH && !description.isBlank();
    }

    /**
     * Checks if the description contains only valid characters. Strips emojis, then checks other characters to allow emojis
     * and other characters to be checked separately
     * @param description the description of the stream
     * @return true if the description contains only valid characters, false otherwise
     */
    private boolean isDescriptionValid(String description) {
        String descriptionRegex = "^[\\p{L}0-9 " + String.join("", DESCRIPTION_VALID_CHARACTERS) + "]*$";
        return stripEmojis(description).matches(descriptionRegex);
    }

    /**
     * Checks the size of the thumbnail image for a stream
     * @param image the thumbnail image for a stream
     * @return true if the image is smaller than the maximum allowed size
     */
    private boolean isImageCorrectSize(MultipartFile image) { return image.getSize() < IMAGE_MAX_SIZE_MB * BYTES_IN_MB; }

    /**
     * Checks the filetype of the thumbnail image for a stream
     * @param image the thumbnail image for a stream
     * @return true if the image's filetype is valid, false otherwise
     */
    private boolean isImageValidFiletype(MultipartFile image) {
        String contentType = image.getContentType();
        return contentType != null && IMAGE_VALID_FILETYPES.stream().anyMatch(contentType::contains);   // From chatgpt
    }
}
