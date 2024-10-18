package nz.ac.canterbury.seng302.gardenersgrove.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * Service to handle sending text to a third-party API for profanity checking
 */
@Service
public class ProfanityFilterService {

    /**
     * Base URL for the endpoint at which we check text for profanity
     */
    private static final String BASE_URL = "https://language.googleapis.com/v1/documents:moderateText";

    /**
     * In the returned JSON, the index of the 'moderationCategories' array at which the profanity confidence object
     * (of the format {"name": "profanity", "confidence": CONF}, where CONF is in [0, 1]
     */
    private static final int PROFANITY_INDEX = 2;

    /**
     * An initial threshold at which to record profanity
     */
    private static final double PROFANITY_THRESHOLD = 0.2;

    /**
     * Sends the requests
     */
    private final RestTemplate restTemplate;

    Logger logger = LoggerFactory.getLogger(ProfanityFilterService.class);

    /**
     * API key for use of the profanity checking API. In .env file
     */
    @Value("${profanity.filter.api.key}")
    private String apiKey;

    /**
     * Basic constructor to initialise the RestTemplate
     */
    public ProfanityFilterService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends text to the profanity checking API, and screens it for profanity
     * @param text the text being checked to see whether it contains profanity
     * @return true if the text is detected to have contained profanity, false otherwise
     * @throws JsonProcessingException if the API does not successfully  check the text, and sends a response in an
     * unexpected format
     * @throws HttpClientErrorException if the API key is incorrect
     */
    @Cacheable("profanity")
    public boolean isTextProfane(String text) throws JsonProcessingException, HttpClientErrorException {
        String url = BASE_URL + "?key=" + apiKey;

        // Set up request body in the required format
        HashMap<String, HashMap<String, String>> request = new HashMap<>();
        HashMap<String, String> document = new HashMap<>();
        document.put("type", "PLAIN_TEXT");
        document.put("content", text);
        request.put("document", document);

        String response = restTemplate.postForObject(url, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode data = objectMapper.readTree(response).get("moderationCategories").get(PROFANITY_INDEX);

        // SonarCube identifies that calls to logger can be relatively expensive and take up resources, should be done conditionally.
        if (logger.isInfoEnabled()) {
            text = text.replaceAll("[\n\r]", "_");
            logger.info(String.format("Checked \"%s\" for profanity: confidence = %s", text, data.get("confidence")));
        }

        return data.get("confidence").floatValue() > PROFANITY_THRESHOLD;
    }
}
