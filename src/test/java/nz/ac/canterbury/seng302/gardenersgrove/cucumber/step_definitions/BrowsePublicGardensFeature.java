package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BrowsePublicGardensFeature {

    public static final int PAGE_SIZE = 9;

    private static MockMvc mockMvc;
    private static GardenService GardenService;
    private static GardenRepository mockGardenRepository;
    private static ProfanityFilterService mockProfanityFilterService;
    private static GardenController gardenController;
    private static FriendService mockfriendService;
    private static UserService mockUserService;
    private Garden publicGarden;
    private AddressDTO validLocation;
    private User user;
    private ResultActions request;
    private String publicSearch;
    private int currentPage;
    Page<Garden> publicGardenPage;
    List<Garden> publicGardenList;
    private static WeatherService mockWeatherService;
    private static TagRepository mockTagRepository;

    @Before
    public void before_or_after_all() {
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        mockfriendService = Mockito.mock(FriendService.class);
        mockTagRepository = Mockito.mock(TagRepository.class);
        GardenService = new GardenService(mockGardenRepository, mockUserService, mockTagRepository);
        GardenService spiedGardenService = Mockito.spy(GardenService);
        mockWeatherService = Mockito.mock(WeatherService.class);
        gardenController = new GardenController(spiedGardenService, mockUserService, mockfriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));

        mockMvc = MockMvcBuilders.standaloneSetup(gardenController).build();
        Mockito.when(mockfriendService.checkIfFriends(Mockito.any(Long.class), Mockito.any(Long.class))).thenReturn(false);

        user = new User();
        user.setId(1L);

        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);

        User owner = new User();
        owner.setId(2L);

        publicSearch = "search";
        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        publicGarden = new Garden("Public Garden", validLocation, owner);
        Plant plant = new Plant(publicGarden, "Plant", 1, "Description", null, null);
        publicGarden.setPlants(new ArrayList<>(List.of(plant)));

        Mockito.when(spiedGardenService.getGardenByID(1L)).thenReturn(java.util.Optional.of(publicGarden));

        publicGardenList = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Garden garden = new Garden("Public Garden " + i, validLocation, owner);
            garden.setPublicGarden(true);
            publicGardenList.add(garden);
        }

        // This is to fake the pagination results of the garden repository
        for (int i = 0; i < publicGardenList.size(); i += PAGE_SIZE) {
            PageRequest pageRequest = PageRequest.of(i/9, PAGE_SIZE);
            List<Garden> sublist = publicGardenList.subList(i, Math.min(i + PAGE_SIZE, publicGardenList.size()));
            Page<Garden> page = new PageImpl<>(sublist, pageRequest, publicGardenList.size());
            Mockito.when(mockGardenRepository.findAllPublicGardensByNamePageable(publicSearch, PageRequest.of(i/9, PAGE_SIZE))).thenReturn(page);
        }

        Mockito.when(mockGardenRepository.findByIsPublicGarden(true)).thenReturn(publicGardenList);
        Mockito.when(mockGardenRepository.findAllGardensByName(publicSearch, "1")).thenReturn(publicGardenList);

    }

    @Given("a garden has been marked as public")
    public void a_garden_has_been_marked_as_public() {
        publicGarden.setPublicGarden(true);
    }
    @When("I view the garden")
    public void i_view_the_garden() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/" + 1L));
    }
    @Then("I can see the details of the plants in the garden")
    public void i_can_see_the_details_of_the_plants_in_the_garden() {
        Garden garden = (Garden) request.andReturn().getModelAndView().getModel().get("garden");
        Assertions.assertEquals(1, garden.getPlants().size());
    }

    @Given("I am anywhere on the system")
    public void i_am_anywhere_on_the_system() {
        // This AC is covered by manual testing and is not applicable to cucumber testing
    }

    @When("I click on the browse gardens link")
    public void i_click_on_the_browse_gardens_link() {
        // This AC is covered by manual testing and is not applicable to cucumber testing
    }
    @Then("I am taken to the browse gardens page")
    public void i_am_taken_to_the_browse_gardens_page() {
        // This AC is covered by manual testing and is not applicable to cucumber testing
    }

    @Given("I fill the search field with {string}")
    public void i_fill_the_search_field_with(String search) {
        publicSearch = search;
    }

    @And("Gardens exist with the word {string} in the title")
    public void gardens_exist_with_the_word_in_the_title(String search) {
        publicGardenList = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            Garden garden = new Garden(search, validLocation, user);
            garden.setPublicGarden(true);
            publicGardenList.add(garden);
        }
        publicGardenPage = new PageImpl<>(publicGardenList);
        Mockito.when(mockGardenRepository.findAllPublicGardensByNamePageable(search, PageRequest.of(0, PAGE_SIZE))).thenReturn(publicGardenPage);
    }

    @When("I click on the search button")
    public void i_click_on_the_search_button() throws Exception {
        // Clicking the search button is not applicable to cucumber testing
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch));
    }

    @Then("I see a list of gardens with the word {string} in the title")
    public void i_see_a_list_of_gardens_with_the_word_in_the_title(String search) {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        List<Garden> gardens = browseGardenDTO.getGardens();
        Assertions.assertEquals(PAGE_SIZE, gardens.size());
        for (Garden garden : gardens) {
            Assertions.assertTrue(garden.getName().contains(search));
        }
    }

    @When("I press the enter key")
    public void i_press_the_enter_key() throws Exception {
        // Pressing the enter key is not applicable to cucumber testing
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch));
    }

    @And("No gardens exist with that search term")
    public void no_gardens_exist_with_that_search_term() {
        Mockito.when(mockGardenRepository.findAllPublicGardensByNamePageable(publicSearch, PageRequest.of(0, PAGE_SIZE))).thenReturn(new PageImpl<>(new ArrayList<>()));
    }

    @Then("I see a message saying {string}")
    public void i_see_a_message_saying(String error) {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        Assertions.assertEquals(error, browseGardenDTO.getSearchError());
    }

    @Given("I search for a term that returns more than {int} results")
    public void i_search_for_a_term_that_returns_more_than_results(Integer numResults) {
        publicGardenList = new ArrayList<>();
        for (int i = 0; i < numResults + 1; i++) {
            Garden garden = new Garden(publicSearch, validLocation, user);
            garden.setPublicGarden(true);
            publicGardenList.add(garden);
        }

        List<Garden> limitedGardens = publicGardenList.stream().limit(numResults).collect(Collectors.toList());
        publicGardenPage = new PageImpl<>(limitedGardens);
        Mockito.when(mockGardenRepository.findAllPublicGardensByNamePageable(publicSearch, PageRequest.of(0, numResults)))
                .thenReturn(publicGardenPage);
    }

    @Then("I see the first {int} results")
    public void i_see_the_first_results(Integer numResults) {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        List<Garden> gardens = browseGardenDTO.getGardens();
        Assertions.assertEquals(numResults, gardens.size());
    }

    @Given("I am on any page of results")
    public void i_am_on_any_page_of_results() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", "2"));
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int page = browseGardenDTO.getParsedPage();
        Assertions.assertEquals(2, page);
    }

    @When("I click on the first page button")
    public void i_click_on_the_first_page_button() throws Exception {
        // Clicking the first page button is not applicable to cucumber testing
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", "1"));
    }
    @Then("I am taken to the first page of results")
    public void i_am_taken_to_the_first_page_of_results() {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        List<Garden> gardens = browseGardenDTO.getGardens();
        Assertions.assertEquals(PAGE_SIZE, gardens.size());
        int page = browseGardenDTO.getParsedPage();
        Assertions.assertEquals(1, page);
    }

    @When("I click on the last page button")
    public void i_click_on_the_last_page_button() throws Exception {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int lastPage = browseGardenDTO.getTotalPages();
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", String.valueOf(lastPage)));
    }

    @Then("I am taken to the last page of results")
    public void i_am_taken_to_the_last_page_of_results() {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int currentPage = browseGardenDTO.getParsedPage();
        int totalPages = browseGardenDTO.getTotalPages();
        Assertions.assertEquals(totalPages, currentPage);
    }

    @Given("I am on the last page of results")
    public void i_am_on_the_last_page_of_results() throws Exception {
        int lastPage = (publicGardenList.size() / PAGE_SIZE);
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", String.valueOf(lastPage)));
    }
    @When("I click on the next page button")
    public void i_click_on_the_next_page_button() throws Exception {
        // Clicking the next page button is disabled when on the last page but this is out of scope for cucumber testing.
        // Can be done manually.
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int currentPage = browseGardenDTO.getParsedPage();
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", String.valueOf(currentPage + 1)));
    }

    @Then("I see two page numbers in both directions")
    public void i_see_two_page_numbers_in_both_directions() {
        // This AC is covered by manual testing and is out of scope for cucumber testing as it is a UI feature
    }

    @Given("I am on the first page of results")
    public void i_am_on_the_first_page_of_results() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", "1"));
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int page = browseGardenDTO.getParsedPage();
        Assertions.assertEquals(1, page);
    }
    @When("I click on the {int} page button")
    public void i_click_on_the_page_button(int page) throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/browse").param("search", publicSearch).param("page", String.valueOf(page)));
        currentPage = page;
    }
    @Then("I am taken to that page of results")
    public void i_am_taken_to_that_page_of_results() {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int page = browseGardenDTO.getParsedPage();
        Assertions.assertEquals(currentPage, page);
    }

    @Then("I see the number of results")
    public void i_see_the_number_of_results() {
        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) request.andReturn().getModelAndView().getModel().get("browseGardenDTO");
        int searchSize = browseGardenDTO.getSearchSize();
        Assertions.assertEquals(publicGardenList.size(), searchSize);
    }

}
