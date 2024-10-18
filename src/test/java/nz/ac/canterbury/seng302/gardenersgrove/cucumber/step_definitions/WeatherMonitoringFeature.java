package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class WeatherMonitoringFeature {
    private static MockMvc mockMvc;
    private ResultActions request;
    private static WeatherService mockWeatherService;
    private static GardenService gardenService;
    private  static UserService mockUserService;
    private static InternalResourceViewResolver viewResolver;

    private static GardenRepository mockGardenRepository;
    private static ProfanityFilterService mockProfanityFilterService;
    private static UserRepository mockUserRepository;
    private static PasswordEncoder mockPasswordEncoder;
    private static PlantRepository mockPlantRepository;
    private static Garden mockGarden1;
    private static Garden mockGarden2;
    private static User jane;
    private static SecurityContextHolder mockSecurityContextHolder;
    private static RestTemplate mockRestTemplate;
    private static WeatherConditionsDTO validWeatherDTO;

    private static WeatherConditionsDTO validWeatherDTONeedToWater;
    private static WeatherConditionsDTO invalidLocationWeatherDTO;
    private static WeatherConditionsDTO validWeatherDTORaining;
    private MockHttpServletRequestBuilder requestBuilder;
    private static TagRepository mockTagRepository;

    private static AddressDTO validLocation;
    @BeforeAll
    public static void setUp() throws ParseException, IOException, JSONException {

        // Mock and create all required classes for garden controller
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockUserRepository = Mockito.mock(UserRepository.class);
        mockPasswordEncoder = Mockito.mock(PasswordEncoder.class);
        mockPlantRepository = Mockito.mock(PlantRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        mockWeatherService = Mockito.mock(WeatherService.class);
        EmailService mockEmailService = Mockito.mock(EmailService.class);
        FriendService mockFriendService = Mockito.mock(FriendService.class);
        mockUserService = Mockito.mock(UserService.class);
        mockTagRepository = Mockito.mock(TagRepository.class);
        gardenService = new GardenService(mockGardenRepository, mockUserService, mockTagRepository);

        // Setup thing required for weatherAPI stuff
        int days = 4;
        String validData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherData.json")));
        String validDataDry = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherDataDry.json")));
        String invalidLocationData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/inValidLocationWeatherData.json")));
        String validDataRaining = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherDataRaining.json")));
        JSONArray validHistoryJsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherHistoryData.json"))));
        JSONArray validHistoryNeedToWaterJsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherHistoryDataNeedToWater.json"))));

        // Valid current weather, with history indicating no need for water

        validWeatherDTO = new WeatherConditionsDTO();
        validWeatherDTO.createWeatherCondition(validData, days);

        // Turn history JSON into a list
        ArrayList<String> validHistoryDataList = new ArrayList<>();
        for (int i = 0; i < validHistoryJsonArray.length(); i++) {
            validHistoryDataList.add(validHistoryJsonArray.getString(i));
        }
        validWeatherDTO.createWeatherConditionsHistory(validHistoryDataList);

        // Valid current weather, with history indicating a need for water

        validWeatherDTONeedToWater = new WeatherConditionsDTO();
        validWeatherDTONeedToWater.createWeatherCondition(validDataDry, days);

        // Turn history JSON into a list
        ArrayList<String> validHistoryNeedToWaterDataList = new ArrayList<>();
        for (int i = 0; i < validHistoryNeedToWaterJsonArray.length(); i++) {
            validHistoryNeedToWaterDataList.add(validHistoryNeedToWaterJsonArray.getString(i));
        }
        validWeatherDTONeedToWater.createWeatherConditionsHistory(validHistoryNeedToWaterDataList);


        invalidLocationWeatherDTO = new WeatherConditionsDTO();
        invalidLocationWeatherDTO.createWeatherCondition(invalidLocationData, days);

        validWeatherDTORaining = new WeatherConditionsDTO();
        validWeatherDTORaining.createWeatherCondition(validDataRaining, days);
        validWeatherDTORaining.createWeatherConditionsHistory(validHistoryDataList);

        // Setup User
        jane = new User();
        jane.setEmail("jane@email.com");
        jane.setPassword("Password1!");
        jane.setEnabled(true);
        jane.grantAuthority("ROLE_USER");
        jane.setId(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(jane);

        // Mock garden stuff
        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        mockGarden1 = new Garden("Garden 1", validLocation, jane);
        Mockito.when(mockGardenRepository.findById(1L)).thenReturn(Optional.ofNullable(mockGarden1));
        Mockito.when(mockFriendService.checkIfFriends(1l, 1l)).thenReturn(true);

        mockGarden2 = new Garden("Garden 2", validLocation, jane);
        Mockito.when(mockGardenRepository.findById(2L)).thenReturn(Optional.ofNullable(mockGarden2));
        Mockito.when(mockGardenRepository.save(mockGarden2)).thenReturn(mockGarden2);
        Mockito.when(mockFriendService.checkIfFriends(1l, 2l)).thenReturn(true);


        GardenController gardenController = new GardenController(gardenService, mockUserService, mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(gardenController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Given("I am on the garden details page for my own garden")
    public void i_am_on_the_garden_details_page_for_a_garden_i_own() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/garden/1");
    }

    @Then("the current weather for my location {string} is shown")
    public void the_current_weather_null_for_my_is_shown(String location) throws Exception {
        AddressDTO addressDTO = new AddressDTO(location, null, null, "Christchurch", "New Zealand", 172.64, -43.53);
        mockGarden1.setLocation(addressDTO);
        String cords = addressDTO.getLatitude() + "," + addressDTO.getLongitude();
        Mockito.when(mockWeatherService.getWeatherConditions(cords)).thenReturn(validWeatherDTO);

        request = mockMvc.perform(requestBuilder);
        request.andExpectAll(
                status().isOk(),
                model().attributeExists("weatherConditions"),
                view().name("gardenView")
        );

        WeatherConditionsDTO resultWeatherConditions = (WeatherConditionsDTO) request.andReturn().getModelAndView().getModel().get("weatherConditions");

        String formattedCurrentDate = "24 May";
        String iconImage = "//cdn.weatherapi.com/weather/64x64/day/176.png";
        String weatherDescription = "Patchy rain nearby";
        Double humidity = 86.0;
        Double temperature = 6.400000095367432;
        Double maxWind = 15.5;

        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getDay(), "Today");
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getDate(), formattedCurrentDate);
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getIcon(), iconImage);
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getDescription(), weatherDescription);
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getHumidity(), humidity);
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getTempC(), temperature);
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(0).getMaxWind(), maxWind);
        Assertions.assertFalse(resultWeatherConditions.hasErrors());

    }

    @Then("the future weather for my location {string} the future {int} days is shown")
    public void the_future_weather_for_my_the_future_days_is_shown(String location, Integer days) throws Exception {
        AddressDTO addressDTO = new AddressDTO(location, null, null, "Christchurch", "New Zealand", 172.64, -43.53);
        mockGarden1.setLocation(addressDTO);
        String cords = addressDTO.getLatitude() + "," + addressDTO.getLongitude();
        Mockito.when(mockWeatherService.getWeatherConditions(cords)).thenReturn(validWeatherDTO);

        request = mockMvc.perform(requestBuilder);
        request.andExpectAll(
                status().isOk(),
                model().attributeExists("weatherConditions"),
                view().name("gardenView")
        );

        WeatherConditionsDTO resultWeatherConditions = (WeatherConditionsDTO) request.andReturn().getModelAndView().getModel().get("weatherConditions");

        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().size(), days + 1);
        Assertions.assertFalse(resultWeatherConditions.hasErrors());
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(1).getDate(), "25 May");
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(2).getDate(), "26 May");
        Assertions.assertEquals(resultWeatherConditions.getWeatherConditions().get(3).getDate(), "27 May");
    }

    @And("the garden has a location {string} that can’t be found")
    public void the_garden_has_a_location_that_can_t_be_found(String location) {
        AddressDTO addressDTO = new AddressDTO(location, null, null, "Christchurch", "New Zealand", null, null);
        mockGarden1.setLocation(addressDTO);
    }
    @Then("a error message tells me {string}")
    public void a_error_message_tells_me(String string) throws Exception {
        String location = mockGarden1.getLocation().getLatitude() + "," + mockGarden1.getLocation().getLongitude();
        Mockito.when(mockWeatherService.getWeatherConditions(location)).thenReturn(invalidLocationWeatherDTO);

        request = mockMvc.perform(requestBuilder);
        request.andExpectAll(
                status().isOk(),
                model().attributeExists("weatherConditions"),
                view().name("gardenView")
        );

        WeatherConditionsDTO resultWeatherConditions = (WeatherConditionsDTO) request.andReturn().getModelAndView().getModel().get("weatherConditions");

        Assertions.assertTrue(resultWeatherConditions.hasErrors());
    }

    @And("the garden has not had rain in the past 2 days")
    public void theGardenHasNotHadRainInThePastDays() {
        AddressDTO addressDTO = new AddressDTO("Christchurch", null, null, "Christchurch", "New Zealand", 172.64, -43.53);
        mockGarden1.setLocation(addressDTO);
        String location = mockGarden1.getLocation().getLatitude() + "," + mockGarden1.getLocation().getLongitude();
        Mockito.when(mockWeatherService.getWeatherConditions(location)).thenReturn(validWeatherDTONeedToWater);
    }

    @Then("a message tells me {string}")
    public void aMessageTellsMe(String weatherMessage) throws Exception {
        request = mockMvc.perform(requestBuilder);
        request.andExpectAll(
                status().isOk(),
                view().name("gardenView"),
                model().attribute("weatherMessage", weatherMessage)
        );
    }

    @And("the garden has had rain in the past 2 days")
    public void theGardenHasHadRainInThePastDays() {
        String location = mockGarden1.getLocation().getLatitude() + "," + mockGarden1.getLocation().getLongitude();
        Mockito.when(mockWeatherService.getWeatherConditions(location)).thenReturn(validWeatherDTO);
    }

    @And("it is currently raining")
    public void itIsCurrentlyRaining() {
        String location = mockGarden1.getLocation().getLatitude() + "," + mockGarden1.getLocation().getLongitude();
        Mockito.when(mockWeatherService.getWeatherConditions(location)).thenReturn(validWeatherDTORaining);
    }

    @And("I click the close button on the weather alert")
    public void iClickTheCloseButtonOnTheWeatherAlert() throws GardenNotFoundException {
        mockGarden2 = gardenService.dismissGardenWeatherMessage(1L, true);
    }

    @Then("the weather alert is hidden")
    public void theWeatherAlertIsHidden() throws Exception {
        request = mockMvc.perform(requestBuilder);
        request.andExpectAll(
                status().isOk(),
                model().attributeExists("weatherConditions"),
                view().name("gardenView"),
                model().attributeDoesNotExist("weatherMessage")
        );
    }

    @And("the weather alert will not show until the next day")
    public void theWeatherAlertWillNotShowUntilTheNextDay() throws Exception {
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        mockGarden1.setWeatherDismissalDate(yesterday);

        request = mockMvc.perform(requestBuilder);
        request.andExpectAll(
                status().isOk(),
                model().attributeExists("weatherConditions"),
                view().name("gardenView"),
                model().attributeExists("weatherMessage")
        );
    }


}
