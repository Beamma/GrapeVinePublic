package nz.ac.canterbury.seng302.gardenersgrove.exception;

/**
 * Custom Exception for trying to update a Garden that does not exist
 */
public class GardenNotFoundException extends Exception {

    /**
     * Constructor for GardenNotoundException
     * @param errorMessage the message to display with the error
     */
    public GardenNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
