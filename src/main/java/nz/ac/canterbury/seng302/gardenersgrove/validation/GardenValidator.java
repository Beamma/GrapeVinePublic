package nz.ac.canterbury.seng302.gardenersgrove.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;

import org.springframework.web.client.HttpClientErrorException;
import java.util.*;

/**
 * Validator for Garden form field inputs
 */
public class GardenValidator {

    private ProfanityFilterService profanityFilterService;

    public GardenValidator(ProfanityFilterService profanityFilterService) {
        this.profanityFilterService = profanityFilterService;
    }

    /**
     * Checks whether a given field is empty (null or white space)
     *
     * @param field The field being checked
     * @return true if the field is empty, and false otherwise
     */
    public static boolean isFieldEmpty(String field) {
        return field == null || field.isBlank();
    }

    /**
     * Checks whether description is white space only
     *
     * @param description being checked
     * @return true if the field only contains white space
     */
    public static boolean isFieldBlank(String description) {
        if (description == null) {
            return false;
        }

        return description.isBlank();
    }

    /**
     * Checks whether a garden name contains only valid characters
     *
     * @param gardenName the name being checked
     * @return true if the name is valid, false otherwise
     */
    public static boolean isGardenNameValid(String gardenName) {
        return gardenName.matches("^[\\p{L}0-9 '.,-]+$") && !gardenName.matches("^-+$");
    }

    /**
     * Checks whether a field is a valid length (less than 256 characters)
     *
     * @param field the field being checked
     * @return true if the field is a valid length, false otherwise
     */
    public static boolean isFieldValidLength(String field) {
        return field.length() < 256;
    }

    /**
     * Checks whether a location contains only valid characters
     *
     * @param location the location being checked
     * @return true if the location is valid, false otherwise
     */
    public static boolean isLocationValid(String location) {
        return location.matches("^[\\p{L}0-9 '., -]+$");
    }

    /**
     * Checks whether a size contains only valid characters
     *
     * @param size the size being checked
     * @return true if the size is valid, false otherwise
     */
    public static boolean isSizeValid(String size) {
        return size.matches("^[0-9]*([,.][0-9]+)?$");   // Regex with help from chatGPT
    }

    /**
     * Checks whether a size is positive
     *
     * @param size the size being checked
     * @return true if the size is positive, false otherwise
     */
    public static boolean isSizePositive(Double size) {
        return size > 0;
    }

    /**
     * Checks whether the description is of valid length
     *
     * @param description the description
     * @return true if the description length is less than or equal to
     */
    public static boolean isDescriptionLengthValid(String description) {
        int descriptionLength = description.codePointCount(0, description.length());
        return (descriptionLength <= 512);
    }

    /**
     * Checks whether the description contains valid characters
     *
     * @param description the description
     * @return true if the description only contains special characters
     */
    public static boolean isDescriptionAllSpecialCharacters(String description) {
        String specialCharacters = "[" + "-/@#!*$%^&.'_+={}()" + "123456789" + "\\s" + "]+"; //regex from https://www.javamadesoeasy.com/2015/12/how-to-check-string-contains-only.html
        return description.matches(specialCharacters);
    }

    /**
     * Checks whether the description contains profanity
     *
     * @param description the description being screened for profanity
     * @return true if the description contains profanity, false otherwise
     * @throws JsonProcessingException if the API does not successfully  check the text, and sends a response in an
     * unexpected format
     * @throws HttpClientErrorException if the API key is incorrect
     */
    public boolean isProfane(String description) throws JsonProcessingException, HttpClientErrorException {
        return profanityFilterService.isTextProfane(description);
    }

    /**
     * Checks whether the garden tag is a valid tag or not
     * Regex from chatgpt
     * @param tag the tag being checked for validity
     * @return true if the tag is valid otherwise false
     */

    public static boolean isValidTag(String tag) {
        String allowedCharacters = "^[\\p{L}1234567890 '_\"-]+$";
        boolean valid = tag.matches(allowedCharacters) && !tag.isBlank();
        return valid;
    }

    /**
     * Checks whether the garden tag is a valid length or not
     * @param tag the tag being checked for length
     * @return true if the tag is valid otherwise false
     */
    public static boolean tagIsValidLength (String tag) {
        return tag.codePointCount(0, tag.length()) <= 25;
    }


    /**
     * Finds errors in the garden name
     *
     * @param name the candidate name being checked for errors
     * @return a ValidationExceptionDTO representing the first error found, if any, and null otherwise
     */
    private static ValidationExceptionDTO validateGardenName(String name) {
        if (isFieldEmpty(name)) {
            return new ValidationExceptionDTO("gardenName", 401, "Garden name cannot be empty");
        }
        if (!isGardenNameValid(name)) {
            return new ValidationExceptionDTO("gardenName", 401, "Garden name must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        }
        if (!isFieldValidLength(name)) {
            return new ValidationExceptionDTO("gardenName", 401, "Garden name must be less than 256 characters");
        }
        return null;
    }

    /**
     * Finds errors in the garden location
     *
     * @param location the candidate location being checked for errors
     * @return an ArrayList of ValidationExceptionDTOs representing the errors found, if any, and an empty ArrayList otherwise
     */
    private static ArrayList<ValidationExceptionDTO> validateGardenLocation(AddressDTO location) {
        ArrayList<ValidationExceptionDTO> exceptions = new ArrayList<>();

        if (isFieldEmpty(location.getCity()) || isFieldEmpty(location.getCountry())) {
            exceptions.add(new ValidationExceptionDTO("location", 401, "City and Country are required"));
        }
        if (!isFieldEmpty(location.getAddressLine1())) {
            if (!isFieldValidLength(location.getAddressLine1())) {
                exceptions.add(new ValidationExceptionDTO("location.addressLine1", 401, "Street address must be less than 256 characters"));
            }
            else if (!isLocationValid(location.getAddressLine1())) {
                exceptions.add(new ValidationExceptionDTO("location.addressLine1", 401, "Street address must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"));
            }
        }
        if (!isFieldEmpty(location.getSuburb())) {
            if (!isFieldValidLength(location.getSuburb())) {
                exceptions.add(new ValidationExceptionDTO("location.suburb", 401, "Suburb must be less than 256 characters"));
            }
            else if (!isLocationValid(location.getSuburb())) {
                exceptions.add(new ValidationExceptionDTO("location.suburb", 401, "Suburb must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"));
            }
        }
        if (!isFieldEmpty(location.getPostcode())) {
            if (!isFieldValidLength(location.getPostcode())) {
                exceptions.add(new ValidationExceptionDTO("location.postcode", 401, "Postcode must be less than 256 characters"));
            }
            else if (!isLocationValid(location.getPostcode())) {
                exceptions.add(new ValidationExceptionDTO("location.postcode", 401, "Postcode must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"));
            }
        }
        if (!isFieldEmpty(location.getCity())) {
            if (!isFieldValidLength(location.getCity())) {
                exceptions.add(new ValidationExceptionDTO("location.city", 401, "City must be less than 256 characters"));
            }
            else if (!isLocationValid(location.getCity())) {
                exceptions.add(new ValidationExceptionDTO("location.city", 401, "City name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"));
            }
        }
        if (!isFieldEmpty(location.getCountry())) {
            if (!isFieldValidLength(location.getCountry())) {
                exceptions.add(new ValidationExceptionDTO("location.country", 401, "Country must be less than 256 characters"));
            }
            else if (!isLocationValid(location.getCountry())) {
                exceptions.add(new ValidationExceptionDTO("location.country", 401, "Country name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes"));
            }
        }

        return exceptions;
    }

    /**
     * Finds errors in the garden description
     *
     * @param description the candidate description being checked for errors
     * @return a ValidationExceptionDTO representing the first error found, if any, and null otherwise
     */
    private ValidationExceptionDTO validateDescription(String description) {
        if (isFieldBlank(description)) {
            return new ValidationExceptionDTO("description", 401, "Description cannot contain only white space");
        }
        if (isFieldEmpty(description)) {
            return null;
        }
        if (!isDescriptionLengthValid(description) || isDescriptionAllSpecialCharacters(description)) {
            return new ValidationExceptionDTO("description", 401, "Description must be 512 characters or less and contain some text");
        }
        try {
            if (isProfane(description)) {
                return new ValidationExceptionDTO("description", 401, "The description does not match the language standards of the app");
            }
        } catch (HttpClientErrorException | JsonProcessingException e) {
            return new ValidationExceptionDTO("description", 401, "Description could not be checked for profanity at this time. Please try again later");
        }

        return null;
    }

    /**
     * Finds errors in the garden size
     *
     * @param size the candidate size string being checked for errors
     * @return a ValidationExceptionDTO representing the first error found, if any, and null otherwise
     */
    private static ValidationExceptionDTO validateGardenSize(String size) {
        if (isFieldEmpty(size)) {
            return null;
        }
        if (!isSizeValid(size)) {
            return new ValidationExceptionDTO("size", 401, "Garden size must be a positive number");
        }
        Double parsedSize = GardenUtils.parseSize(size);
        if (!isSizePositive(parsedSize)) {
            return new ValidationExceptionDTO("size", 401, "Garden size must be a positive number");
        }
        if (parsedSize >= 1_000_000_000) {  // Avoids integer overflow
            return new ValidationExceptionDTO("size", 401, "Garden size must be less than 1,000,000,000");
        }
        return null;
    }

    /**
     * Finds errors in the garden
     *
     * @param gardenDTO the candidate gardenDTO being checked for errors
     * @param skipDescriptionValidation is true if the description had not changed, false otherwise
     *                                  Reduces profanity API calls and allows to pass error modal resubmit
     * @return an ArrayList of ValidationExceptionDTOs representing the errors found, if any, and an empty ArrayList otherwise
     */
    public List<ValidationExceptionDTO> validateGarden(GardenDTO gardenDTO, Optional<Boolean> skipDescriptionValidation) {
        boolean skipDescription = skipDescriptionValidation.orElse(false);
        ArrayList<ValidationExceptionDTO> exceptions = new ArrayList<>();

        exceptions.add(validateGardenName(gardenDTO.getGardenName()));
        exceptions.addAll(validateGardenLocation(gardenDTO.getLocation()));
        if (!skipDescription) {
            exceptions.add(validateDescription(gardenDTO.getDescription()));
        }
        exceptions.add(validateGardenSize(gardenDTO.getSize()));

        exceptions.removeIf(Objects::isNull);
        return exceptions;
    }

}
