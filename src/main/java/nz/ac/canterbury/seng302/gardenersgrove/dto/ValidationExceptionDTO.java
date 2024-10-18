package nz.ac.canterbury.seng302.gardenersgrove.dto;

/**
 * Data transfer object for exceptions
 */
public class ValidationExceptionDTO {
    /**
     * The field in which the error has occurred
     */
    private String field;

    /**
     * The code for the error
     */
    private int errorCode;

    /**
     * the error message for the given error to be shown to the user
     */
    private String message;

    /**
     * Is true if profanity is detected
     */
    private Boolean isProfane = false;

    public ValidationExceptionDTO(String field, int errorCode, String message) {
        this.field = field;
        this.errorCode = errorCode;
        this.message = message;
    }

    public ValidationExceptionDTO(String field, int errorCode, String message, Boolean isProfane) {
        this.field = field;
        this.errorCode = errorCode;
        this.message = message;
        this.isProfane = isProfane;
    }

    public String getField() {
        return field;
    }

    /**
     * Gets error code as a String, as the BindingResult.rejectValue method takes the error code as a String
     * @return String of error code
     */
    public String getErrorCode() {
        return String.valueOf(errorCode);
    }

    public String getMessage() {
        return message;
    }

    public Boolean getIsProfane() {
        return this.isProfane;
    }
}
