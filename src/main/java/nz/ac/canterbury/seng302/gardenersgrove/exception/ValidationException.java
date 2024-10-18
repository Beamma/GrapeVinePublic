package nz.ac.canterbury.seng302.gardenersgrove.exception;

/**
 * The exception thrown by the authentication service.
 */
public class ValidationException extends Exception {

    private final String field;
    private final String errorCode;

    /**
     * Constructor for validation exception
     * @param field The error field
     * @param errorCode The error code
     * @param message The error message
     */
    public ValidationException(String field, int errorCode, String message) {
        super(message);
        this.field = field;
        this.errorCode = String.valueOf(errorCode);
    }

    /**
     * Getter for the error filed
     */
    public String getField() {
        return field;
    }

    /**
     * Getter for the error code
     * @return The error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
