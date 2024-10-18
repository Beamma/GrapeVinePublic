package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class WeatherConditionsDTOTest {

    private WeatherConditionsDTO weatherConditionsDTO;
    private String validData;
    private String inValidLocationData ;
    private String inValidDaysData;
    private ArrayList<String> validHistoryData;

    private ArrayList<String> invalidHistoryData;
    private final int DAYS_INTO_FUTURE = 4;

    @BeforeEach
    public void beforeEach() throws IOException, JSONException {

        // Create new weather dto
        this.weatherConditionsDTO = new WeatherConditionsDTO();

        // Get valid data
        this.validData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherData.json")));

        // Get invalid days data
        this.inValidLocationData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/inValidLocationWeatherData.json")));

        // Get invalid days data
        this.inValidDaysData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/inValidDaysWeatherData.json")));

        // Get valid historical data
        JSONArray validHistoryJsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherHistoryData.json"))));
        this.validHistoryData = new ArrayList<>();
        for (int i = 0; i < validHistoryJsonArray.length(); i++) {
            this.validHistoryData.add(validHistoryJsonArray.getString(i));
        }

        // Get invalid historical data
        JSONArray inValidHistoryJsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/invalidWeatherHistoryData.json"))));
        this.invalidHistoryData = new ArrayList<>();
        for (int i = 0; i < inValidHistoryJsonArray.length(); i++) {
            this.invalidHistoryData.add(inValidHistoryJsonArray.getString(i));
        }
    }

    @Test
    void GivenValidJson_CreateValidWeathers () {
        // Act
        weatherConditionsDTO.createWeatherCondition(validData, DAYS_INTO_FUTURE);

        // Assert
        // Check no errors
        Assertions.assertFalse(weatherConditionsDTO.hasErrors());

        // Check number of days
        Assertions.assertEquals(DAYS_INTO_FUTURE, weatherConditionsDTO.getWeatherConditions().size());
    }

    @Test
    void GivenInValidLocationJson_Errors () {
        // Act
        weatherConditionsDTO.createWeatherCondition(inValidLocationData, DAYS_INTO_FUTURE);

        // Assert
        // Check errors
        Assertions.assertTrue(weatherConditionsDTO.hasErrors());
    }

    @Test
    void GivenInValidDaysJson_ReturnsOnlyValidDays () {
        // Act
        weatherConditionsDTO.createWeatherCondition(inValidDaysData, DAYS_INTO_FUTURE);

        // Assert
        // Check errors
        Assertions.assertEquals(1, weatherConditionsDTO.getWeatherConditions().size());
        Assertions.assertFalse(weatherConditionsDTO.hasErrors());

    }

    @Test
    void givenValidHistoricalData_createValidHistoricalWeather() {
        // Act
        weatherConditionsDTO.createWeatherConditionsHistory(validHistoryData);

        // Assert
        // Check no errors
        Assertions.assertFalse(weatherConditionsDTO.hasErrors());

        // Check number of days
        Assertions.assertEquals(3, weatherConditionsDTO.getWeatherConditionsHistory().size());
    }

    @Test
    void givenInvalidHistoricalData_createInvalidHistoricalWeather() {
        // Act
        weatherConditionsDTO.createWeatherConditionsHistory(invalidHistoryData);

        // Assert
        // Check errors
        Assertions.assertTrue(weatherConditionsDTO.hasErrors());
    }

    @Test
    void GivenInValidLocationJson_WeatherHistory_Errors () {
        // Act
        ArrayList<String> invalidResponses = new ArrayList<>();
        invalidResponses.add(inValidLocationData);
        weatherConditionsDTO.createWeatherConditionsHistory(invalidResponses);

        // Assert
        // Check errors
        Assertions.assertTrue(weatherConditionsDTO.hasErrors());
    }
}
