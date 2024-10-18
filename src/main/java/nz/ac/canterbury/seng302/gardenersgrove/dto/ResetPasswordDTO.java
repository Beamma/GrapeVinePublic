package nz.ac.canterbury.seng302.gardenersgrove.dto;

/**
 * Data transfer object for the reset password page
 */
public class ResetPasswordDTO {

    private String token;
    private String password;
    private String passwordRepeat;

    /**
     * Constructor for the DTO
     */
    public ResetPasswordDTO() {
    }

    /**
     * Getter for token
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Setter for token
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Getter for password
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for password
     * @param password users password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for password
     * @return the password
     */
    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    /**
     * Setter for password
     * @param passwordRepeat users password
     */
    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }
}