package nz.ac.canterbury.seng302.gardenersgrove.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.service.PostService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PostValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

class PostValidationTest {

    private static ProfanityFilterService profanityFilterService = Mockito.mock(ProfanityFilterService.class);
    private static PostValidator postValidator = new PostValidator(profanityFilterService, Mockito.mock(PostService.class));

    @BeforeEach
    void setUp() throws JsonProcessingException {
        Mockito.when(profanityFilterService.isTextProfane(Mockito.anyString()) ).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));
    }


    // Length tests
    @Test
    void titleTooLong_returnsFalse() {
        Assertions.assertFalse(postValidator.isTitleCorrectLength("a".repeat(65)));
    }

    @Test
    void contentTooLong_returnsFalse() {
        Assertions.assertFalse(postValidator.isContentCorrectLength("a".repeat(513)));
    }


    // Validity (characters) tests
    @Test
    void titleContainsOnlyLettersAndNumbers_returnsTrue() {
        Assertions.assertTrue(postValidator.isTitleValid("abc123"));
    }


    // Chatgpt generated the strings
    @ParameterizedTest
    @ValueSource(strings = {"abc123!", "abc123@", "abc123#", "abc123$", "abc123%", "abc123^", "abc123&", "abc123*", "abc123(", "abc123)", "abc123_", "abc123=", "abc123+", "abc123[", "abc123]", "abc123{", "abc123}", "abc123;", "abc123:", "abc123\"", "abc123<", "abc123>", "abc123/", "abc123?", "abc123\\", "abc123|", "abc123`", "abc123~", "abc123\t", "abc123\n", "abc123\r", "abc123\f", "abc123\b", "abc123\u000B"})
    void titleContainsInvalidCharacters_returnsFalse(String title) {
        Assertions.assertFalse(postValidator.isTitleValid(title));
    }



}
