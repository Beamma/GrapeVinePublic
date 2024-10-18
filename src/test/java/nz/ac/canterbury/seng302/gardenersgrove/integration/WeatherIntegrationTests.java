package nz.ac.canterbury.seng302.gardenersgrove.integration;

import static org.hamcrest.Matchers.containsString;

import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.ForgotPasswordListener;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnForgotPasswordEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WeatherIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // Create spy classes (For classes where methods should be run and checked)

    @SpyBean
    private GardenService gardenService;

    // Create mocked classes (For classes run by program but wanting to be ignored)

    @MockBean
    private UserAuthenticationService userAuthenticationService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RegistrationListener registrationListener;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private SecurityContextHolder securityContextHolder;

    @MockBean
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private ForgotPasswordListener forgotPasswordListener;

    @MockBean
    private OnForgotPasswordEvent onForgotPasswordEvent;

    @MockBean
    private FriendService friendService;

    @MockBean
    private WeatherService weatherService;

    // Set test variables
    private static final String errorMessage = "Location not found, please update your location to see the weather";

    // Weather DTO's
    private WeatherConditionsDTO validWeatherDTO;
    private WeatherConditionsDTO validWeatherDTONeedToWater;

    private WeatherConditionsDTO validWeatherDTOInvalidHistory;
    private WeatherConditionsDTO invalidLocationWeatherDTO;
    private WeatherConditionsDTO invalidDaysWeatherDTO;

    private WeatherConditionsDTO validWeatherHistoryDTO;
    private WeatherConditionsDTO invalidWeatherHistoryDTO;

    private WeatherConditionsDTO validWeatherHistoryNeedToWaterDTO;


    // Garden
    private Garden garden;
    private long gardenId;

    @BeforeEach
    public void before_each() throws IOException, JSONException {

        // Days
        int days = 4;

        // Get valid data
        String validData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherData.json")));

        // Get valid data, is dry
        String validDataDry = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherDataDry.json")));

        // Get invalid days data
        String invalidLocationData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/inValidLocationWeatherData.json")));

        // Get invalid days data
        String invalidDaysData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/inValidDaysWeatherData.json")));

        // Get valid historical data
        String validHistoryData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherHistoryData.json")));

        // Get invalid historical data
        String invalidHistoryData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/invalidWeatherHistoryData.json")));

        // Get valid historical data need to water
        String validHistoryNeedToWaterData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherHistoryDataNeedToWater.json")));

        // Valid current weather, history indicates no need to water
        this.validWeatherDTO = new WeatherConditionsDTO();
        validWeatherDTO.createWeatherCondition(validData, days);
        // Turn JSON into list
        JSONArray validHistoryJsonArray = new JSONArray(validHistoryData);
        ArrayList<String> validHistoryDataList = new ArrayList<>();
        for (int i = 0; i < validHistoryJsonArray.length(); i++) {
            validHistoryDataList.add(validHistoryJsonArray.getString(i));
        }
        validWeatherDTO.createWeatherConditionsHistory(validHistoryDataList);

        // Valid current weather, History indicates need to water
        this.validWeatherDTONeedToWater = new WeatherConditionsDTO();
        validWeatherDTONeedToWater.createWeatherCondition(validDataDry, days);
        // Turn JSON into list
        JSONArray validHistoryNeedToWaterJsonArray = new JSONArray(validHistoryNeedToWaterData);
        ArrayList<String> validHistoryNeedToWaterDataList = new ArrayList<>();
        for (int i = 0; i < validHistoryNeedToWaterJsonArray.length(); i++) {
            validHistoryNeedToWaterDataList.add(validHistoryNeedToWaterJsonArray.getString(i));
        }
        validWeatherDTONeedToWater.createWeatherConditionsHistory(validHistoryNeedToWaterDataList);

        // Valid current weather, invalid history
        this.validWeatherDTOInvalidHistory = new WeatherConditionsDTO();
        validWeatherDTOInvalidHistory.createWeatherCondition(validData, days);
        // Turn JSON into list
        JSONArray invalidHistoryJsonArray = new JSONArray(invalidHistoryData);
        ArrayList<String> invalidHistoryDataList = new ArrayList<>();
        for (int i = 0; i < invalidHistoryJsonArray.length(); i++) {
            invalidHistoryDataList.add(invalidHistoryJsonArray.getString(i));
        }
        validWeatherDTOInvalidHistory.createWeatherConditionsHistory(invalidHistoryDataList);

        // Invalid current weather (days), valid history
        this.invalidDaysWeatherDTO = new WeatherConditionsDTO();
        invalidDaysWeatherDTO.createWeatherCondition(invalidDaysData, days);
        invalidDaysWeatherDTO.createWeatherConditionsHistory(validHistoryDataList);

        // Invalid current weather (location), valid history
        this.invalidLocationWeatherDTO = new WeatherConditionsDTO();
        invalidLocationWeatherDTO.createWeatherCondition(invalidLocationData, days);
        invalidLocationWeatherDTO.createWeatherConditionsHistory(validHistoryDataList);

        // Create user
        User user = new User();
        user.setId(1L);
        user.setEmail("Valid@Email.com");
        user.setPassword("Password1!");
        user.grantAuthority("ROLE_USER");

        // Garden
        this.gardenId = 1;
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        this.garden = new Garden("Test Garden", validLocation, user);
        garden.setPlants(new ArrayList<Plant>());
        garden.setWeatherMessageDismissed(false);

        // Set up mocks for user service
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(gardenService.getGardenByID(gardenId)).thenReturn(Optional.ofNullable(garden));
        Mockito.when(gardenService.getGardensByUserId(user.getId())).thenReturn(new ArrayList<Garden>());
        Mockito.when(friendService.checkIfFriends(user.getId(), user.getId())).thenReturn(true);

        // Authentication mocks
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>(List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void GetGardenInformation_ValidData_ContainsWeathers() throws Exception {
        // Set valid data
        Mockito.when(weatherService.getWeatherConditions(garden.getLocation().getLatitude() + "," + garden.getLocation().getLongitude())).thenReturn(this.validWeatherDTO);

        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/garden/" + this.gardenId)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("gardenView"))

                // Check for each weather's day
                .andExpect(content().string(containsString(validWeatherDTO.getWeatherConditions().get(0).getDay())))
                .andExpect(content().string(containsString(validWeatherDTO.getWeatherConditions().get(1).getDay())))
                .andExpect(content().string(containsString(validWeatherDTO.getWeatherConditions().get(2).getDay())))
                .andExpect(content().string(containsString(validWeatherDTO.getWeatherConditions().get(3).getDay())));
    }

    @Test
    public void GetGardenInformation_InvalidLocation_ContainsWarning() throws Exception {
        // Set invalid data
        Mockito.when(weatherService.getWeatherConditions(garden.getLocation().getLatitude() + "," + garden.getLocation().getLongitude())).thenReturn(this.invalidLocationWeatherDTO);

        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/garden/" + this.gardenId)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("gardenView"))

                // Check for error message
                .andExpect(content().string(containsString(errorMessage)));

        // Check DTO contains errors
        Assertions.assertTrue(invalidLocationWeatherDTO.hasErrors());
    }

    @Test
    public void GetGardenInformation_ValidData_ContainsWeathersHistoryNoNeedToWater() throws Exception {
        // Set valid data
        Mockito.when(weatherService.getWeatherConditions(garden.getLocation().getLatitude() + "," + garden.getLocation().getLongitude())).thenReturn(this.validWeatherDTO);
        // Add validWeatherDTO to prevent thymeleaf parsing error
        Mockito.when(weatherService.getWeatherConditions(garden.getLocation().getLatitude() + "," + garden.getLocation().getLongitude())).thenReturn(this.validWeatherDTO);


        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/garden/"+this.gardenId)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("gardenView"))

                // Check that weather message is correct
                .andExpect(model().attributeExists("weatherMessage"));


    }

    @Test
    public void GetGardenInformation_ValidData_ContainsWeathersHistoryNeedToWater() throws Exception {
        // Set valid data
        Mockito.when(weatherService.getWeatherConditions(garden.getLocation().getLatitude() + "," + garden.getLocation().getLongitude())).thenReturn(this.validWeatherDTONeedToWater);
        // Add validWeatherDTO to prevent thymeleaf parsing error
        Mockito.when(weatherService.getWeatherConditions(garden.getLocation().getLatitude() + "," + garden.getLocation().getLongitude())).thenReturn(this.validWeatherDTONeedToWater);

        // Mock the request and check values

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/garden/"+this.gardenId)
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("gardenView"))

                // Check that weather message is correct
                .andExpect(model().attribute("weatherMessage", "There hasn’t been any rain recently, make sure to water your plants if they need it"));
    }

}