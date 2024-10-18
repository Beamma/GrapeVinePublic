package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.GardenValidator;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GardenCreationFeature {

    private static GardenRepository mockGardenRepository;
    @MockBean
    private static FriendService mockFriendService;
    @MockBean
    private static WeatherService mockWeatherService;
    @MockBean
    private static ProfanityFilterService mockProfanityFilterService;
    @MockBean
    private static TagRepository tagRepository;
    private static UserService mockUserService;

    private static MockMvc mockMvc;

    private static User user;
    private Garden addedGarden;
    private static List<Garden> gardens;
    private ResultActions request;
    private MockHttpServletRequestBuilder requestBuilder;


    @BeforeAll
    public static void setUp() throws JsonProcessingException {

        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        gardens = new ArrayList<>();
        user = new User("my@email.com", "1999-12-31", "Gerd", "Müller", false, "Password1!", "Password1!");
        user.setId(1L);

        Mockito.when(mockGardenRepository.save(Mockito.any(Garden.class))).thenAnswer(i -> {
            Garden addedGarden = i.getArgument(0);
            Garden spiedGarden = Mockito.spy(addedGarden);
            Mockito.when(spiedGarden.getGardenId()).thenReturn(1L);
            gardens.add(spiedGarden);
            return spiedGarden;
        });
        Mockito.when(mockGardenRepository.findAll()).thenReturn(gardens);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);

        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("shit") || ((String) i.getArgument(0)).contains("ass"));


        GardenService gardenService = new GardenService(mockGardenRepository, mockUserService, tagRepository);
        GardenController gardenController = new GardenController(gardenService,
                mockUserService,
                mockFriendService,
                mockWeatherService,
                mockProfanityFilterService,
                new GardenFilterService(mockGardenRepository));
        mockMvc = MockMvcBuilders.standaloneSetup(gardenController)
                .build();
    }

    @Given("I am logged in")
    public void i_am_logged_in() {
        user.grantAuthority("ROLE_USER");
        user.setEnabled(true);
        User mockUser = Mockito.spy(user);
        Mockito.when(mockUser.getId()).thenReturn(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(mockUser);
    }

    @When("I hit the Create new garden button")
    public void i_hit_the_create_new_garden_button() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/add"));
    }

    @Then("I see a form to create a new garden")
    public void i_see_a_form_to_create_a_new_garden() throws Exception {
        request.andExpect(status().isOk())
                .andExpect(view().name("addGardenForm"));
    }

    @Given("I am on the create garden form")
    public void i_am_on_the_create_garden_form() throws Exception {
        requestBuilder = MockMvcRequestBuilders.post("/garden/add");
    }

    @When("I enter inputs for name: {string} description {string} size: {string} city: {string} and country {string}")
    public void i_enter_valid_inputs_for_and(String gardenName, String description, String gardenSize, String city, String country) {
        AddressDTO location = new AddressDTO(null, null, null, city, country, -143.54, 35.356);
        if (!gardenSize.isBlank() && GardenValidator.isSizeValid(gardenSize)) {
            addedGarden = new Garden(gardenName, location, GardenUtils.parseSize(gardenSize), description, user);
        }

        GardenDTO gardenDTO = new GardenDTO(gardenName, description, gardenSize, location);

        requestBuilder.flashAttr("gardenDTO", gardenDTO);
    }

    @When("I submit the form")
    public void i_submit_the_form() throws Exception {
        request = mockMvc.perform(requestBuilder);
    }

    @Then("I am redirected to garden details with the new garden")
    public void i_am_redirected_to_garden_details_with_the_new_garden() throws Exception {
        request.andExpectAll(
                status().is(302),
                redirectedUrl("/garden/1")
        );
    }

    @Then("I am shown an error in the {string} field saying {string}")
    public void i_am_shown_an_error_in_the_field_saying_or(String errorField, String errorMessage) throws Exception {
        MvcResult result = request
                .andExpectAll(
                        model().attributeHasFieldErrorCode("gardenDTO", errorField, "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT
        boolean containsExpectedErrorMessage = bindingResult.getFieldErrors(errorField)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));

        Assertions.assertTrue(containsExpectedErrorMessage);
    }




}
