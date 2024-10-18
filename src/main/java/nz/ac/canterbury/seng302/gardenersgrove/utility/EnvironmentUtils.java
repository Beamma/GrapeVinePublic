package nz.ac.canterbury.seng302.gardenersgrove.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * A helper utility class for fetching information
 * about the applications current environment inc:
 * Current Profile(s): (Production or Staging)
 * Base Url
 */
@Component
public class EnvironmentUtils {

    @Autowired
    private Environment environment;

    private static final String DEPLOYED_BASE_URL = "https://csse-seng302-team900.canterbury.ac.nz";
    private static final String LOCAL_BASE_URL = "http://localhost:8080";


    /**
     * Get a list of the active profile(s)
     * @return production or staging or none if running locally
     */
    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }

    /**
     * Get the base url of the application server, dependent on whether the server
     * is running locally or on deployed.
     * @return The base url of the server
     */
    public String getBaseUrl() {
        if (Arrays.stream(getActiveProfiles()).anyMatch(profile -> profile.equals("staging") || profile.equals("production"))) {
            return DEPLOYED_BASE_URL;
        }
        return LOCAL_BASE_URL;
    }

    /**
     * Get the url extension of the server, whether it is on test or prod or local.
     * @return /test or /prod depending on deployed location, empty string for local.
     */
    public String getInstance() {
        if (Arrays.asList(getActiveProfiles()).contains("staging")) {
            return "/test";
        }
        if (Arrays.asList(getActiveProfiles()).contains("production")) {
            return "/prod";
        }
        return "";
    }
}
