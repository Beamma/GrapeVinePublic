package nz.ac.canterbury.seng302.gardenersgrove.exception;

/**
 * Custom Exception for trying to update a Friend that does not exist
 */
public class BadRequestException extends Exception {

    /**
     * Constructor for FriendNotoundException
     * @param errorMessage the message to display with the error
     */
    public BadRequestException(String errorMessage) {
        super(errorMessage);
    }
}
