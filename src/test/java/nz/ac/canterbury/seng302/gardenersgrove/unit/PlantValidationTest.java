package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.validation.PlantValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PlantValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"1000000000", "1000000001", "10000000000"}) // Last check would trigger an exception trying to parse the integer
    public void CountGiven_CountIsTooLarge_FalseReturned(String tooLargeCount) {
        Assertions.assertFalse(PlantValidator.isCountWithinRange(tooLargeCount));
    }

    @Test
    public void CountGiven_CountIsWithinRange_TrueReturned() {
        Assertions.assertTrue(PlantValidator.isCountWithinRange("999999999"));
    }

    @Test
    public void FieldGiven_FieldIsEmpty_TrueReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsSpaces_TrueReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("     ");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsTabs_TrueReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("        ");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsTabsChars_TrueReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("\t\t\t");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsSpaceChars_TrueReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("\s\s\s");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsNewlineChars_TrueReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("\n\n\n");
        Assertions.assertTrue(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsChars_FalseReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("Test");
        Assertions.assertFalse(isEmpty);
    }

    @Test
    public void FieldGiven_FieldIsCharsWithSpaces_FalseReturned() {
        var isEmpty = PlantValidator.isFieldEmpty("Test with spaces");
        Assertions.assertFalse(isEmpty);
    }

    @Test
    public void PlantNameGiven_NameContainsLetters_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsNumbers_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsNumbersAndLetters_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("123abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsLettersAndNumbers_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("abc123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsSpaces_FalseReturned() {
        var isValid = PlantValidator.isPlantNameValid("   ");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsCapitalLetters_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("ABCDEFG");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsFullStops_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid(".");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsComma_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid(",");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsApostrophe_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("'");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsDash_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("-");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void PlantNameGiven_NameContainsAllValidChars_TrueReturned() {
        var isValid = PlantValidator.isPlantNameValid("abcdefg ABCDEFG 123456 . , ' -");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsLetters_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsNumbers_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsNumbersAndLetters_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("123abc");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsLettersAndNumbers_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("abc123");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsSpaces_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("   ");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsCapitalLetters_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("ABCDEFG");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsFullStops_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid(".");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsComma_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid(",");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsApostrophe_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("'");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsDash_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("-");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_NameContainsAllValidChars_TrueReturned() {
        var isValid = PlantValidator.isDescriptionValid("abcdefg ABCDEFG 123456 . , ' -");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsEmptyLength_TrueReturned() {
        var isValid = PlantValidator.isNameValidLength("");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsShortLength_TrueReturned() {
        var isValid = PlantValidator.isNameValidLength("A short name");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsLength255_TrueReturned() {
        var isValid = PlantValidator.isNameValidLength("A".repeat(255));
        Assertions.assertTrue(isValid);
    }

    @Test
    public void FieldGiven_FieldIsLength256_FalseReturned() {
        var isValid = PlantValidator.isNameValidLength("A".repeat(256));
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsWholeNumber_TrueReturned() {
        var isValid = PlantValidator.isCountValid("5");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void CountGiven_CountIsWholeNumberZero_FalseReturned() {
        var isValid = PlantValidator.isCountValid("0");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsNegative_FalseReturned() {
        var isValid = PlantValidator.isCountValid("-1");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsDecimal_FalseReturned() {
        var isValid = PlantValidator.isCountValid("1.1");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsDecimalWithoutFirstDigit_FalseReturned() {
        var isValid = PlantValidator.isCountValid(".1");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsEuropeanDecimal_FalseReturned() {
        var isValid = PlantValidator.isCountValid("1,1");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsEuropeanDecimalWithoutFirstDigit_FalseReturned() {
        var isValid = PlantValidator.isCountValid(",1");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsLongDecimal_FalseReturned() {
        var isValid = PlantValidator.isCountValid("3.14159265358979");
        Assertions.assertFalse(isValid);
    }

    @Test
    public void CountGiven_CountIsLongWholeNumber_TrueReturned() {
        var isValid = PlantValidator.isCountValid("314159265358979");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_DescriptionIsNoLength_ReturnsTrue() {
        var isValid = PlantValidator.isDescriptionLengthValid("");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_DescriptionIsOneLength_ReturnsTrue() {
        var isValid = PlantValidator.isDescriptionLengthValid("A");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_DescriptionIsFiveHunderdTwelveLength_ReturnsTrue() {
        var isValid = PlantValidator.isDescriptionLengthValid("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Assertions.assertTrue(isValid);
    }

    @Test
    public void DescriptionGiven_DescriptionIsFiveHunderdThirteenLength_ReturnsFalse() {
        var isValid = PlantValidator.isDescriptionLengthValid("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Assertions.assertFalse(isValid);
    }

    @ParameterizedTest
    @ValueSource(strings = {"11-12-202", "1-11-2020", "1-1-2020", "111-11-2020", "11-111-2020", "11-11-20200", "abc"})
    public void DateGiven_DateIsInvalid_ReturnsFalse(String date) {
        var isValid = PlantValidator.isDateValid(date);
        Assertions.assertFalse(isValid);
    }

    @Test
    public void DateGiven_DateIsValid_ReturnsTrue() {
        var isValid = PlantValidator.isDateValid("2020-11-11");
        Assertions.assertTrue(isValid);
    }
}
