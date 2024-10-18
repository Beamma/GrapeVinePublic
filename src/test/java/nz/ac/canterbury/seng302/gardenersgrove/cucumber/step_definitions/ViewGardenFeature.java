package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


public class ViewGardenFeature {

    private static GardenRepository mockGardenRepository;
    private static UserRepository mockUserRepository;
    @MockBean
    private static FriendService mockFriendService;
    @MockBean
    private static ProfanityFilterService mockProfanityFilterService;
    private static UserService mockUserService;

    private static MockMvc mockMvc;

    private static User user;

    private static User mockUser;

    private static Garden mockGarden;
    private static Garden garden;
    private static List<Garden> gardens;
    private static List<User> users;
    private ResultActions request;
    private MockHttpServletRequestBuilder requestBuilder;

    private AtomicReference<MvcResult> gardenResult = new AtomicReference<>();
    private static WeatherService mockWeatherService;
    private static TagRepository mockTagRepository;

    @BeforeAll
    public static void before_or_after_all() {

        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockUserRepository = Mockito.mock(UserRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        mockWeatherService = Mockito.mock(WeatherService.class);
        mockTagRepository = Mockito.mock(TagRepository.class);
        gardens = new ArrayList<>();
        users = new ArrayList<>();

        user = new User("my@email.com", "1999-12-31", "Gerd", "Müller", false, "Password1!", "Password1!");

        mockUser = Mockito.spy(user);

        Mockito.when(mockUser.getId()).thenReturn(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(mockUser);
        Mockito.when(mockUserService.validateUserId(Mockito.anyString())).thenReturn(true);
        Mockito.when(mockUserService.getById(String.valueOf(Mockito.any(Long.class)))).thenReturn(mockUser);
        Mockito.when(mockUserRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(mockUser));

        garden = new Garden("My Garden",
                new AddressDTO(null, null, null, "New York", "United States of America", null, null),
                1.0,
                mockUser);

        mockGarden = Mockito.spy(garden);
        mockGarden.setUser(mockUser);
        Mockito.when(mockGarden.getGardenId()).thenReturn(1L);
        Mockito.when(mockGardenRepository.save(Mockito.any(Garden.class))).thenReturn(mockGarden);
        gardens.add(mockGarden);
        Mockito.when(mockGardenRepository.findAll()).thenReturn(gardens);
        Mockito.when(mockGardenRepository.findById(Mockito.any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return gardens.stream().filter(garden -> garden.getGardenId().equals(id)).findFirst();
        });

        mockFriendService = Mockito.mock(FriendService.class);
        Mockito.when(mockFriendService.checkIfFriends(Mockito.any(Long.class), Mockito.any(Long.class))).thenReturn(true);
        Mockito.when(mockUserService.getById(Mockito.anyString())).thenReturn(mockUser);

        GardenService gardenService = new GardenService(mockGardenRepository, mockUserService, mockTagRepository);
        GardenController gardenController = new GardenController(gardenService,
                mockUserService,
                mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));
        mockMvc = MockMvcBuilders.standaloneSetup(gardenController)
                .build();


    }

    @Given("I am logged into Gardeners Grove")
    public void i_am_logged_into_gardeners_grove() {
        mockUser.grantAuthority("ROLE_USER");
        mockUser.setEnabled(true);
    }


    @Given("I have a garden called {string} at {string}, {string}, {string}")
    public void i_have_a_garden_called_at(String gardenName, String address, String city, String country) {
        AddressDTO gardenLocation = new AddressDTO(address, null, null, city, country, null, null);
        garden = new Garden(gardenName, gardenLocation, GardenUtils.parseSize("1"), mockUser);
    }


    @When("I click the Garden's button")
    public void i_click_the_button() throws Exception {
        String phrase = "<a th:each=\"garden : ${gardens}\" th:href=\"@{'/garden/' + ${garden.gardenId}}\" th:text=\"${#strings.abbreviate(garden.name, 20)}\">Garden</a>";
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/fragments/navbar.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);
    }

    @Then("I am redirected to the details page of garden {int}")
    public void i_am_redirected_to_details_page(int gardenId) throws Exception {
        Long myGardenId = mockGarden.getGardenId();
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/" + myGardenId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("gardenView"));
    }

    // AC2

    @When("I click the My Gardens button")
    public void i_click_the_my_gardens_button() throws Exception {
        String phrase = "<a th:href=\"@{'/garden/list/' + ${navBarUser.getId()}}\">Gardens</a>";
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/fragments/navbar.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);

    }
    @Then("I am redirected to the My Gardens page")
    public void i_am_redirected_to_the_my_gardens_page() throws Exception {
        Long userId = mockUser.getId();
        AtomicReference<MvcResult> gardenResult = new AtomicReference<>();
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/list/" + userId))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.view().name("gardenListPage")).andDo(gardenResult::set);
    }



    @When("I am on the garden details page of {string}")
    public void i_am_on_the_garden_details_page_of(String gardenName) throws Exception {
        Long gardenId = mockGarden.getGardenId();
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/" + gardenId))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.view().name("gardenView"));
    }

    @Then("I cannot edit any of the {string} details")
    public void i_cannot_edit_any_of_the_details(String gardenName) {
        //Will be tested manually, cannot be tested by acceptance tests
    }

    @Given("I am not logged in")
    public void i_am_not_logged_in() {
        mockUser.setEnabled(false);
        mockUser.grantAuthority(null);
    }

    @When("I try to access garden with id 4")
    public void i_try_to_access_garden_with_the_id() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/4"))
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andDo(gardenResult::set);
    }

    @When("I try to access garden with id 1")
    public void i_try_to_access_garden_with_id() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/1"))
                .andDo(gardenResult::set);
    }

    @Then("I am redirected to the login page")
    public void i_am_redirected_to_the_page() {
        //will be done with frontend testing, spring security prevents this anyway.
    }

    @Then("I am shown the error {string} in the {string} field")
    public void iAmShownTheErrorInTheField(String errorMessage, String error) {
        Assertions.assertEquals(errorMessage, gardenResult.get().getModelAndView().getModel().get(error));
    }



}