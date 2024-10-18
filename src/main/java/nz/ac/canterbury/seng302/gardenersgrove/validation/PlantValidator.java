package nz.ac.canterbury.seng302.gardenersgrove.validation;

/**
 * Validator for Plant form field inputs
 */
public class PlantValidator {

    /**
     * Checks whether a given field is empty (null or white space)
     * @param field The field being checked
     * @return true if the field is empty, and false otherwise
     */
    public static boolean isFieldEmpty(String field) {
        return field == null || field.isBlank();
    }

    /**
     * Checks whether a plant name contains only valid characters
     * @param plantName the name being checked
     * @return true if the name is valid, false otherwise
     */
    public static boolean isPlantNameValid(String plantName) {
        return plantName.matches("^[a-zA-ZÀ-ÖØ-öø-ÿĀĒĪŌŪāēīōū0-9\\s',.-]+$") && plantName.trim().length() > 0;
    }

    /**
     * Checks whether a description contains only valid characters
     * @param description the description being checked
     * @return true if the description is valid, false otherwise
     */
    public static boolean isDescriptionValid(String description) {
        //return description.matches("^[a-zA-Z0-9\\s.,'-]*$");
        return description.matches("^[a-zA-ZÀ-ÖØ-öø-ÿĀĒĪŌŪāēīōū0-9\\s',.-]+$");

    }

    /**
     * Checks whether a description isnt over max chars
     * @param description the description being checked
     * @return true if the description is valid, false otherwise
     */
    public static boolean isDescriptionLengthValid(String description) {
        return description.length() <= 512;
    }

    /**
     * Checks whether a count contains only valid characters
     * @param count the count being checked
     * @return true if the count is valid, false otherwise
     */
    public static boolean isCountValid(String count) {
        return count.matches("^[1-9]+[0-9]*$");
    }

    /**
     * Checks whether a count is within a reasonable range
     * @param count the count being checked
     * @return true if the count is less than one billion
     */
    public static boolean isCountWithinRange(String count) {
        int parsedCount = 0;
        try {
            parsedCount = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return false;
        }
        return parsedCount < 1000000000;
    }

    /**
     * Checks whether a field is a valid length (less than 256 characters)
     * @param field the field being checked
     * @return true if the field is a valid length, false otherwise
     */
    public static boolean isNameValidLength(String field) {
        return field.length() < 256;
    }

    /**
     * Checks whether a date is valid (yyyy-mm-dd)
     * @param date the date being checked
     * @return true if the date is valid, false otherwise
     */
    public static boolean isDateValid(String date) {
        return date.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$");
    }
}
