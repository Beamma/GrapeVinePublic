package nz.ac.canterbury.seng302.gardenersgrove.dto;

/**
 * Data transfer object for the forgot password page
 */
public class ForgotPasswordDTO {

    private String email;

    /**
     * Constructor for the DTO
     */
    public ForgotPasswordDTO() {
    }

    /**
     * Getter for email
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for email
     * @param email users email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}