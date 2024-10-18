package nz.ac.canterbury.seng302.gardenersgrove.utility;

import nz.ac.canterbury.seng302.gardenersgrove.validation.GardenValidator;

import java.io.IOException;

/**
 * Class for utility methods pertaining to gardens
 */
public class GardenUtils {

    /**
     * Converts a valid size input as a string to a double
     *
     * @param size the string being converted
     * @return the Double with the value in the string
     */
    public static Double parseSize(String size) {
        return Double.parseDouble(size.replace(",", "."));
    }

    /**
     * Generates the string that should be used to repopulate the garden name field
     *
     * @param gardenName the name as it originally appeared
     * @return the string with which the form field should be repopulated
     */
    public static String gardenNameFormRepopulation(String gardenName) {
        return (gardenName == null || gardenName.isBlank() || gardenName.length() > 255) ? "" : gardenName;
    }

    /**
     * Generates the string that should be used to repopulate the garden name field
     *
     * @param gardenName the name as it originally appeared
     * @param replacement the string that should replace the provided name if invalid
     * @return the string with which the form field should be repopulated
     */
    public static String gardenNameFormRepopulation(String gardenName, String replacement) {
        return (gardenName == null || gardenName.isBlank() || gardenName.length() > 255) ? replacement : gardenName;
    }

    /**
     * Generates the string that should be used to repopulate the location field
     *
     * @param location the location as it originally appeared
     * @return the string with which the form field should be repopulated
     */
    public static String locationFormRepopulation(String location) {
        return (location == null || location.isBlank() || location.length() > 255) ? "" : location;
    }

    /**
     * Generates the string that should be used to repopulate the location field
     *
     * @param location the location as it originally appeared
     * @param replacement the string that should replace the provided location if invalid
     * @return the string with which the form field should be repopulated
     */
    public static String locationFormRepopulation(String location, String replacement) {
        return (location == null || location.isBlank() || location.length() > 255) ? replacement : location;
    }

    /**
     * Generates the string to repopulate the description field
     *
     * @param description the original description
     * @return the string for repopulation
     */
    public static String descriptionFormRepopulation(String description) {
        return (description == null || description.isBlank()) ? "" : description;
    }
    public static String descriptionFormRepopulation(String description, String replacement) throws IOException {
        return (description == null || description.isBlank())?
                replacement :
                description;
    }

    /**\
     * Generates the string to repopulate the size field
     *
     * @param size the original size
     * @return the string for the repopulation of the size field
     */
    public static String sizeFormRepopulation(String size) {
        return (size == null || size.isBlank()) ? "" : size;
    }

    public static String sizeFormRepopulation(String size, String replacement) {
        return (size == null || size.isBlank()) ? replacement : size;
    }
}
