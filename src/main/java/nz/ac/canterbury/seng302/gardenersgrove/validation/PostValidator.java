package nz.ac.canterbury.seng302.gardenersgrove.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.PostService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class PostValidator {

    private final ProfanityFilterService profanityFilterService;

    private final PostService postService;

    public PostValidator(ProfanityFilterService profanityFilterService, PostService postService) {
        this.profanityFilterService = profanityFilterService;
        this.postService = postService;
    }

    static Logger logger = LoggerFactory.getLogger(PostValidator.class);


    /**
     * Checks if the title is less than or equal to 64 characters
     * @param title the title of the post
     * @return true if the title is less than or equal to 64 characters
     */
    public static boolean isTitleCorrectLength(String title) {
        return title.length() <= 64;
    }


    /**
     * Checks if the content is less than or equal to 512 characters
     * @param content the content of the post
     * @return true if the content is less than or equal to 512 characters
     */
    public static boolean isContentCorrectLength(String content) {
        return content.length() <= 512;
    }


    /**
     * Checks if the field is empty
     * @param field the field to check
     * @return true if the field is empty
     */

    public static boolean isFieldEmpty(String field) {
        return field == null || field.isEmpty() || field.isBlank();
    }




    // Regex is from chatgpt
    /**
     * Checks if the title is valid, should include emojis
     * @param title the title of the post
     * @return true if the title is valid
     */
    public static boolean isTitleValid(String title) {
        return stripEmojis(title).matches("^[\\p{L}0-9 '., -]*$");
    }

    /**
     * Checks if the content is valid
     * @param content the content of the post
     * @return true if the content is valid
     */
    public static boolean isContentValid(String content) {
        return stripEmojis(content).matches("^[\\p{L}0-9 '., \\-\r\n\t]*$");
    }

    // Regex is from chatgpt
    /**
     * Strips emojis from the content so that it can be checked for validity
     */
    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "[\uD83C-\uDBFF\uDC00-\uDFFF]|[\u200D]|[\u2600-\u27BF]|[\uD83E-\uDD00-\uDDFF]|[\u2300-\u23FF]"
    );

    public static String stripEmojis(String content) {
        return EMOJI_PATTERN.matcher(content).replaceAll("");
    }

    /**
     * Checks if the field is profane
     * @param field the field to check
     * @return true if the field is profane
     */
    public boolean isProfane(String field) throws JsonProcessingException, HttpClientErrorException {
        return profanityFilterService.isTextProfane(field);
    }

    /**
     * Validates the post title, using the other methods
     * @param title the title of the post
     * @return a ValidationExceptionDTO if the title is invalid, null otherwise
     */
    private ValidationExceptionDTO validatePostTitle(String title) {
        String titleString = "title";
        if (isFieldEmpty(title)) {
            return null;
        }
        if (!isTitleCorrectLength(title)) {
            return new ValidationExceptionDTO(titleString, 401, "Post title must be 64 characters long or less");
        } else if (!isTitleValid(title)) {
            return new ValidationExceptionDTO(titleString, 401, "Post title must only include letters, numbers, spaces, dots, hyphens, or apostrophes");
        } else {
            try {
                if (isProfane(title)) {
                    return new ValidationExceptionDTO(titleString, 401, "The title does not match the language standards of the app.", true);
                }
            } catch (HttpClientErrorException | JsonProcessingException e) {
                return new ValidationExceptionDTO(titleString, 401, "Title could not be checked for profanity at this time. Please try again later");
            }
        }
        return null;
    }

    /**
     * Validates the post content, using the other methods
     * @param content the content of the post
     * @return a ValidationExceptionDTO if the content is invalid, null otherwise
     */

    private ValidationExceptionDTO validatePostContent(String content) {
        String contentString = "content";
        if (isFieldEmpty(content)) {
            return new ValidationExceptionDTO(contentString, 401, "Post content must not be empty");
        }
        if (!isContentCorrectLength(content)) {
            return new ValidationExceptionDTO(contentString, 401, "Post content must be 512 characters long or less");
        } else if (!isContentValid(content)) {
            return new ValidationExceptionDTO(contentString, 401, "Post content must only include letters, numbers, spaces, dots, hyphens, or apostrophes");
        } else {
            try {
                if (isProfane(content)) {
                    return new ValidationExceptionDTO(contentString, 401, "The content does not match the language standards of the app.", true);
                }
            } catch (HttpClientErrorException | JsonProcessingException e) {
                return new ValidationExceptionDTO(contentString, 401, "Content could not be checked for profanity at this time. Please try again later");
            }
        }
        return null;
    }

    /**
     * Validates a posts image.
     *
     * @param image the posts image
     * @return a ValidationExceptionDTO if the content is invalid
     */
    private ValidationExceptionDTO validatePostImage(MultipartFile image) {
        String imageField = "image";

        // Stop if no image
        if (image == null || image.isEmpty()) {
            return null;
        }

        // Check image
        if (!ImageValidator.isImageTypeCorrect(image)) {
            return new ValidationExceptionDTO(imageField, 401, "Image must be of type png, jpg or svg");
        } else if (!ImageValidator.isImageSizeValid(image)){
            return new ValidationExceptionDTO(imageField, 401, "Image must be smaller than 10MB");
        }
        return null;
    }

    /**
     * Validates the post, doing it for the content and the title
     * @param postDTO the post to validate
     * @return an ArrayList of ValidationExceptionDTOs
     */
    public List<ValidationExceptionDTO> validatePost(PostDTO postDTO) {
        List<ValidationExceptionDTO> exceptions = new ArrayList<>();

        exceptions.add(validatePostTitle(postDTO.getTitle()));
        exceptions.add(validatePostContent(postDTO.getContent()));
        exceptions.add(validatePostImage(postDTO.getImage()));

        exceptions.removeIf(Objects::isNull);
        return exceptions;

    }

    /**
     * Checks if a post exists
     */
    public boolean checkPostExists(Long postId) {
        return postService.getPostById(postId) != null;
    }

    /**
     * Checks if a users owns the post.
     */
    public boolean checkUserOwnsPost(Long postId, User user) {
        return Objects.equals(postService.getPostById(postId).getOwner().getId(), user.getId());
    }
}
