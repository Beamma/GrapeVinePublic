package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

/**
 * Service to get the weather for a location
 */
@Service
public class WeatherService {
    private static final int NUM_DAYS_INTO_FUTURE = 3;
    private static final int NUM_DAYS_INTO_PAST = 3;
    private final String baseUrl = "https://api.weatherapi.com/v1/";
    private final RestTemplate restTemplate;
    Logger logger = LoggerFactory.getLogger(WeatherConditionsDTO.class);

    @Value("${weather.api.key}")
    private String apiKey;

    /**
     * Constructor for the WeatherService
     */
    public WeatherService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get the weather for a given location, cached for up to an hour
     * Documentation from: https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html
     *
     * @param location The location to get the weather for
     * @return The weather condition for the location
     */
    @Cacheable("weather")
    public WeatherConditionsDTO getWeatherConditions(String location) {

        // Log request
        logger.info("Weather API request for: " + location);

        // Generate array of date strings from 3 days ago to yesterday
        ArrayList<String> dateList = new ArrayList<>();
        for (int i = 1; i < (NUM_DAYS_INTO_PAST + 1); i++) {
            LocalDate date = LocalDate.now().minusDays(i - 1);
            Date dateDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateFormatted = dateFormat.format(dateDate);
            dateList.add(dateFormatted);
        }

        // Create weather DTO
        WeatherConditionsDTO weatherConditions = new WeatherConditionsDTO();
        ArrayList<String> responses = new ArrayList<>();

        // Send API request
        try {
            // Try sending get for current
            String currentWeatherUrl = baseUrl + "forecast.json?key=" + apiKey + "&q=" + location + "&days=" + NUM_DAYS_INTO_FUTURE;
            String currentWeatherResponse = restTemplate.getForObject(currentWeatherUrl, String.class);

            // Try sending get for history
            for (String date : dateList) {
                String historyUrl = baseUrl+"history.json?key="+apiKey+"&q="+location+"&dt="+date;
                String historyResponse = restTemplate.getForObject(historyUrl, String.class);
                responses.add(historyResponse);
            }

            // Create weather values given result
            weatherConditions.createWeatherCondition(currentWeatherResponse, NUM_DAYS_INTO_FUTURE);
            weatherConditions.createWeatherConditionsHistory(responses);

        } catch (RestClientException e) {

            // Set errors (API)
            weatherConditions.setHasErrors();
        }

        // Return weather DTO
        return weatherConditions;
    }

}
