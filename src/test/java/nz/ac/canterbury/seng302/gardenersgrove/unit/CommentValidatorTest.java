package nz.ac.canterbury.seng302.gardenersgrove.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.dto.CommentDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.CommentValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class CommentValidatorTest {

    ProfanityFilterService profanityFilterService;

    CommentValidator commentValidator;

    Post post;
    User user;
    CommentDTO commentDTO;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        profanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(profanityFilterService.isTextProfane(Mockito.any())).thenReturn(false);
        Mockito.when(profanityFilterService.isTextProfane("BadWord")).thenReturn(true);

        commentValidator = new CommentValidator(profanityFilterService);

        user = new User();
        post = new Post("Test", "Test", user);
    }

    @Test
    void given_test_is_valid_return_true() {
        Assertions.assertTrue(commentValidator.isCommentCorrectLength("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    void given_test_is_invalid_return_false() {
        Assertions.assertFalse(commentValidator.isCommentCorrectLength("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "    ", "\n"})
    void given_empty_comment_return_true(String comment) {
        Assertions.assertTrue(commentValidator.isFieldEmpty(comment));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "a ", " a ", "    a", "\na", "a\n", "\na"})
    void given_not_empty_comment_return_false(String comment) {
        Assertions.assertFalse(commentValidator.isFieldEmpty(comment));
    }

    @Test
    void given_post_not_exists_return_false() {
        Assertions.assertFalse(commentValidator.postExists(null));
    }

    @Test
    void given_post_exists_return_true() {
        Assertions.assertTrue(commentValidator.postExists(post));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "a ", " a ", "    a", "\na", "a\n", "\na", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void given_comment_is_valid_no_errors (String comment) {
        commentDTO = new CommentDTO(comment, user, post);
        commentValidator.validateComment(commentDTO);

        Assertions.assertNull(commentDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Test", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void given_post_not_exist_then_error (String comment) {
        commentDTO = new CommentDTO(comment, user, null);
        commentValidator.validateComment(commentDTO);

        Assertions.assertEquals("The post you are commenting on no longer exists", commentDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "    ", "\n"})
    void given_field_empty_then_error (String comment) {
        commentDTO = new CommentDTO(comment, user, post);
        commentValidator.validateComment(commentDTO);

        Assertions.assertEquals("Comment must not be empty", commentDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void given_comment_too_long_then_error (String comment) {
        commentDTO = new CommentDTO(comment, user, post);
        commentValidator.validateComment(commentDTO);

        Assertions.assertEquals("Comments must be 512 characters or less", commentDTO.getError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"BadWord"})
    void given_comment_profane_then_error (String comment) {
        commentDTO = new CommentDTO(comment, user, post);
        commentValidator.validateComment(commentDTO);

        Assertions.assertEquals("The comment does not match the language standards of the app.", commentDTO.getError());
    }

}
