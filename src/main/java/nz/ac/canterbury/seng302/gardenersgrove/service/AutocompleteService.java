package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;

/**
 * Service for managing location autocompletion
 */
@Service
public class AutocompleteService {

    /**
     * Allows us to access the location API key form the .env file
     */
    @Value("${location.api.key}")
    private String apiKey;

    Logger logger = LoggerFactory.getLogger(AutocompleteService.class);

    /**
     * The maximum number of requests per second
     */
    private final int MAX_REQUESTS = 2;

    /**
     * The request count sent within the last second
     */
    private int requestCount = 0;

    /**
     * Ensures the rate limit for the number of requests doesn't exceed the specified maximum
     */
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Constructor for AutoCompleteService. Sets up scheduler to ensure that requests can only be sent at the pre-
     * -specified maximum rate
     */
    public AutocompleteService() {
        scheduler.scheduleAtFixedRate(() -> {
            requestCount = 0;
        }, 0, 1, TimeUnit.SECONDS); // code from https://stackoverflow.com/questions/20387881/scheduledexecutorservice-to-run-every-minute
    }

    /**
     * Queries the location API to get the data to autofill
     * @param input a string representing the section of the url in the API request for the input data, e.g. 'Christchur'
     * @param type (Optionally) the type of location we are hoping to autofill: e.g. city, country etc
     * @return the result of the request to the location API, unless the rate limit has been exceeded, in which case it
     * returns null
     */
    public Object getAutocomplete(String input, String type) {
        if (requestCount >= MAX_REQUESTS) {
            return null;
        }
        String typeParam = type == null ? "" : "&type=" + type;
        String url = String.format("https://api.geoapify.com/v1/geocode/autocomplete?text=%s%s&limit=3&apiKey=%s", input, typeParam, apiKey);
        RestTemplate restTemplate = new RestTemplate();
        requestCount++;
        return restTemplate.getForObject(url, Object.class);
    }
}
