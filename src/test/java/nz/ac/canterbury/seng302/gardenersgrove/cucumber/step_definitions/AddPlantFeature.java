package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AddPlantFeature {

    public static MockMvc MOCK_MVC;

    private GardenService gardenService;
    private static ProfanityFilterService mockProfanityFilterService;

    private PlantService plantService;

    private UserService userService;


    private FriendService friendService;

    @MockBean
    private GardenRepository mockGardenRepository;


    // Set test variables

    private User user;

    private Garden garden;

    private ResultActions result;

    private MockHttpServletRequestBuilder requestBuilder;
    private WeatherService mockWeatherService;

    @Before
    public void before_each() {

        // Create mock user
        this.user = new User();
        user.setId(1L);
        user.setEmail("valid@email.com");
        user.setPassword("Password1!");
        user.grantAuthority("ROLE_USER");

        // Create mock garden
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        this.garden = new Garden("Test Garden", validLocation, user);
        garden.setGardenId(1L);
        garden.setPlants(new ArrayList<>());

        // Mock services
        gardenService = Mockito.mock(GardenService.class);
        PlantRepository mockPlantRepository = Mockito.mock(PlantRepository.class);
        plantService = Mockito.spy(new PlantService(mockPlantRepository));
        userService = Mockito.mock(UserService.class);
        friendService = Mockito.mock(FriendService.class);
        mockWeatherService = Mockito.mock(WeatherService.class);

        // Set up mocks for user service
        Mockito.when(userService.getCurrentUser()).thenReturn(user);
        Mockito.when(gardenService.getGardenByID(garden.getGardenId())).thenReturn(Optional.ofNullable(garden));
        Mockito.when(gardenService.checkGardenOwnership(garden.getGardenId())).thenReturn(true);
        Mockito.when(gardenService.getGardensByUserId(user.getId())).thenReturn(new ArrayList<Garden>());
        Mockito.when(friendService.checkIfFriends(user.getId(), user.getId())).thenReturn(true);
        Mockito.doAnswer(i -> {
            Plant p = i.getArgument(0);
            garden.getPlants().add(p);
            return p;
        }).when(plantService).addPlant(Mockito.any());

        // Set up mocks
        GardenController gardenController = new GardenController(gardenService, userService, friendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));
        PlantController plantController = new PlantController(plantService, gardenService, userService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(gardenController, plantController).build();
    }

    @Given("I am on a garden details page for a garden I own")
    public void i_am_on_a_garden_details_page_for_a_garden_i_own() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders
                        .get("/garden/1"))
                .andExpectAll(status().isOk(),
                        view().name("gardenView"),
                        model().attribute("owner", true),
                        model().attribute("garden", garden)
                );
    }

    @When("I click add new plant button")
    public void i_click_add_new_plant_button() throws Exception {
        result = MOCK_MVC.perform(MockMvcRequestBuilders
                .get("/garden/" + garden.getGardenId() + "/plant"));
    }

    @Then("I see an add plant form")
    public void i_see_an_add_plant_form() throws Exception {
        result.andExpectAll(
                status().isOk(),
                view().name("addPlantForm"),
                model().attribute("plantName", ""),
                model().attribute("description", ""),
                model().attribute("datePlanted", ""),
                model().attribute("count", ""),
                model().attributeDoesNotExist("plantImage")
        );
    }

    @Given("I am on the add plant form")
    public void i_am_on_the_add_plant_form() throws Exception {
        requestBuilder = MockMvcRequestBuilders.multipart("/garden/" + garden.getGardenId() + "/plant");
    }

    @Given("I enter the name {string}, count {string}, description {string}, and planted-on date {string}")
    public void i_enter_the_name_count_description_and_planted_on_date(String name, String count, String description, String date) {
        description = description.equals("longDescription...") ? "a".repeat(513) : description;
        MockMultipartFile file = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[0]);
        requestBuilder.param("plantName", name)
                .param("description", description)
                .param("datePlanted", date)
                .param("count", count);
        ((MockMultipartHttpServletRequestBuilder) requestBuilder).file(file);
    }

    @When("I submit the add plant form")
    public void i_submit_the_add_plant_form() throws Exception {
        result = MOCK_MVC.perform(requestBuilder);
    }

    @Then("a new plant record is added to the garden,")
    public void a_new_plant_record_is_added_to_the_garden() {
        Assertions.assertEquals(1, garden.getPlants().size());
    }

    @Then("I am taken back to the garden details page.")
    public void i_am_taken_back_to_the_garden_details_page() throws Exception {
        result.andExpectAll(
                redirectedUrl("/garden/" + garden.getGardenId())
        );
    }

    @Then("I see an error: {string} for the field: {string}")
    public void i_see_an_error_for_the_field(String errorMessage, String errorField) throws Exception {
        result.andExpectAll(
                view().name("addPlantForm"),
                model().attribute(errorField, errorMessage)
        );
    }

    @When("I cancel the add plant form")
    public void i_cancel_the_add_plant_form() throws Exception {
        result = MOCK_MVC.perform(MockMvcRequestBuilders
                .get("/garden/" + garden.getGardenId()));
    }

    @Then("I go back to the garden details page.")
    public void i_go_back_to_the_garden_details_page() throws Exception {
        result.andExpectAll(
                view().name("gardenView"),
                model().attribute("garden", garden)
        );
    }

    @Then("no new plant record is added to the garden,")
    public void no_new_plant_record_is_added_to_the_garden() {
        Assertions.assertEquals(0, garden.getPlants().size());
    }
}
