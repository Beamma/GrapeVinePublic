package nz.ac.canterbury.seng302.gardenersgrove.exception;

/**
 * Custom Exception for trying to update a Plant that does not exist
 */
public class PlantNotFoundException extends Exception {

    /**
     * Constructor for PlantNotoundException
     * @param errorMessage the message to display with the error
     */
    public PlantNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
