package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AddTagsToGardensFeature {
    private static MockMvc mockMvc;
    private static GardenRepository mockGardenRepository;
    private static ProfanityFilterService mockProfanityFilterService;
    private  static TagRepository mockTagRepository;
    private static GardenService gardenService;
    private static User mockUser1;
    private static Garden mockGarden1;
    private static ResultActions request;
    private static Tag tag1;
    private MockHttpServletRequestBuilder requestBuilder;
    private static WeatherService mockWeatherService;
    @BeforeAll
    public static void setUp() throws JsonProcessingException {
        // Setup Mocks
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        UserService mockUserService = Mockito.mock(UserService.class);
        mockTagRepository = Mockito.mock(TagRepository.class);
        FriendService mockFriendService = Mockito.mock(FriendService.class);
        gardenService = new GardenService(mockGardenRepository, mockUserService, mockTagRepository);
        mockWeatherService = Mockito.mock(WeatherService.class);

        // Setup current user
        mockUser1 = Mockito.mock(User.class);
        Mockito.when(mockUser1.getId()).thenReturn(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(mockUser1);
        Mockito.when(mockFriendService.checkIfFriends(1L, 1L)).thenReturn(true);

        // Setup mock garden with tags
        tag1 = new Tag("First Tag");
        mockGarden1 = Mockito.mock(Garden.class);
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        Mockito.when(mockGarden1.getTags()).thenReturn(tags);
        Mockito.when(mockGarden1.getUser()).thenReturn(mockUser1);
        Mockito.when(mockGarden1.getLocation()).thenReturn(validLocation);
        Mockito.when(mockGarden1.isPublicGarden()).thenReturn(true);
        Mockito.when(mockGardenRepository.findById(1L)).thenReturn(Optional.ofNullable(mockGarden1));
        Mockito.when(mockTagRepository.findByName("First Tag")).thenReturn(Optional.ofNullable(tag1));
        Mockito.when(mockTagRepository.findByName("New Tag 1")).thenReturn(Optional.empty());

        List<Tag> tagList = new ArrayList<>();
        tagList.add(tag1);
        Mockito.when(mockTagRepository.findAll()).thenReturn(tagList);
        Mockito.when(mockGarden1.getTagsOrdered()).thenReturn(tagList);

        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("shit"));

        GardenController gardenController = new GardenController(gardenService, mockUserService, mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(gardenController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Given("I am on the garden details page for a public garden")
    public void i_am_on_the_garden_details_page_for_a_public_garden () throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/1"));
    }

    @Then("I can see a list of tags that the garden has been marked with by its owner")
    public void i_can_see_a_list_of_tags_that_the_garden_has_been_marked_with_by_its_owner() throws Exception {
        request.andExpectAll(
                view().name("gardenView"),
                status().isOk()
        );

        Garden returnedGarden = (Garden) request.andReturn().getModelAndView().getModel().get("garden");
        Tag tag = returnedGarden.getTagsOrdered().get(0);
        Assertions.assertEquals(tag.getName(), "First Tag");

    }

    @Given("I am typing a tag")
    public void i_am_typing_a_tag() throws Exception {
        // Cannot replicate this, so instead send the request.
        request = mockMvc.perform(MockMvcRequestBuilders.get("/garden/1"));
    }

    @Then("I should see autocomplete options for tags that already exist in the system")
    public void i_should_see_autocomplete_options_for_tags_that_already_exist_in_the_system() throws Exception {
        request.andExpectAll(
                view().name("gardenView"),
                status().isOk()
        );

        List<Tag> tags = (List<Tag>) request.andReturn().getModelAndView().getModel().get("tags");
        Assertions.assertEquals(tags.get(0).getName(), "First Tag");
    }

    @Given("I have entered valid text for a {string} that does not exist")
    public void i_have_entered_valid_text_for_a_tag_that_does_not_exist(String tag) {
        Mockito.clearInvocations(mockGarden1);
        Mockito.clearInvocations(mockGardenRepository);
        Mockito.clearInvocations(mockTagRepository);
        requestBuilder = MockMvcRequestBuilders.put("/garden/1/tag").with(csrf()).param("tag", tag);
    }
    @When("I click the “+” button or press enter")
    public void i_click_the_button_or_press_enter() throws Exception {
        request = mockMvc.perform(requestBuilder);
    }
    @Then("the tag is added to my garden")
    public void the_tag_is_added_to_my_garden() {
        Mockito.verify(mockGarden1, Mockito.times(1)).addTag(Mockito.any());
        Mockito.verify(mockGardenRepository, Mockito.times(1)).save(mockGarden1);
    }
    @Then("the textbox is cleared")
    public void the_textbox_is_cleared() throws Exception {
        request.andExpectAll(status().is(302));
    }
    @Then("the tag becomes a new user-defined tag on the system showing up in future auto-complete suggestions")
    public void the_tag_becomes_a_new_user_defined_tag_on_the_system_showing_up_in_future_auto_complete_suggestions() {
        Mockito.verify(mockTagRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Given("I have entered invalid text {string}")
    public void i_have_entered_invalid_text(String tag) {
        Mockito.clearInvocations(mockGarden1);
        Mockito.clearInvocations(mockGardenRepository);
        Mockito.clearInvocations(mockTagRepository);
        requestBuilder = MockMvcRequestBuilders.put("/garden/1/tag").with(csrf()).param("tag", tag);
    }
    @Then("an tagError message tells me {string}")
    public void an_error_message_tells_me_the_tag_name_must_only_contain_alphanumeric_characters_spaces_or(String error) throws Exception {
        request.andExpectAll(flash().attribute("tagError", error));
    }
    @Then("no tag is added to my garden and no tag is added to the user defined tags the system knows")
    public void no_tag_is_added_to_my_garden_and_no_tag_is_added_to_the_user_defined_tags_the_system_knows() {
        Mockito.verify(mockTagRepository, Mockito.times(0)).save(Mockito.any());
        Mockito.verify(mockGarden1, Mockito.times(0)).addTag(Mockito.any());
        Mockito.verify(mockGardenRepository, Mockito.times(0)).save(mockGarden1);
    }

    @Given("I have entered a tag that is more than {int} characters")
    public void i_have_entered_a_tag_that_is_more_than_characters(Integer int1) throws Exception {
        Mockito.clearInvocations(mockGarden1);
        Mockito.clearInvocations(mockGardenRepository);
        Mockito.clearInvocations(mockTagRepository);
        requestBuilder = MockMvcRequestBuilders.put("/garden/1/tag").with(csrf()).param("tag", "a".repeat(int1 + 1));
    }
}
