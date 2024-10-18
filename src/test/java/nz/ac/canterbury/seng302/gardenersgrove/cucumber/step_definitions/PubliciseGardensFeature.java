package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;


import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class PubliciseGardensFeature {
    @Autowired
    UserService userService;

    @Autowired
    GardenRepository gardenRepository;

    @Autowired
    PlantService plantService;

    @Autowired
    PlantRepository plantRepository;

    @Autowired
    UserRepository userRepository;
    @Autowired
    TagRepository tagRepository;

    @Autowired
    GardenService gardenService;
    WeatherService weatherService;

    @Autowired
    FriendService friendService;

    @PersistenceContext
    private EntityManager entityManager;

    private ProfanityFilterService mockProfanityFilterService;

    GardenDTO gardenDTOValidDescription;
    GardenDTO gardenDTODifferentValidDescription;
    GardenDTO gardenDTOInvalidDescription;
    GardenDTO gardenDTONullDescription;
    GardenDTO gardenDTOProfaneDescription;

    GardenDTO gardenDTONoDescription;

    ResultActions result;

    MockHttpServletRequestBuilder request;

    MockMvc mockMvc;

    GardenController gardenController;

    @Before("@U19")
    public void setup() throws IOException {

        // Create Garden DTOs

        gardenDTOValidDescription = new GardenDTO("My Garden", "Description", "2.0", new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null));

        gardenDTODifferentValidDescription = new GardenDTO("My Garden", "Different Description", "2.0", new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null));

        gardenDTOInvalidDescription = new GardenDTO("My Garden", "a".repeat(513), "2.0", new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null));

        gardenDTONullDescription = new GardenDTO("My Garden", null, "2.0", new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null));

        gardenDTOProfaneDescription = new GardenDTO("My Garden", "badWord", "2.0", new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null));

        gardenDTONoDescription = new GardenDTO("My Garden", "", "2.0", new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null));

        // Set up authenticated user
        User mockUser = new User("mockUser@gmail.com", "2001-01-01", "Mock", "User", false, "password", "password");
        userService.addUser(mockUser);
        gardenRepository.save(new Garden(gardenDTOValidDescription, mockUser));
        Authentication auth = new UsernamePasswordAuthenticationToken(mockUser.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Get A Valid Weather DTO From Json File
        String weatherCurrentJson = Files.readString(Paths.get("src/test/resources/test-data/validWeatherData.json"));
        String weatherHistory = Files.readString(Paths.get("src/test/resources/test-data/validWeatherHistoryData.json"));

        WeatherConditionsDTO mockWeatherConditionsDTO = new WeatherConditionsDTO();
        ArrayList<String> responses = new ArrayList<String>() {{add(weatherHistory);}};
        mockWeatherConditionsDTO.createWeatherConditionsHistory(responses);
        mockWeatherConditionsDTO.createWeatherCondition(weatherCurrentJson, 3);
        weatherService = Mockito.mock(WeatherService.class);
        Mockito.when(weatherService.getWeatherConditions(Mockito.any())).thenReturn(new WeatherConditionsDTO());



        // Mock profanity service
        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));
        gardenController = new GardenController(gardenService, userService, friendService, weatherService, mockProfanityFilterService, new GardenFilterService(gardenRepository));

        mockMvc = MockMvcBuilders.standaloneSetup(gardenController).build();

    }

    //AC 1

    @After("@U19")
    @Transactional
    public void tearDown() {
        gardenRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM GARDEN").executeUpdate();
    }

    @Given("I am on the garden details page for a garden I am the owner of")
    public void i_am_on_the_garden_details_page_for_a_garden_i_am_the_owner_of() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/garden/1"))
                .andExpect(status().is(200))
                .andExpect(view().name("gardenView"));
    }


    @When("I click the check box marked \"Make my garden public\"")
    public void iClickTheCheckBoxMarked() throws Exception {
        request = MockMvcRequestBuilders.put("/garden/1").with(csrf()).param("isPublic", "true");
        result = mockMvc.perform(request);
        result.andExpect(status().is(302));
    }

    @Then("my garden will be set to public")
    @Transactional
    public void myGardenWillBeSetToPublic() {
        Optional<Garden> garden = gardenRepository.findById(1L);
        Assertions.assertTrue(garden.isPresent());
        Assertions.assertTrue(garden.get().isPublicGarden());
    }

    //AC 2

    @Given("I am on the form to create a new garden")
    public void iAmOnTheFormToCreateANewGarden() {
        request = MockMvcRequestBuilders.post("/garden/add");
    }

    @When("I fill in valid details of garden name {string} city {string} country {string} size {string} and description of {string}")
    public void iFillInValidDetailsOfGardenNameLocationSizeAndDescription(String gardenName, String city, String country, String gardenSize, String description) throws Exception {
        AddressDTO location = new AddressDTO(null, null, null, city, country, null, null);
        GardenDTO gardenDTO = new GardenDTO(gardenName, description, gardenSize, location);
        request.flashAttr("gardenDTO", gardenDTO);
        result = mockMvc.perform(request);
    }

    @Then("the garden is created with a description {string}")
    public void theGardenIsCreatedWithADescription(String desc) {
        Assertions.assertTrue(gardenRepository.findById(3L).isPresent());
        Assertions.assertEquals(desc, gardenRepository.findById(3L).get().getDescription());
    }

    //AC 3

    @Given("I am on the form to edit a garden with id {int}")
    public void iAmOnTheFormToEditAGarden(Integer integer) {
        request = MockMvcRequestBuilders.put("/garden/add/" + integer);
    }

    @When("I add a description {string}")
    public void iAddADescription(String gardenDescription) {
        AddressDTO location = new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My garden", gardenDescription, "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
    }

    @When("I submit the edit garden form")
    @Transactional
    public void iSubmitTheEditGardenForm() throws Exception {
        result = mockMvc.perform(request);
    }

    @Then("the garden is updated with the description {string}")
    @Transactional
    public void theGardenIsUpdatedWithADescription(String addedDescription) {
        Optional<Garden> garden = gardenRepository.findById(4L);
        Assertions.assertEquals(addedDescription, garden.get().getDescription());

    }

    //AC 4

    @When("I remove the garden description")
    public void iRemoveTheGardenDescription() throws Exception {
        AddressDTO location = new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My garden", "", "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
        result = mockMvc.perform(request);
    }

    @Then("the garden is updated without a description")
    public void theGardenIsUpdatedWithoutADescription() {
        Assertions.assertNull(gardenRepository.findById(5L).get().getDescription());
    }

    //AC 5.1

    @When("I enter a garden description that is longer than {int} characters")
    public void iEnterAGardenDescriptionThatIsLongerThanCharacters(Integer int1) {
        AddressDTO location = new AddressDTO(null, null, null, "Christchurch", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My garden", "a".repeat(int1 + 1), "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
    }

    @Then("A {string} message {string} is displayed")
    public void aMessageDescriptionMustBeCharactersOrLessAndContainSomeTextIsDisplayed(String errorField, String errorMessage) throws Exception {
        MvcResult output = result
                .andExpect(model().attributeHasFieldErrorCode("gardenDTO", errorField, "401"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) output.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT
        boolean containsExpectedError = bindingResult.getFieldErrors(errorField)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

    // AC 8.1

    @Given("the profanity API is down")
    public void the_profanity_api_is_down() throws Exception {
        // Mock API is down
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
    }

    @Given("I have entered valid values to create a garden")
    public void i_have_entered_valid_values_to_create_a_garden() throws Exception {
        request = MockMvcRequestBuilders.post("/garden/add");
        AddressDTO location = new AddressDTO(null, null, null, "Wellington", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My API-Less Garden", "Description", "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
        result = mockMvc.perform(request);
    }

    @When("I submit the create garden form")
    public void i_submit_the_create_garden_form() throws Exception {
        result = mockMvc.perform(request);
    }

    @Then("the garden is not added")
    public void the_garden_is_not_added() {
        Assertions.assertEquals(1, gardenRepository.findAll().size());
    }

    @Then("a modal pops up with the option to continue without description")
    public void a_modal_pops_up_with_the_option_to_continue_without_description() {
        // Front-end specific element - Must be tested manually
    }

    // AC 8.2

    @Given("I have entered valid values to edit a garden with id {int}")
    public void i_have_entered_valid_values_to_edit_a_garden(Integer integer) throws Exception {
        request = MockMvcRequestBuilders.put("/garden/add/" + integer);
        AddressDTO location = new AddressDTO(null, null, null, "Wellington", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My API-Less Garden", "Edited description", "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
        result = mockMvc.perform(request);
    }
    @Then("the garden is not edited")
    public void the_garden_is_not_edited() {
        Assertions.assertNotEquals("Edited description", gardenRepository.findById(11L).get().getDescription());}

    @Then("a modal pops up with the option to continue without updating description")
    public void a_modal_pops_up_with_the_option_to_continue_without_updating_description() {
        // Front-end specific element - Must be tested manually
    }

    // AC 9

    @When("I am on the modal")
    public void i_am_on_the_modal() {
        // Front-end specific element - Must be tested manually
    }

    @Given("I have entered in valid details of garden name {string} city {string} country {string} size {string} and description of {string}")
    public void i_have_entered_in_valid_details_of_garden_name_city_country_size_and_description_of(String gardenName, String city, String country, String gardenSize, String description) {
        AddressDTO location = new AddressDTO(null, null, null, city, country, null, null);
        GardenDTO gardenDTO = new GardenDTO(gardenName, description, gardenSize, location);
        request.flashAttr("gardenDTO", gardenDTO);
    }

    @When("I click the Continue without description button")
    public void i_click_the_continue_without_description_button() throws Exception {
        result = mockMvc.perform(request);
    }

    @Then("the create form is resubmitted without the description")
    public void the_create_form_is_resubmitted_without_the_description() throws Exception {
        request = MockMvcRequestBuilders.post("/garden/add");
        AddressDTO location = new AddressDTO(null, null, null, "Wellington", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My API-Less Garden", null, "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
        result = mockMvc.perform(request);
    }

    @Then("the garden is created without a description")
    public void theGardenIsCreatedWithoutADescription() {
        Assertions.assertNull(gardenRepository.findById(13L).get().getDescription());
    }

    @When("I click the Continue without updating description button")
    public void i_click_the_continue_without_updating_description_button() throws Exception {
        result = mockMvc.perform(request);
    }

    @Then("the edit form is resubmitted without changing the description")
    public void theEditFormIsResubmittedWithoutChangingTheDescription() throws Exception {
        request = MockMvcRequestBuilders.put("/garden/add/14");
        // Set back to the original description of saved garden
        AddressDTO location = new AddressDTO(null, null, null, "Wellington", "New Zealand", null, null);
        GardenDTO gardenDTO = new GardenDTO("My API-Less Garden Edited", "Description", "2.0", location);
        request.flashAttr("gardenDTO", gardenDTO);
        result = mockMvc.perform(request);
    }

    @Then("the garden is updated except the description")
    @Transactional
    public void the_garden_is_updated_except_the_description() {
        Assertions.assertEquals("Description", gardenRepository.findById(14L).get().getDescription());
        Assertions.assertEquals("My API-Less Garden Edited", gardenRepository.findById(14L).get().getName());
    }

}
