package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.GardenValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GardenUtilsTest {

    @Test
    public void sizeGiven_SizeIsZeroWholeNumber_ConversionCorrect() {
        var size = GardenUtils.parseSize("0");
        Assertions.assertEquals(0.0, size);
    }

    @Test
    public void SizeGiven_SizeIsZeroDecimal_ConversionCorrect() {
        var size = GardenUtils.parseSize("0.0");
        Assertions.assertEquals(0.0, size);
    }

    @Test
    public void SizeGiven_SizeIsZeroEuropeanDecimal_ConversionCorrect() {
        var size = GardenUtils.parseSize("0,0");
        Assertions.assertEquals(0.0, size);
    }

    @Test
    public void SizeGiven_SizeIsWholeNumber_ConversionCorrect() {
        var size = GardenUtils.parseSize("5");
        Assertions.assertEquals(5.0, size);
    }

    @Test
    public void SizeGiven_SizeIsDecimal_ConversionCorrect() {
        var size = GardenUtils.parseSize("1.0");
        Assertions.assertEquals(1.0, size);
    }

    @Test
    public void SizeGiven_SizeIsEuropeanDecimal_ConversionCorrect() {
        var size = GardenUtils.parseSize("1,0");
        Assertions.assertEquals(1.0, size);
    }

    @Test
    public void SizeGiven_SizeIsDecimalWithoutFirstDigit_ConversionCorrect() {
        var size = GardenUtils.parseSize(".7");
        Assertions.assertEquals(0.7, size);
    }

    @Test
    public void SizeGiven_SizeIsEuropeanDecimalWithoutFirstDigit_ConversionCorrect() {
        var size = GardenUtils.parseSize(",8");
        Assertions.assertEquals(0.8, size);
    }

    @Test
    public void GardenNameGiven_NameIsNull_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation(null);
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsEmpty_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("");
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsSpaces_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("    ");
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsSpaceChars_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("\s\s\s\s");
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsTabs_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("     ");
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsTabChars_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("\t\t");
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsNewLineChars_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("\n\n\n");
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIsValid_NameReturned() {
        var validName = "Valid name";
        var name = GardenUtils.gardenNameFormRepopulation(validName);
        Assertions.assertEquals(validName, name);
    }

    @Test
    public void GardenNameGiven_NameIs256Chars_EmptyStringReturned() {
        var name = GardenUtils.gardenNameFormRepopulation("a".repeat(256));
        Assertions.assertEquals("", name);
    }

    @Test
    public void GardenNameGiven_NameIs255Chars_NameReturned() {
        var validName = "a".repeat(255);
        var name = GardenUtils.gardenNameFormRepopulation(validName);
        Assertions.assertEquals(validName, name);
    }

    @Test
    public void LocationGiven_LocationIsNull_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation(null);
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsEmpty_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("");
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsSpaces_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("    ");
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsSpaceChars_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("\s\s\s\s");
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsTabs_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("     ");
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsTabChars_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("\t\t");
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsNewLineChars_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("\n\n\n");
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIsValid_NameReturned() {
        var validName = "Valid name";
        var loc = GardenUtils.locationFormRepopulation(validName);
        Assertions.assertEquals(validName, loc);
    }

    @Test
    public void LocationGiven_LocationIs256Chars_EmptyStringReturned() {
        var loc = GardenUtils.locationFormRepopulation("a".repeat(256));
        Assertions.assertEquals("", loc);
    }

    @Test
    public void LocationGiven_LocationIs255Chars_NameReturned() {
        var validName = "a".repeat(255);
        var loc = GardenUtils.locationFormRepopulation(validName);
        Assertions.assertEquals(validName, loc);
    }

    @Test
    public void GardenNameGiven_NameIsNull_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation(null, def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGiven_NameIsEmpty_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("", def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGiven_NameIsSpaces_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("    ", def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGiven_NameIsSpaceChars_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("\s\s\s\s", def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGiven_NameIsTabs_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("     ", def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGiven_NameIsTabChars_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("\t\t", def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGiven_NameIsNewLineChars_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("\n\n\n", def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGivenWithDefault_NameIsValid_NameReturned() {
        String def = "default";
        var validName = "Valid name";
        var name = GardenUtils.gardenNameFormRepopulation(validName, def);
        Assertions.assertEquals(validName, name);
    }

    @Test
    public void GardenNameGiven_NameIs256Chars_DefaultReturned() {
        String def = "default";
        var name = GardenUtils.gardenNameFormRepopulation("a".repeat(256), def);
        Assertions.assertEquals(def, name);
    }

    @Test
    public void GardenNameGivenWithDefault_NameIs255Chars_NameReturned() {
        String def = "default";
        var validName = "a".repeat(255);
        var name = GardenUtils.gardenNameFormRepopulation(validName, def);
        Assertions.assertEquals(validName, name);
    }

    @Test
    public void LocationGiven_LocationIsNull_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation(null, def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGiven_LocationIsEmpty_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("", def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGiven_LocationIsSpaces_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("    ", def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGiven_LocationIsSpaceChars_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("\s\s\s\s", def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGiven_LocationIsTabs_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("     ", def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGiven_LocationIsTabChars_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("\t\t", def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGiven_LocationIsNewLineChars_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("\n\n\n", def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGivenWithDefault_LocationIsValid_NameReturned() {
        String def = "default";
        var validName = "Valid name";
        var loc = GardenUtils.locationFormRepopulation(validName, def);
        Assertions.assertEquals(validName, loc);
    }

    @Test
    public void LocationGiven_LocationIs256Chars_DefaultReturned() {
        String def = "default";
        var loc = GardenUtils.locationFormRepopulation("a".repeat(256), def);
        Assertions.assertEquals(def, loc);
    }

    @Test
    public void LocationGivenWithDefault_LocationIs255Chars_NameReturned() {
        String def = "default";
        var validName = "a".repeat(255);
        var loc = GardenUtils.locationFormRepopulation(validName, def);
        Assertions.assertEquals(validName, loc);
    }
}
