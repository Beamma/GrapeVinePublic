package nz.ac.canterbury.seng302.gardenersgrove.security;

import nz.ac.canterbury.seng302.gardenersgrove.Agora.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * For generating tokens for Agora.
 */
@Service
public class AgoraTokenGenerator {

    @Value("${agora.app.id}")
    private String APP_ID;

    @Value("${agora.cert}")
    private String APP_CERTIFICATE;

    @Value("${CUSTOMER_KEY}")
    private String CUSTOMER_KEY;

    @Value("${CUSTOMER_SECRET}")
    private String CUSTOMER_SECRET;
    
    // Roles in Agora RTC
    public static final int ROLE_PUBLISHER = 1;  // Can publish and subscribe
    public static final int ROLE_SUBSCRIBER = 2; // Can only subscribe
    
    // Token validity in seconds (e.g., 3600 seconds = 1 hour)
    private static final int TOKEN_EXPIRATION_IN_SECONDS = 3600;

    private static final int PRIVILEGE_EXPIRATION_IN_SECONDS = 3600;

    /**
     * Generates a secure token to user for livestreams.
     *
     * @param channelName The name of the channel (livestream id)
     * @param uid The users id
     * @param role The role (host or subscriber)
     * @return the token
     */
    public String generateToken(String channelName, int uid, RtcTokenBuilder2.Role role) {
        // Initialize the RtcTokenBuilder2
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();

        // Build the token using RtcTokenBuilder2
        return tokenBuilder.buildTokenWithUid(
                APP_ID,
                APP_CERTIFICATE,
                channelName,
                uid,
                role,
                TOKEN_EXPIRATION_IN_SECONDS,
                PRIVILEGE_EXPIRATION_IN_SECONDS
        );
    }

    /**
     * @return appId of the instance of livestreaming we are using
     */
    public String getAppId() {
        return APP_ID;
    }

    /**
     * @return the API credentials for the Agora API encoded in Base64
     */
    public String getAPICredentials() {
        String credentials = CUSTOMER_KEY + ":" + CUSTOMER_SECRET;
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
