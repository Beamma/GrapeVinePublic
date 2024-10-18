package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseFeedDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class LinkGardenToPostFeature {

    @Autowired
    UserRepository userRepository;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    GardenController gardenController;
    @Autowired
    PostController postController;

    ResultActions result;
    MockHttpServletRequestBuilder requestBuilder;
    MockMvc mockMvc;

    private User user;
    private Garden g1;
    private Garden g2;
    private Garden g3;

    @Before("@U9009")
    public void setup() {
        user = userRepository.save(new User("test@email.com", "2002-12-12", "First", "", true, "password", "password"));
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc = MockMvcBuilders.standaloneSetup(gardenController, postController).build();
    }

    @After("@U9009")
    @Transactional
    public void tearDown() {
        gardenRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Given("a user has has public gardens named {string}, {string} {string}")
    public void a_user_has_has_public_gardens_named(String name1, String name2, String name3) {
        Garden garden1 = new Garden(name1, new AddressDTO("", "", "", "City", "Country", null, null), user);
        Garden garden2 = new Garden(name2, new AddressDTO("", "", "", "City", "Country", null, null), user);
        Garden garden3 = new Garden(name3, new AddressDTO("", "", "", "City", "Country", null, null), user);
        garden1.setPublicGarden(true);
        garden2.setPublicGarden(true);
        garden3.setPublicGarden(true);
        g1 = gardenRepository.save(garden1);
        g2 = gardenRepository.save(garden2);
        g3 = gardenRepository.save(garden3);
    }

    @Given("the user has entered a search string of {string}")
    public void the_user_has_entered_a_search_string_of(String searchQuery) {
        requestBuilder = MockMvcRequestBuilders.get("/garden/public/search")
                .param("name", searchQuery);
    }

    @When("the user submits the search")
    public void the_user_submits_the_search() throws Exception {
        result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Then("I see all matching gardens shown")
    public void i_see_all_matching_gardens_shown_ordered_by_recency() throws Exception {
        result.andExpectAll(
                MockMvcResultMatchers.jsonPath("$").isArray(),
                MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3))
        );
    }

    @Then("the gardens all have their name and city included and are ordered by recency")
    public void the_gardens_all_have_their_name_and_city_included() throws Exception {
        result.andExpectAll(
                MockMvcResultMatchers.jsonPath("$").isArray(),
                MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3)),
                MockMvcResultMatchers.jsonPath("$[0].name").value(g3.getName()),
                MockMvcResultMatchers.jsonPath("$[0].location.city").value(g3.getLocation().getCity()),
                MockMvcResultMatchers.jsonPath("$[1].name").value(g2.getName()),
                MockMvcResultMatchers.jsonPath("$[1].location.city").value(g2.getLocation().getCity()),
                MockMvcResultMatchers.jsonPath("$[2].name").value(g1.getName()),
                MockMvcResultMatchers.jsonPath("$[2].location.city").value(g1.getLocation().getCity())
        );
    }

    @Then("I see no gardens")
    public void i_see_no_gardens() throws Exception {
        result.andExpectAll(
                status().isOk(),
                MockMvcResultMatchers.jsonPath("$").isArray(),
                MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    }

    @Then("I am shown an error message saying {string}")
    public void an_error_message_saying(String message) throws Exception {
        result.andExpectAll(
                status().isOk(),
                MockMvcResultMatchers.jsonPath("$").isMap(),
                MockMvcResultMatchers.jsonPath("$.errorMessage").value(message)
        );
    }

    @Then("I am told: None of your public gardens match your search")
    public void i_am_told_none_of_your_public_gardens_match_your_search() throws Exception {
        // Error is shown on frontend, if the returned list of gardens is empty.
        result.andExpectAll(
                status().isOk(),
                MockMvcResultMatchers.jsonPath("$").isArray(),
                MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    }

    @Given("a user has selected a garden to link")
    public void a_user_has_selected_a_garden_to_link() {
        PostDTO postDTO = new PostDTO("Title", "Content");
        postDTO.setGardenId(g1.getGardenId());
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postDTO);
    }

    @Then("I am redirected to the feed page")
    public void i_am_redirected_to_the_feed_page() throws Exception {
        result.andExpect(status().is3xxRedirection());
    }

    @When("I submit the add post form")
    public void i_submit_the_add_post_form() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("I see the garden linked to the most recent post")
    public void i_see_the_garden_linked_to_the_most_recent_post() throws Exception {

        result = this.mockMvc.perform((get("/feed/browse")
                ))
                .andExpect(view().name("posts/feedPage"));

        BrowseFeedDTO browseFeedDTO = (BrowseFeedDTO) result.andReturn().getModelAndView().getModel().get("browseFeedDTO");
        Assertions.assertEquals(g1.getGardenId(), browseFeedDTO.getPosts().get(0).getLinkedGarden().getGardenId());
        Assertions.assertEquals("Garden 1", browseFeedDTO.getPosts().get(0).getLinkedGarden().getName());


    }

    @Given("I am on the post feed page")
    public void i_am_on_the_post_feed_page() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/feed/browse");
        result = mockMvc.perform(requestBuilder);
    }

    @Given("I have created a post with a linked garden")
    public void i_have_created_a_post_with_a_linked_garden() {
        AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);
        Garden garden = new Garden("My garden", addressDTO, user);
        garden = gardenRepository.save(garden);

        PostDTO postDTO = new PostDTO("", "test content");
        postDTO.setGarden(garden);

        Post post = new Post(postDTO, null, user);
        post = postRepository.save(post);
    }

    @When("I click the linked garden element")
    public void i_click_the_linked_garden_element() throws Exception {
        BrowseFeedDTO browseFeedDTO = (BrowseFeedDTO) result.andReturn().getModelAndView().getModel().get("browseFeedDTO");

        requestBuilder = MockMvcRequestBuilders.get("/garden/"+ browseFeedDTO.getPosts().get(0).getLinkedGarden().getGardenId());
        result = mockMvc.perform(requestBuilder);
        result.andExpect(status().isOk());

    }

    @Then("I am taken to the garden view page for that garden")
    public void i_am_taken_to_the_garden_view_page_for_that_garden() {
        Garden garden = (Garden) result.andReturn().getModelAndView().getModel().get("garden");
        Assertions.assertEquals("My garden", garden.getName());
    }

}
