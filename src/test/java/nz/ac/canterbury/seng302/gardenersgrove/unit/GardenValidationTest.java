package nz.ac.canterbury.seng302.gardenersgrove.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.GardenValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GardenValidationTest {
    private static ProfanityFilterService mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
    private GardenValidator gardenValidator = new GardenValidator(mockProfanityFilterService);

    @BeforeAll
    public static void beforeEach() throws JsonProcessingException {
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString()) ).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));
    }
    @Test
    public void FieldGiven_FieldIsNull_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty(null);
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsEmpty_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsSpaces_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("     ");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsTabs_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("        ");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsTabsChars_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("\t\t\t");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsSpaceChars_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("\s\s\s");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsNewlineChars_TrueReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("\n\n\n");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsChars_FalseReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("Test");
        Assertions.assertFalse(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsCharsWithSpaces_FalseReturned() {
        var isEmpty = gardenValidator.isFieldEmpty("Test with spaces");
        Assertions.assertFalse(isEmpty);
    }

    @Test
    public void GardenNameGiven_NameContainsLetters_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsNumbers_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsNumbersAndLetters_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("123abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsLettersAndNumbers_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("abc123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsSpaces_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("   ");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsCapitalLetters_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("ABCDEFG");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsFullStops_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid(".");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsComma_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid(",");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsApostrophe_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("'");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsDash_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("-");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsAllValidChars_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("abcdefg ABCDEFG 123456 . , ' -");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsSpecialCharacters_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("Māori");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void GardenNameGiven_NameContainsSpecialCharactersCase2_TrueReturned() {
        var isValid = gardenValidator.isGardenNameValid("Müller");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsLetters_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsNumbers_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsNumbersAndLetters_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("123abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsLettersAndNumbers_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("abc123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsSpaces_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("   ");
        Assertions.assertTrue(isValid);
    }
    @Test
    public void LocationGiven_NameContainsSpace_TrueReturned() {
        var isValid = gardenValidator.isLocationValid(" ");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsCapitalLetters_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("ABCDEFG");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsFullStops_TrueReturned() {
        var isValid = gardenValidator.isLocationValid(".");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsComma_TrueReturned() {
        var isValid = gardenValidator.isLocationValid(",");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsApostrophe_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("'");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsDash_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("-");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationNameGiven_NameContainsSpecialCharacters_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("Māori");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationNameGiven_NameContainsSpecialCharactersCase2_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("Müller");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void LocationGiven_NameContainsAllValidChars_TrueReturned() {
        var isValid = gardenValidator.isLocationValid("abcdefg ABCDEFG 123456 . , ' -");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsEmptyLength_TrueReturned() {
        var isValid = gardenValidator.isFieldValidLength("");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsShortLength_TrueReturned() {
        var isValid = gardenValidator.isFieldValidLength("A short name");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsLength255_TrueReturned() {
        var isValid = gardenValidator.isFieldValidLength("A".repeat(255));
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsLength256_FalseReturned() {
        var isValid = gardenValidator.isFieldValidLength("A".repeat(256));
        Assertions.assertFalse(isValid);
    }

    @Test
    public void SizeGiven_SizeIsWholeNumber_TrueReturned() {
        var isValid = gardenValidator.isSizeValid("5");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsWholeNumberZero_TrueReturned() {
        var isValid = gardenValidator.isSizeValid("0");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsNegative_FalseReturned() {
        var isValid = gardenValidator.isSizeValid("-1");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void SizeGiven_SizeIsDecimal_TrueReturned() {
        var isValid = gardenValidator.isSizeValid("1.1");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsDecimalWithoutFirstDigit_TrueReturned() {
        var isValid = gardenValidator.isSizeValid(".1");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsEuropeanDecimal_TrueReturned() {
        var isValid = gardenValidator.isSizeValid("1,1");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsEuropeanDecimalWithoutFirstDigit_TrueReturned() {
        var isValid = gardenValidator.isSizeValid(",1");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsLongDecimal_TrueReturned() {
        var isValid = gardenValidator.isSizeValid("3.14159265358979");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsLongWholeNumber_TrueReturned() {
        var isValid = gardenValidator.isSizeValid("314159265358979");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void SizeGiven_SizeIsZero_FalseReturned() {
        var isValid = gardenValidator.isSizePositive(0.0);
        Assertions.assertFalse(isValid);
    }

    @Test
    public void SizeGiven_SizeIsNegativeDouble_FalseReturned() {
        var isValid = gardenValidator.isSizePositive(-5.0);
        Assertions.assertFalse(isValid);
    }

    @Test
    public void SizeGiven_SizeIsPositive_TrueReturned() {
        var isValid = gardenValidator.isSizePositive(6.0);
        Assertions.assertTrue(isValid);
    }

    @ParameterizedTest
    @ValueSource(strings = {"badWord"})
    public void profanitiesBySelf_TrueReturned(String desc) throws IOException {
        boolean isProfane = gardenValidator.isProfane(desc);
        Assertions.assertTrue(isProfane);
    }

    @ParameterizedTest
    @ValueSource(strings = {"word", "not", "profane"})
    public void nonProfanitiesBySelf_TrueReturned(String desc) throws IOException {
        boolean isProfane = gardenValidator.isProfane(desc);
        Assertions.assertFalse(isProfane);
    }

    @ParameterizedTest
    @ValueSource(strings = {"buttress", "petition", "lassie", "as she", "Robbo obgyn"})
    public void nestedProfanities_FalseReturned(String desc) throws IOException {
        boolean isProfane = gardenValidator.isProfane(desc);
        Assertions.assertFalse(isProfane);
    }

    @ParameterizedTest
    @ValueSource(strings = {"this sentence contains a badWord", "My description has the word badWord "})
    public void profanitiesInSentence_TrueReturned(String desc) throws IOException {
        boolean isProfane = gardenValidator.isProfane(desc);
        Assertions.assertTrue(isProfane);
    }

    @ParameterizedTest
    @ValueSource(strings = {"!badWord."})
    public void profanitiesInPunctuation_TrueReturned(String desc) throws IOException {
        boolean isProfane = gardenValidator.isProfane(desc);
        Assertions.assertTrue(isProfane);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "tag", "words", "ĒĪŌŪāēī", "Ẵ", "\"", "\uD811\uDD69"})
    public void tagValid_TrueReturned(String tag) {
        boolean isValid = gardenValidator.isValidTag(tag);
        Assertions.assertTrue(isValid);
    }
    @ParameterizedTest
    @ValueSource(strings = {"invalid?", "tag;", "|words|", "¶¶"})
    public void tagInvalid_FalseReturned(String tag) {
        boolean isValid = gardenValidator.isValidTag(tag);
        Assertions.assertFalse(isValid);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "garden name", "words", "ĒĪŌŪāēī", "'", "Ẵ", "123", "\uD811\uDD69"})
    public void nameValid_TrueReturned(String name) {
        boolean isValid = gardenValidator.isGardenNameValid(name);
        Assertions.assertTrue(isValid);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid?", "name;", "|words|", "¶¶", ""})
    public void nameInvalid_TrueReturned(String name) {
        boolean isValid = gardenValidator.isGardenNameValid(name);
        Assertions.assertFalse(isValid);
    }




}
