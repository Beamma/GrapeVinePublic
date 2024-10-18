package nz.ac.canterbury.seng302.gardenersgrove.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Transfer Object for weather.
 */
public class WeatherConditionsDTO {

    // Logger
    Logger logger = LoggerFactory.getLogger(WeatherConditionsDTO.class);

    // List of days weather
    private final List<Weather> weatherConditions;

    private final List<Weather> weatherConditionsHistory;

    // Error flag
    private boolean hasErrors = false;

    /**
     * Constructor creates empty list.
     */
    public WeatherConditionsDTO() {
        weatherConditions = new ArrayList<>();
        weatherConditionsHistory = new ArrayList<>();
    }

    /**
     * Creates weathers given JSON.
     * Contains ChatGPT code (date formatter)
     */
    public void createWeatherCondition(String response, int days) {

        // Define the output date formats
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

        try {
            // Create object mapper
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode data = objectMapper.readTree(response).get("forecast").get("forecastday");

            // Loop through days
            for (int day = 0; day < days; day++) {

                // Create a new weather
                Weather weather = new Weather();

                // Get the current day's data node
                JsonNode dayNode = data.get(day);
                if (dayNode == null) {
                    continue;
                }

                // Set icon
                weather.setIcon(dayNode.get("day").get("condition").get("icon").asText());

                // Set description
                weather.setDescription(dayNode.get("day").get("condition").get("text").asText());

                // Set temperature
                weather.setTempC(dayNode.get("day").get("avgtemp_c").floatValue());

                // Set humidity
                weather.setHumidity(dayNode.get("day").get("avghumidity").floatValue());

                // Set max wind
                weather.setMaxWind(dayNode.get("day").get("maxwind_kph").floatValue());

                // Get the date string
                String localTime = dayNode.get("date").asText();

                // Parse the input string to a LocalDate object
                LocalDate date = LocalDate.parse(localTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Set day
                if (day == 0) {
                    weather.setDay("Today");
                } else {
                    weather.setDay(date.format(dayFormatter));
                }

                // Set date
                weather.setDate(date.format(dateFormatter));

                // Add the weather object to the list
                weatherConditions.add(weather);
            }
        } catch (Exception e) {
            logger.info("JSON Error: " + e.getMessage());
            this.hasErrors = true;
        }
    }
    /**
     * Creates weather history given JSON.
     * @param responses An array of the responses for historical weather conditions
     */

    public void createWeatherConditionsHistory(ArrayList<String> responses) {
        try {
            // Create object mapper
            ObjectMapper objectMapper = new ObjectMapper();
            for (String response : responses) {
                JsonNode data = objectMapper.readTree(response).get("forecast").get("forecastday");

                // Create a new weather
                Weather weather = new Weather();

                // Get the current day's data node
                JsonNode dayNode = data.get(0);
                if (dayNode == null) {
                    continue;
                }

                // Set icon
                weather.setIcon(dayNode.get("day").get("condition").get("icon").asText());

                // Set description
                weather.setDescription(dayNode.get("day").get("condition").get("text").asText());

                // Set temperature
                weather.setTempC(dayNode.get("day").get("avgtemp_c").floatValue());

                // Set humidity
                weather.setHumidity(dayNode.get("day").get("avghumidity").floatValue());

                // Set max wind
                weather.setMaxWind(dayNode.get("day").get("maxwind_kph").floatValue());

                // Get the date string
                String localTime = dayNode.get("date").asText();

                // Parse the input string to a LocalDate object
                LocalDate date =
                        LocalDate.parse(localTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                // Define the output date formats
                DateTimeFormatter dayFormatter =
                        DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
                DateTimeFormatter dateFormatter =
                        DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

                // Set day
                weather.setDay(date.format(dayFormatter));

                // Set date
                weather.setDate(date.format(dateFormatter));

                // Add the weather object to the list
                weatherConditionsHistory.add(weather);
            }
        } catch (Exception e) {
            logger.info("JSON Error: " + e.getMessage());
            this.hasErrors = true;
        }
    }



    /**
     * Getter for weather conditions.
     * @return The weather conditions.
     */
    public List<Weather> getWeatherConditions() {
        return this.weatherConditions;
    }

    /**
     * Getter for weather conditions history.
     * @return The weather conditions history.
     */
    public List<Weather> getWeatherConditionsHistory() {
        return this.weatherConditionsHistory;
    }

    /**
     * Used to check for errors.
     * @return True if it has errors.
     */
    public boolean hasErrors() {
        return this.hasErrors;
    }

    /**
     * Used to set errors (if invalid API response).
     */
    public void setHasErrors() {
        this.hasErrors = true;
    }


    /**
     * Check if it is raining, using the current weather conditions (won't work unless createWeatherCondition has been called).
     * @return True if it is raining.
     */
    public boolean isRaining() {
        Weather currentWeather = weatherConditions.get(0);
        return currentWeather.getDescription().contains("Rain") ||
                currentWeather.getDescription().contains("Shower") ||
                currentWeather.getDescription().contains("rain") ||
                currentWeather.getDescription().contains("shower") ||
                currentWeather.getDescription().contains("drizzle");
    }

    /**
     * Check if the plant needs to be watered. The plant needs to be watered if it is not raining
     * and the weather has been sunny for the past two days.
     * @return True if the plant needs to be watered.
     */
    public boolean needToWater() {
        return (!isRaining() &&
                weatherConditionsHistory.get(1).getDescription().contains("Sunny") &&
                weatherConditionsHistory.get(2).getDescription().contains("Sunny"));
    }
}
