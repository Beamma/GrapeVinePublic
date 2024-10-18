package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PlantEditFeature {
    private static MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;
    private static UserService mockUserService;
    private static GardenService gardenService;
    private static PlantRepository mockPlantRepository;
    private static ProfanityFilterService mockProfanityFilterService;
    private ResultActions request;
    private static Garden mockGarden1;
    private static Plant mockPlant1;
    private static Plant mockPlant2;

    private static GardenRepository mockGardenRepository;
    private static WeatherService mockWeatherService;

    private static TagRepository tagRepository;

    @BeforeAll
    public static void setUp() throws ParseException {
        // Set up mock repositories and services.
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockPlantRepository = Mockito.mock(PlantRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        tagRepository = Mockito.mock(TagRepository.class);
        gardenService = new GardenService(mockGardenRepository, mockUserService, tagRepository);
        PlantService plantService = new PlantService(mockPlantRepository);
        PlantController plantController = new PlantController(plantService, gardenService, mockUserService);
        mockWeatherService = Mockito.mock(WeatherService.class);
        // Setup mock user
        User jane = new User();
        jane.setEmail("jane@email.com");
        jane.setPassword("Password1!");
        jane.setEnabled(true);
        jane.grantAuthority("ROLE_USER");
        jane.setId(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(jane);

        // Create a date that is in a valid form for the Plant entity
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Create some mock plants
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        mockGarden1 = new Garden("Garden 1", validLocation, jane);
        mockPlant1 = new Plant(mockGarden1, "Plant1", 1, "Desc1", date, null);
        mockPlant2 = new Plant(mockGarden1, "Plant2", 2, "Desc2", date, null);
        mockGarden1.setPlants(List.of(mockPlant1));
        Mockito.when(mockGardenRepository.findById(1L)).thenReturn(Optional.of(mockGarden1));
        Mockito.when(mockPlantRepository.findByIdAndGardenId(1L, 1L)).thenReturn(Optional.ofNullable(mockPlant1));
        Mockito.when(mockPlantRepository.findById(2L)).thenReturn(Optional.ofNullable(mockPlant2));
        Mockito.when(mockPlantRepository.findByIdAndGardenId(2L, 1L)).thenReturn(Optional.ofNullable(mockPlant2));

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(plantController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Given("I am on the garden details page for a garden I own")
    public void i_am_on_the_garden_details_page_for_a_garden_i_own() throws Exception {
        // Setup mocks and controller for the garden view page
        FriendService mockFriendService = Mockito.mock(FriendService.class);
        Mockito.when(mockFriendService.checkIfFriends(1l, 1l)).thenReturn(true);
        GardenController gardenController = new GardenController(gardenService, mockUserService, mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));

        // Send request
        MockMvc mockMvc2 = MockMvcBuilders.standaloneSetup(gardenController)
                .build();
        request = mockMvc2.perform(MockMvcRequestBuilders.get("/garden/1"));
        request.andExpect(status().isOk()).andExpect(view().name("gardenView"));
    }
    @Then("There is a list of all plants I have recorded in the garden with their {string}, a {string}, and {int} and {string}")
    public void there_is_a_list_of_all_plants_i_have_recorded_in_the_garden_with_their_a_and_and_if_provided(String string, String string2, Integer int1, String string3) throws Exception {
        ResultActions result = request.andExpect(status().isOk())
                .andExpect(view().name("gardenView"))
                .andExpect(model().attribute("garden", mockGarden1));


        // Check that a list containing only mockPlant1 is being returned
        Garden resultGarden = (Garden) Objects.requireNonNull(result.andReturn().getModelAndView()).getModel().get("garden");
        List<Plant> resultPlants = List.of(resultGarden.getPlants().toArray(new Plant[0]));

        Assertions.assertEquals(resultPlants, List.of(mockPlant1));
    }
    @When("I click on a Edit button next to each plant")
    public void i_click_on_a_button_next_to_each_plant() throws IOException {
        String phrase = "<span>Edit Plant </span>";
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/gardenView.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);
    }
    @Then("I see the edit plant form with all details of the plant pre-populated")
    public void i_see_the_edit_plant_form_with_all_details_of_the_plant_pre_populated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/garden/1/plant/1"))
                .andExpectAll(status().isOk(),
                        view().name("addPlantForm"),
                        model().attribute("editPlant", "true"),
                        model().attribute("plantName", "Plant1"),
                        model().attribute("count", "1"),
                        model().attribute("description", "Desc1"),
                        model().attribute("datePlanted", "2024-01-01"),
                        model().attributeDoesNotExist("existingPlantImage")
                        );
    }

    @Given("I am on the edit plant form")
    public void i_am_on_the_edit_plant_form() {
        requestBuilder = MockMvcRequestBuilders.put("/garden/1/plant/2").with(csrf());
    }

    @And("I enter valid values for the {string} and optionally a {string}, {string}, and a planted-on {string}")
    public void i_enter_valid_values_for_the_name_and_optionally_a_count_description_and_a_planted_on_date(String name, String count, String desc, String date) {
        requestBuilder
            .param("plantName", name)
            .param("count", count)
            .param("description", desc)
            .param("datePlanted", date);



    }

    @When("I click submit")
    public void i_click_submit() throws Exception {
        request = mockMvc.perform(requestBuilder);
    }

    @Then("the plant record is updated, and I am taken back to the garden details page {string} {string} {string} {string}")
    public void the_plant_record_is_updated_and_i_am_taken_back_to_the_garden_details_page(String name, String count, String desc, String date) throws Exception {
        request.andExpectAll(
                status().is3xxRedirection()
        );
        mockPlant2.setName(name);
        mockPlant2.setCount(Integer.parseInt(count));
        mockPlant2.setDescription(desc);

        // Create a date that is in a valid form for the Plant entity
        Date date2 = null;
        try {
            date2 = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mockPlant2.setDate(date2);
        Mockito.verify(mockPlantRepository).save(mockPlant2);
    }

    @And("I enter an empty or invalid {string}")
    public void i_enter_an_empty_or_invalid(String string) {
        requestBuilder
                .param("plantName", string)
                .param("count", "1")
                .param("description", "test")
                .param("datePlanted", "2024-01-01");
    }

    @Then("An {string} message tells me {string}")
    public void an_error_message_tells_me(String error, String string) throws Exception {
        request.andExpectAll(
                status().isBadRequest(),
                view().name("addPlantForm"),
                model().attribute(error, string),
                model().attributeExists("plantName"),
                model().attributeExists("count"),
                model().attributeExists("description")
        );
    }

    @And("I enter a description that is longer than {int} characters")
    public void i_enter_a_description_that_is_longer_than_characters(Integer int1) {
        String desc = "a".repeat(int1 + 1); // Create a description longer than the max amount of chars.

        requestBuilder
                .param("plantName", "name")
                .param("count", "1")
                .param("description", desc)
                .param("datePlanted", "2024-01-01");

    }

    @And("I enter an invalid {string}")
    public void i_enter_an_invalid(String count) {
        requestBuilder
                .param("plantName", "name")
                .param("count", count)
                .param("description", "desc")
                .param("datePlanted", "2024-01-01");
    }

    @And("I enter a {string} that is not in the Aotearoa NZ format")
    public void i_enter_a_that_is_not_in_the_aotearoa_nz_format(String date) {
        requestBuilder
                .param("plantName", "name")
                .param("count", "1")
                .param("description", "desc")
                .param("datePlanted", date);
    }

    @Given("I am on the edit plant page")
    public void i_am_on_the_edit_plant_page() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/garden/1/plant/2").with(csrf());
        request = mockMvc.perform(requestBuilder);
        request.andExpect(view().name("addPlantForm"));
    }

    @When("I click the {string} button")
    public void i_click_the_button(String string) throws IOException {
        String phrase = string + "</span>";
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/addPlantForm.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);
    }
    @Then("I am taken back to the garden details page")
    public void i_am_taken_back_to_the_garden_details_page() throws Exception {
        // Setup mocks and controller for the garden view page
        FriendService mockFriendService = Mockito.mock(FriendService.class);
        Mockito.when(mockFriendService.checkIfFriends(1l, 1l)).thenReturn(true);
        GardenController gardenController = new GardenController(gardenService, mockUserService, mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));
        // Send request
        MockMvc mockMvc2 = MockMvcBuilders.standaloneSetup(gardenController)
                .build();
        request = mockMvc2.perform(MockMvcRequestBuilders.get("/garden/1"));
        request.andExpect(status().isOk()).andExpect(view().name("gardenView"));
    }
}
