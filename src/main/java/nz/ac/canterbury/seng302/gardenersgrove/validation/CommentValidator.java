package nz.ac.canterbury.seng302.gardenersgrove.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.dto.CommentDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Validator for checking that comments on post are syntactically valid, and conform to the sites rules
 */
public class CommentValidator {

    private final ProfanityFilterService profanityFilterService;

    private static final Integer MAX_COMMENT_LENGTH = 512;
    private static final String ERROR_POST_NOT_EXIST = "The post you are commenting on no longer exists";
    private  static final String ERROR_IS_PROFANE = "The comment does not match the language standards of the app.";
    private static final String ERROR_PROFANITY_NOT_PROCESSED = "Comment could not be checked for profanity at this time. Please try again later";
    private static final String ERROR_INCORRECT_LENGTH = "Comments must be " + MAX_COMMENT_LENGTH + " characters or less";
    private static final String ERROR_EMPTY_FIELD = "Comment must not be empty";
    private static final String ERROR_INVALID_CHARS = "Comments must only include letters, numbers, spaces, dots, hyphens, commas, or apostrophes";

    public CommentValidator(ProfanityFilterService profanityFilterService) {
        this.profanityFilterService = profanityFilterService;
    }

    /**
     * Pass a CommentDTO into this function, and it will validate the comment
     * and supplies errors if any
     * @param commentDTO a data transfer object for carrying data around the server
     */
    public void validateComment(CommentDTO commentDTO) {
        String comment = commentDTO.getComment();

        if (!postExists(commentDTO.getPost())) {
            commentDTO.setError(ERROR_POST_NOT_EXIST);
        } else if (isFieldEmpty(comment)) {
            commentDTO.setError(ERROR_EMPTY_FIELD);
        } else if (!isCommentCorrectLength(comment)) {
            commentDTO.setError(ERROR_INCORRECT_LENGTH);
        } else if (!isCommentValid(comment)) {
            commentDTO.setError(ERROR_INVALID_CHARS);
        } else {
            try {
                if (isProfane(comment)) {
                    commentDTO.setError(ERROR_IS_PROFANE);
                    commentDTO.setContainsProfanity(true);
                } else {
                    commentDTO.setContainsProfanity(false);
                }
            } catch (HttpClientErrorException | JsonProcessingException e) {
                commentDTO.setError(ERROR_PROFANITY_NOT_PROCESSED);
                commentDTO.setContainsProfanity(false);
            }
        }
    }

    /**
     * Checks if the title is less than or equal to 512 characters
     * @param title the title of the post
     * @return true if the title is less than or equal to 512 characters
     */
    public boolean isCommentCorrectLength(String title) {
        return title.length() <= MAX_COMMENT_LENGTH;
    }

    /**
     * Checks if the field is empty
     * @param field the field to check
     * @return true if the field is empty
     */

    public boolean isFieldEmpty(String field) {
        return field == null || field.isEmpty() || field.isBlank();
    }

    /**
     * Checks if the comment is valid, currently always returns true, as we allow all chars.
     * How-ever keeping here as in the future we may want to restrict some chars.
     * @param comment the content of the post
     * @return true if the content is valid
     */
    public boolean isCommentValid(String comment) {
        return true;
    }

    /**
     * Checks if the comment is profane
     * @param comment the field to check
     * @return true if the comment is profane
     */
    public boolean isProfane(String comment) throws JsonProcessingException, HttpClientErrorException {
        return profanityFilterService.isTextProfane(comment);
    }

    /**
     * Checks if the post for the comment is valid
     * @param post the post the comment is going on
     * @return boolean, if the post exists
     */
    public boolean postExists(Post post) {
        return post != null;
    }
}
