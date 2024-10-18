package nz.ac.canterbury.seng302.gardenersgrove.integration;


import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.context.annotation.Import;
import org.springframework.ui.ModelMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;


@SpringBootTest
@ActiveProfiles("test")
@Import(Garden.class)
public class GardenControllerTest {
    MockMvc mockMvc;

    List<Garden> databaseGardens = new ArrayList<>();

    Garden mockGarden;
    AddressDTO validLocation;

    Garden mockGardenWithDescription;

    Plant plant1;

    Plant plant2;

    @MockBean
    User user1;

    @MockBean
    UserService userService;

    @MockBean
    FriendService friendService;

    @MockBean
    WeatherService weatherService;

    GardenService gardenService;

    @MockBean
    UserRepository userRepository;


    GardenRepository gardenRepository;

    GardenController gardenController;

    ProfanityFilterService mockProfanityFilterService;

    @MockBean
    JavaMailSender mailSender;

    GardenDTO validGardenDTO;
    GardenDTO editedGardenDTO;

    @BeforeEach
    void setup() throws IOException, JSONException {
        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        gardenRepository = Mockito.mock(GardenRepository.class);
        Mockito.when(userRepository.save(Mockito.any())).then(returnsFirstArg());

        gardenService = Mockito.mock(GardenService.class);

        User user1 = Mockito.mock(User.class);
        Mockito.when(user1.getId()).thenReturn(1L);

        List<GrantedAuthority> authorities = new ArrayList<>();
        Authority authority = new Authority("ROLE_USER");
        authorities.add(new SimpleGrantedAuthority(authority.getRole()));
        Mockito.when(user1.getAuthorities()).thenReturn(authorities);

        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        Mockito.when(userService.validateUserId(String.valueOf(user1.getId()))).thenReturn(true);
        Mockito.when(userService.getById(String.valueOf(user1.getId()))).thenReturn(user1);


        String validData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherData.json")));
        WeatherConditionsDTO validWeatherDTO = new WeatherConditionsDTO();
        validWeatherDTO.createWeatherCondition(validData, 4);


        String validHistoryData = new String(Files.readAllBytes(Paths.get("src/test/resources/test-data/validWeatherHistoryData.json")));
        JSONArray validHistoryJsonArray = new JSONArray(validHistoryData);
        ArrayList<String> validHistoryDataList = new ArrayList<>();
        for (int i = 0; i < validHistoryJsonArray.length(); i++) {
            validHistoryDataList.add(validHistoryJsonArray.getString(i));
        }

        validWeatherDTO.createWeatherConditionsHistory(validHistoryDataList);

        Mockito.when(weatherService.getWeatherConditions(Mockito.anyString())).thenReturn(validWeatherDTO);

        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString()) ).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));


        Assertions.assertEquals(userService.getCurrentUser(), user1);

        // For testing getting ALL gardens from the database
        Mockito.when(gardenRepository.findAll()).thenReturn(databaseGardens);
        Mockito.when(gardenRepository.save(Mockito.any())).then(returnsFirstArg());
        GardenFilterService mockGardenFilter = new GardenFilterService(gardenRepository);
        // For testing getting gardens by ID
        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        Garden mockGarden = new Garden("Vegetable Garden", validLocation, 4.5, user1);
        Garden mockGarden2 = new Garden("Vegetable Garden", validLocation, user1);

        Garden mockGardenWithPlants = new Garden("Full Vegetable Garden", validLocation, user1);
        plant1 = new Plant(mockGardenWithPlants, "Name", 1, null, null, null);
        plant2 = new Plant(mockGardenWithPlants, "Name 2", 1, "Desc", Date.from(Instant.now()), null);
        mockGardenWithPlants.setPlants(List.of(plant1, plant2));

        validGardenDTO = new GardenDTO("The Patch", "Description", "1.5", validLocation);
        editedGardenDTO = new GardenDTO("New Name", "New desc", "35", validLocation);

        Garden garden1 = new Garden("Garden 1", validLocation, user1);
        Garden garden2 = new Garden("Garden 2", validLocation, user1);
        Garden garden3 = new Garden("Garden 3", validLocation, user1);
        Garden garden4 = new Garden("Garden 4", validLocation, user1);
        Mockito.when(gardenService.getGardens()).thenReturn(databaseGardens);
        Mockito.when(gardenService.getGardenByID(1L)).thenReturn(Optional.of(mockGarden));
        Mockito.when(gardenService.getGardenByID(2L)).thenReturn(Optional.of(mockGarden2));
        Mockito.when(gardenService.searchPublicGardensByUserIdAndName(user1.getId(), "Garden 3")).thenReturn(List.of(garden3));
        Mockito.when(gardenService.searchPublicGardensByUserIdAndName(user1.getId(), "Garden")).thenReturn(List.of(garden1, garden2, garden3, garden4));

        Garden mockGardenWithDescription = new Garden("Vegetable Garden", validLocation, 4.5, "Description", user1);
        Mockito.when(gardenService.getGardenByID(5L)).thenReturn(Optional.of(mockGardenWithDescription));

        Mockito.when(gardenService.getGardenByID(3L)).thenReturn(Optional.of(mockGardenWithPlants));
        Mockito.when(gardenService.getGardenByID(2L)).thenReturn(Optional.of(mockGarden2));
        Mockito.when(gardenService.getGardenByID(18L)).thenReturn(Optional.empty());
        Mockito.doAnswer(i -> {
            Garden g = i.getArgument(0);
            if (g.getGardenId() == null) {
                Garden gWithId = Mockito.spy(g);
                Mockito.when(gWithId.getGardenId()).thenReturn(5L);
                return gWithId;
            }
            return g;
        }).when(gardenService).addGarden(Mockito.any());

        // For testing getting ALL gardens from the database
        Mockito.when(gardenRepository.findAll()).thenReturn(databaseGardens);

        gardenController = new GardenController(gardenService, userService, friendService, weatherService, mockProfanityFilterService, mockGardenFilter);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(gardenController)
                .build();
    }

    @Test
    public void allFieldsNullTest() throws Exception {
        validGardenDTO.setGardenName(null);
        validGardenDTO.setDescription(null);
        validGardenDTO.setSize(null);
        validGardenDTO.getLocation().setCity(null);

        String invalidNameMessage = "Garden name cannot be empty";
        String invalidCityAndCountryMessage = "City and Country are required";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "gardenName", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedNameError = bindingResult.getFieldErrors("gardenName")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(invalidNameMessage));
        boolean containsExpectedLocationError = bindingResult.getFieldErrors("location")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(invalidCityAndCountryMessage));

        Assertions.assertAll(
                () -> Assertions.assertTrue(containsExpectedNameError),
                () -> Assertions.assertTrue(containsExpectedLocationError)
        );
    }

    @Test
    public void gardenNameNullTest() throws Exception {
        validGardenDTO.setGardenName(null);

        String invalidNameMessage = "Garden name cannot be empty";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "gardenName", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedNameError = bindingResult.getFieldErrors("gardenName")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(invalidNameMessage));

        Assertions.assertTrue(containsExpectedNameError);
    }

    @Test
    public void cityNullTest() throws Exception {
        validGardenDTO.getLocation().setCity(null);

        String invalidCityAndCountryMessage = "City and Country are required";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedLocationError = bindingResult.getFieldErrors("location")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(invalidCityAndCountryMessage));

        Assertions.assertTrue(containsExpectedLocationError);
    }

    @Test
    public void countryNullTest() throws Exception {
        validGardenDTO.getLocation().setCountry(null);

        String invalidCityAndCountryMessage = "City and Country are required";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedLocationError = bindingResult.getFieldErrors("location")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(invalidCityAndCountryMessage));

        Assertions.assertTrue(containsExpectedLocationError);
    }

    @Test
    public void addressLine1TooLongTest() throws Exception {
        String tooLongName =  "a".repeat(256);
        validGardenDTO.getLocation().setAddressLine1(tooLongName);

        String addressLine1ErrorMessage = "Street address must be less than 256 characters";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.addressLine1", "401")
                        )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.addressLine1")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(addressLine1ErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void suburbTooLongTest() throws Exception {
        String tooLongName =  "a".repeat(256);
        validGardenDTO.getLocation().setSuburb(tooLongName);


        String suburbErrorMessage = "Suburb must be less than 256 characters";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.suburb", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.suburb")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(suburbErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void postcodeTooLongTest() throws Exception {
        String tooLongName =  "a".repeat(256);
        validGardenDTO.getLocation().setPostcode(tooLongName);

        String postcodeErrorMessage = "Postcode must be less than 256 characters";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.postcode", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.postcode")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(postcodeErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void cityTooLongTest() throws Exception {
        String tooLongName =  "a".repeat(256);
        validGardenDTO.getLocation().setCity(tooLongName);

        String cityErrorMessage = "City must be less than 256 characters";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.city", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.city")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(cityErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void countryTooLongTest() throws Exception {
        String tooLongName =  "a".repeat(256);
        validGardenDTO.getLocation().setCountry(tooLongName);

        String countryErrorMessage = "Country must be less than 256 characters";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.country", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.country")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(countryErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void addressLine1InvalidCharacters() throws Exception {
        String invalidName =  "@%$#@%";
        validGardenDTO.getLocation().setAddressLine1(invalidName);

        String addressLine1ErrorMessage = "Street address must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.addressLine1", "401")
                        )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.addressLine1")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(addressLine1ErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void suburbInvalidCharactersTest() throws Exception {
        String invalidName =  "@%$#@%";
        validGardenDTO.getLocation().setSuburb(invalidName);

        String expectedErrorMessage = "Suburb must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.suburb", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.suburb")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void postcodeInvalidCharactersTest() throws Exception {
        String invalidName =  "@%$#@%";
        validGardenDTO.getLocation().setPostcode(invalidName);

        String expectedErrorMessage = "Postcode must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.postcode", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.postcode")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void cityInvalidCharactersTest() throws Exception {
        String invalidName =  "@%$#@%";
        validGardenDTO.getLocation().setCity(invalidName);

        String expectedErrorMessage = "City name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.city", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.city")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void countryInvalidCharactersTest() throws Exception {
        String invalidName =  "@%$#@%";
        validGardenDTO.getLocation().setCountry(invalidName);

        String expectedErrorMessage = "Country name must only include letters, numbers, spaces, commas, dots, hyphens or apostrophes";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "location.country", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedAddressError = bindingResult.getFieldErrors("location.country")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedAddressError);
    }

    @Test
    public void allFieldsButCountryAndCityNullTest() throws Exception {
        validGardenDTO.getLocation().setAddressLine1(null);
        validGardenDTO.getLocation().setPostcode(null);
        validGardenDTO.getLocation().setSuburb(null);

        mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/5"),
                        model().hasNoErrors()
                );
    }

    @Test
    public void blueSkyWithoutSizeTest() throws Exception {
        validGardenDTO.setSize(null);

        mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/5"),
                        model().hasNoErrors()
                );
    }

    @Test
    public void blueSkyWithSizeTest() throws Exception {
        mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/5"),
                        model().hasNoErrors()
                );
    }

    @Test
    public void blueSkyWithoutDescription() throws Exception {
        validGardenDTO.setDescription(null);
        mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/5"),
                        model().hasNoErrors()
                );
    }

    @Test
    public void validNamesDontYieldErrors() throws Exception {
        validGardenDTO.setDescription(" . ' , - ");
        mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/5"),
                        model().hasNoErrors()
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "       ", "   ", "\n\n"})
    public void emptyNamesYieldErrors(String name) throws Exception {
        validGardenDTO.setGardenName(name);

        String expectedErrorMessage = "Garden name cannot be empty";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "gardenName", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("gardenName")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @Test
    public void tooLongNameYieldsErrors() throws Exception {
        validGardenDTO.setGardenName("a".repeat(256));

        String expectedErrorMessage = "Garden name must be less than 256 characters";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "gardenName", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("gardenName")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }


    @ParameterizedTest
    @ValueSource(strings = {"@#$%", "@'-"})
    public void invalidNamesReturnErrors(String name) throws Exception {
        validGardenDTO.setGardenName(name);

        String expectedErrorMessage = "Garden name must only include letters, numbers, spaces, dots, hyphens or apostrophes";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "gardenName", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("gardenName")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"513 chars", "123", "@#$%"})
    public void invalidDescriptionReturnsErrors(String desc) throws Exception {
        validGardenDTO.setDescription(desc.equals("513 chars") ? "a".repeat(513) : desc);

        String invalidDescriptionMessage = "Description must be 512 characters or less and contain some text";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "description", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean expectedDescriptionError = bindingResult.getFieldErrors("description")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(invalidDescriptionMessage));

        Assertions.assertTrue(expectedDescriptionError);
    }

    @ParameterizedTest
    @ValueSource(strings = {".3", ",5", "5,3", "7", "999999999"})
    public void validSizesDontReturnErrors(String size) throws Exception {
        validGardenDTO.setSize(size);
        mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/5"),
                        model().hasNoErrors()
                );
    }


    @ParameterizedTest
    @ValueSource(strings = {"-3", "-3.5", "0", "7.", "7,"})
    public void invalidSizesReturnErrors(String size) throws Exception {
        validGardenDTO.setSize(size);

        String expectedErrorMessage = "Garden size must be a positive number";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "size", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("size")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @Test
    public void sizeTooLargeReturnsErrors() throws Exception {
        validGardenDTO.setSize("1000000000");

        String expectedErrorMessage = "Garden size must be less than 1,000,000,000";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "size", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("size")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @Test
    public void ValidEditProvided_GardenExists_Returns302() throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(1L)).thenReturn(true);
        databaseGardens.add(mockGarden);
        mockMvc.perform(put("/garden/add/1")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        status().is(302),
                        redirectedUrl("/garden/1")
                );
    }

    @Test
    public void ValidEditProvided_GardenDoesNotExist_Returns403() throws Exception {
        mockMvc.perform(put("/garden/add/18")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        model().attribute("status", 403),
                        view().name("error")
                );
    }

    @Test
    public void InvalidEditProvided_GardenExists_Returns400AndAutofillsInvalidFields() throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(1L)).thenReturn(true);
        editedGardenDTO.setGardenName("");

        String expectedErrorMessage = "Garden name cannot be empty";

        MvcResult result = mockMvc.perform(put("/garden/add/1")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "gardenName", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("gardenName")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @Test
    public void validEditWithDescription() throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(1L)).thenReturn(true);

        mockMvc.perform(put("/garden/add/1")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/1"),
                        model().hasNoErrors()
                );
    }

    @Test
    public void invalidEdit_DescriptionTooLong() throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(5L)).thenReturn(true);
        editedGardenDTO.setDescription("a".repeat(513));

        String expectedErrorMessage = "Description must be 512 characters or less and contain some text";

        MvcResult result = mockMvc.perform(put("/garden/add/5")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "description", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("description")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @Test
    public void invalidEdit_DescriptionAllSpecialCharacters() throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(5L)).thenReturn(true);
        editedGardenDTO.setDescription("@@@");

        String expectedErrorMessage = "Description must be 512 characters or less and contain some text";

        MvcResult result = mockMvc.perform(put("/garden/add/5")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "description", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("description")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    @Test
    public void invalidEdit_DescriptionContainsProfanity() throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(5L)).thenReturn(true);
        editedGardenDTO.setDescription("This is a description that contains the profanity: badWord.");

        String expectedErrorMessage = "The description does not match the language standards of the app";

        MvcResult result = mockMvc.perform(put("/garden/add/5")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "description", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsExpectedError = bindingResult.getFieldErrors("description")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(expectedErrorMessage));

        Assertions.assertTrue(containsExpectedError);
    }


    @Test
    public void gardenRouteIdIsChar() throws Exception {
            mockMvc.perform(get("/garden/A"))
                    .andExpectAll(
                            status().is(404)
                    );
    }

    @Test
    public void gardenRouteIdIsSpeechMark() throws Exception {
            mockMvc.perform(get("/garden/\""))
                    .andExpectAll(
                            status().is(404)
                    );
    }

    @Test
    public void gardenRouteIdIsNegativeNum() throws Exception {
            mockMvc.perform(get("/garden/-1"))
                    .andExpectAll(
                            status().is(404)
                    );
    }

    @Test
    public void gardenRouteIdIsMultipleChar() throws Exception {
            mockMvc.perform(get("/garden/AAA"))
                    .andExpectAll(
                            status().is(404)
                    );
    }


    @Test
    public void gardenRouteIdIsValidWithSize() {
        Mockito.when(friendService.checkIfFriends(1L, 1L)).thenReturn(true);
        try {
            MvcResult mvcResult = mockMvc.perform(get("/garden/1"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

            Garden garden = (Garden) modelMap.getAttribute("garden");

            assertEquals("Vegetable Garden", garden.getName());
            assertEquals(validLocation, garden.getLocation());
            assertEquals(4.5, garden.getSize(), 0.001);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void gardenRouteIdIsValidWtihOutSize() {
        Mockito.when(friendService.checkIfFriends(1L, 1L)).thenReturn(true);
        try {
            MvcResult mvcResult = mockMvc.perform(get("/garden/2"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

            Garden garden = (Garden) modelMap.getAttribute("garden");

            assertEquals("Vegetable Garden", garden.getName());
            assertEquals(validLocation, garden.getLocation());
            assertNull(garden.getSize());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void gardenRouteIdIsOutOfRange() {

        try {
            mockMvc.perform(get("/garden/99"))
                    .andExpectAll(
                            model().attribute("status", 403),
                            model().attributeDoesNotExist("gardens"),
                            view().name("error")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenNoGardensInDataBase_ReturnAttributeNoGardens() {

        User user5 = Mockito.mock(User.class);
        Mockito.when(user5.getId()).thenReturn(5L);
        Mockito.when(userService.getCurrentUser()).thenReturn(user5);
        Mockito.when(userService.validateUserId("5")).thenReturn(true);
        Mockito.when(userService.getById("5")).thenReturn(user5);
        Mockito.when(friendService.checkIfFriends(5L, 5L)).thenReturn(true);
        try {
            mockMvc.perform(get("/garden/list/5" ))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("noGardens"))
                    .andExpect(model().attribute(
                            "noGardens", true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void userWithGardens_doesntHaveNoGardensFlag() {
        try {
            databaseGardens.add(mockGarden);
            MvcResult result = mockMvc.perform(get("/garden/list/1" + user1.getId().toString()))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelAndView modelAndView = result.getModelAndView();
            List<Garden> gardens = (List<Garden>) modelAndView.getModel().get("gardens");
            assertFalse(modelAndView.getModel().containsKey("noGardens"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenGardenHasPlants_WhenGardenRequested_PlantsAreIncluded() {
        Mockito.when(friendService.checkIfFriends(1L, 1L)).thenReturn(true);
        try {
            databaseGardens.add(mockGarden);
            MvcResult result = mockMvc.perform(get("/garden/3"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelAndView modelAndView = result.getModelAndView();
            Garden garden = (Garden) modelAndView.getModel().get("garden");
            List<Plant> plants = garden.getPlants();
            Assertions.assertEquals(2, plants.size());
            Assertions.assertEquals(plant1, plants.get(0));
            Assertions.assertEquals(plant2, plants.get(1));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenGardenHasNoPlants_WhenGardenRequested_PlantsAreNotIncluded() {
        Mockito.when(friendService.checkIfFriends(1L, 1L)).thenReturn(true);
        try {
            databaseGardens.add(mockGarden);
            MvcResult result = mockMvc.perform(get("/garden/2"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelAndView modelAndView = result.getModelAndView();
            Garden garden = (Garden) modelAndView.getModel().get("garden");
           Assertions.assertNull(garden.getPlants());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUserAccessingOwnGardenList_ReturnThereGardens () {
        Mockito.when(friendService.checkIfFriends(1L, 1L)).thenReturn(true);
        try {
            mockMvc.perform(get("/garden/list/1"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("gardens"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUserAccessingOtherGardenList_Return403 () {
        try {
            mockMvc.perform(get("/garden/list/2"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attributeDoesNotExist("gardens"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUserAccessingNonExistingGardenList_Return403 () {
        try {
            MvcResult result = mockMvc.perform(get("/garden/list/99"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attributeDoesNotExist("gardens"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenTheyOwn_ReturnGardenInfo () {
        Mockito.when(friendService.checkIfFriends(1L, 1L)).thenReturn(true);
        try {
            MvcResult result = mockMvc.perform(get("/garden/1"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("garden"))
                    .andExpect(view().name("gardenView"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenTheyDontOwn_Return403AndError () {
        User user3 = Mockito.mock(User.class);
        Mockito.when(user3.getId()).thenReturn(3L);
        Garden mockGarden3 = new Garden("Vegetable Garden", validLocation, user3);
        Mockito.when(gardenService.getGardenByID(3L)).thenReturn(Optional.of(mockGarden3));

        try {

            mockMvc.perform(get("/garden/3"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attributeDoesNotExist("garden"))
                    .andExpect(view().name("error"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenDoesntExist_Return403AndError () {
        Mockito.when(gardenService.checkGardenOwnership(99L)).thenReturn(false);
        try {
            MvcResult result = mockMvc.perform(get("/garden/99"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attributeDoesNotExist("garden"))
                    .andExpect(view().name("error"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenEditTheyOwn_ReturnGardenInfo () {
        Mockito.when(gardenService.checkGardenOwnership(1L)).thenReturn(true);
        try {
            MvcResult result = mockMvc.perform(get("/garden/add/1"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("gardenDTO"))
                    .andExpect(view().name("addGardenForm"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenEditDontOwn_Return403AndError () {
        Mockito.when(gardenService.checkGardenOwnership(3L)).thenReturn(false);
        try {
            mockMvc.perform(get("/garden/add/3"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attributeDoesNotExist("garden"))
                    .andExpect(view().name("error"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenEditDoesntExist_Return403AndError () {
        try {
            MvcResult result = mockMvc.perform(get("/garden/add/99"))
                    .andExpect(model().attribute("status", 403))
                    .andExpect(model().attributeDoesNotExist("garden"))
                    .andExpect(view().name("error"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1GardenEditTheyOwn_ReturnGardenInfo () throws Exception {
        Mockito.when(gardenService.checkGardenOwnership(1L)).thenReturn(true);
        mockMvc.perform(put("/garden/add/1")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/1"),
                        model().hasNoErrors()
                );
    }

    @Test
    public void GivenUser1GardenEditDontOwn_Return403AndError () {
        Mockito.when(gardenService.checkGardenOwnership(2L)).thenReturn(false);
        try {
            mockMvc.perform(put("/garden/add/2").with(csrf())
                            .flashAttr("gardenDTO", validGardenDTO))
                    .andExpectAll(
                            view().name("error"),
                            model().attribute("error", "Forbidden, you do not own this garden"),
                            model().attribute("status", 403)
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1GardenEditDoesntExist_Return403AndError () {
        try {
            mockMvc.perform(put("/garden/add/99").with(csrf())
                            .flashAttr("gardenDTO", validGardenDTO))
                    .andExpectAll(
                            view().name("error"),
                            model().attribute("error", "Forbidden, you do not own this garden"),
                            model().attribute("status", 403));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UserBrowsesGardens_PageLessThanOne_ReturnsPageOne() {
        List<Garden> emptyGardens = new ArrayList<>();
        Page<Garden> page = new PageImpl<>(emptyGardens);
        Mockito.when(gardenRepository.findRecentPublicGardens(Mockito.any())).thenReturn(page);

        try {
            MvcResult result = mockMvc.perform(get("/garden/browse").with(csrf())
                            .param("page", "0"))
                    .andExpectAll(
                            status().is(200),
                            view().name("browseGardens")
                    ).andReturn();

            BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");
            Assertions.assertEquals(1,browseGardenDTO.getParsedPage());
            assertNull(browseGardenDTO.getGardens());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UserBrowsesGardens_PageInvalidNumber_ReturnsPage() {
        List<Garden> emptyGardens = new ArrayList<>();
        Page<Garden> page = new PageImpl<>(emptyGardens);
        Mockito.when(gardenRepository.findRecentPublicGardens(Mockito.any())).thenReturn(page);

        try {
            MvcResult result = mockMvc.perform(get("/garden/browse").with(csrf())
                            .param("page", "A"))
                    .andExpectAll(
                            status().is(200),
                            view().name("browseGardens")
                    ).andReturn();

            BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");
            Assertions.assertEquals(1,browseGardenDTO.getParsedPage());
            assertNull(browseGardenDTO.getGardens());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UserBrowsesGardens_HasSearchQuery_ReturnsResults() {
        String searchQuery = "test";

        List<Garden> gardens = new ArrayList<>(
                Arrays.asList(
                        new Garden("Test Garden", validLocation, user1),
                        new Garden("Test Garden 2", validLocation, user1)
                )
        );
        Page<Garden> page = new PageImpl<>(gardens);

        Mockito.when(gardenRepository.findAllPublicGardensByNamePageable(searchQuery, PageRequest.of(0, 9))).thenReturn(page);

        try {
            MvcResult result = mockMvc.perform(get("/garden/browse").with(csrf())
                            .param("search", searchQuery))
                    .andExpectAll(
                            status().is(200),
                            view().name("browseGardens")
                    ).andReturn();

            BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");
            Assertions.assertEquals(1,browseGardenDTO.getParsedPage());
            Assertions.assertEquals(browseGardenDTO.getGardens(), gardens);
            Assertions.assertEquals(browseGardenDTO.getSearchSize(), gardens.size());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UserBrowsesGardens_SearchHasNoResults_SearchErrorPresent() {
        String searchQuery = "test";

        List<Garden> gardens = new ArrayList<>();
        Page<Garden> page = new PageImpl<>(gardens);
        Mockito.when(gardenRepository.findAllPublicGardensByNamePageable(searchQuery, PageRequest.of(0, 9))).thenReturn(page);

        try {
            MvcResult result = mockMvc.perform(get("/garden/browse").with(csrf())
                            .param("search", searchQuery))
                    .andExpectAll(
                            status().is(200),
                            view().name("browseGardens")
                    ).andReturn();

            BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");
            Assertions.assertEquals(1,browseGardenDTO.getParsedPage());
            assertNull(browseGardenDTO.getGardens());
            Assertions.assertEquals("No gardens match your search",browseGardenDTO.getSearchError());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UserBrowsesRecentGardens_ManuallyEntersTooHighPage_ReturnsLastPage() {
        int highPage = 999;
        List<Garden> gardens = new ArrayList<>(
                Arrays.asList(
                        new Garden("Test Garden", validLocation, user1),
                        new Garden("Test Garden 2", validLocation, user1)
                )
        );
        Page<Garden> populatedPage = new PageImpl<>(gardens);
        Page<Garden> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 2);

        Mockito.when(gardenRepository.findRecentPublicGardens(PageRequest.of(highPage - 1, 10))).thenReturn(emptyPage);
        Mockito.when(gardenRepository.findRecentPublicGardens(PageRequest.of(0, 10))).thenReturn(populatedPage);

        try {
            MvcResult result = mockMvc.perform(get("/garden/browse").with(csrf())
                            .param("page", String.valueOf(highPage)))
                    .andExpectAll(
                            status().is(200),
                            view().name("browseGardens")
                    ).andReturn();

            BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");
            Assertions.assertEquals(1,browseGardenDTO.getParsedPage());
            Assertions.assertEquals(browseGardenDTO.getGardens(), gardens);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void UserSearchesGardens_ManuallyEntersTooHighPage_ReturnsLastPage() {
        String searchQuery = "test";
        int highPage = 999;
        List<Garden> gardens = new ArrayList<>(
                Arrays.asList(
                        new Garden("Test Garden", validLocation, user1),
                        new Garden("Test Garden 2", validLocation, user1)
                )
        );
        Page<Garden> populatedPage = new PageImpl<>(gardens);
        Page<Garden> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 9), 2);
        Mockito.when(gardenRepository.findAllPublicGardensByNamePageable(searchQuery, PageRequest.of(0, 9))).thenReturn(populatedPage);
        Mockito.when(gardenRepository.findAllPublicGardensByNamePageable(searchQuery, PageRequest.of(highPage - 1, 9))).thenReturn(emptyPage);

        try {
            MvcResult result = mockMvc.perform(get("/garden/browse").with(csrf())
                            .param("search", searchQuery)
                            .param("page", String.valueOf(highPage)))
                    .andExpectAll(
                            status().is(200),
                            view().name("browseGardens")
                    ).andReturn();

            BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");
            Assertions.assertEquals(1,browseGardenDTO.getParsedPage());
            Assertions.assertEquals(browseGardenDTO.getGardens(), gardens);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "test1, true",
            "test2, true",
            "test3, true",
            "nonexistent, false"
    })
    void checkTag_ReturnsExpectedResult(String tag, boolean expectedResult) throws Exception {
        // Set up valid tags
        List<Tag> tags = List.of(new Tag("test1"), new Tag("test2"), new Tag("test3"));

        // Setup mock
        Mockito.when(gardenService.getPublicTags()).thenReturn(tags);

        // Perform request and check result
        mockMvc.perform(get("/tags/check").with(csrf())
                        .param("tag", tag))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedResult)));
    }
    @Test
    void ProfanityServiceDown_GetErrorMessageOnPost() throws Exception {

        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "HTTP error"));
        String apiDownErrorMessage = "Description could not be checked for profanity at this time. Please try again later";

        MvcResult result = mockMvc.perform(post("/garden/add")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attributeHasFieldErrorCode("gardenDTO", "description", "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        boolean containsApiDownError = bindingResult.getFieldErrors("description")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(apiDownErrorMessage));

        Assertions.assertTrue(containsApiDownError);
    }

    @Test
    void ProfanityServiceDown_DescChange_GetErrorMessageOnPut() throws Exception {
        // Mock API error
        Mockito.when(gardenService.checkGardenOwnership(5L)).thenReturn(true);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "HTTP error"));
        String apiDownErrorMessage = "Description could not be checked for profanity at this time. Please try again later";

        // Set up descriptions
        String oldDesc = "Description";
        String newDesc = "New desc";

        // Put edited garden
        MvcResult result = mockMvc.perform(put("/garden/add/5")
                        .flashAttr("gardenDTO", editedGardenDTO))
                .andExpectAll(
                        view().name("addGardenForm"),
                        model().attribute("apiErrorOpen", true),
                        model().attribute("descriptionCopy", oldDesc),
                        model().attributeHasFieldErrorCode("gardenDTO", "description", "401")
                )
                .andReturn();

        // Get binding result
        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT

        // Check binding result for API error
        boolean containsApiDownError = bindingResult.getFieldErrors("description")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(apiDownErrorMessage));

        // Assertions
        Mockito.verify(gardenService, Mockito.times(1)).checkDescriptionNoChange(5L, newDesc);
        Assertions.assertFalse(gardenService.checkDescriptionNoChange(5L, newDesc));
        Assertions.assertTrue(containsApiDownError);
    }

    @Test
    void ProfanityServiceDown_DescNoChange_ReturnNoErrorOnPut() throws Exception {
        // Mock API error
        Mockito.when(gardenService.checkGardenOwnership(5L)).thenReturn(true);
        Garden garden5 = new Garden("The Patch",  validLocation, 1.5, "Description", user1);
        Mockito.when(gardenRepository.findById(5L)).thenReturn(Optional.of(garden5));

        // Set up descriptions
        String validDesc = validGardenDTO.getDescription();
        Mockito.when(gardenService.checkDescriptionNoChange(5L, validDesc)).thenReturn(true);

        // Put unedited garden
        MvcResult result = mockMvc.perform(put("/garden/add/5")
                        .flashAttr("gardenDTO", validGardenDTO))
                .andExpectAll(
                        view().name("redirect:/garden/5"),
                        model().hasNoErrors()
                )
                .andReturn();

        // Assertions
        Mockito.verify(gardenService, Mockito.times(1)).checkDescriptionNoChange(5L, validDesc);
        Assertions.assertTrue(gardenService.checkDescriptionNoChange(5L, validDesc));
        Mockito.verify(mockProfanityFilterService, Mockito.times(0)).isTextProfane(Mockito.anyString());
    }

}
