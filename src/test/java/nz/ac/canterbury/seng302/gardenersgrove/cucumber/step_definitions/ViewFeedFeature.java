package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;


import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseFeedDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for view feed feature.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class ViewFeedFeature {

    // Users
    User user1;
    User user2;

    // Posts
    Post post1;
    Post post2;
    Post post3;
    Post post4;

    // Autowire used classes
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostController postController;

    MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;
    ResultActions result;
    BrowseFeedDTO browseFeedDTO;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Sets up the database for U9002 feature testing.
     */
    @Before("@U9002")
    public void setUp() {

        // Setup users
        User tempUser = new User("user1@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1 = userRepository.save(tempUser);

        tempUser = new User("user2@gmail.com", "2001-01-01", "Jane", "Down", false, "Password1!", "Password1!");
        user2 = userRepository.save(tempUser);


        // Setup posts
        LocalDateTime now = LocalDateTime.now();
        Post tempPost = new Post("Post1", "I am user 1", now, user1);
        post1 = postRepository.save(tempPost);

        tempPost = new Post("Post2", "I am user 1", now.minusMinutes(10), user1);
        post2 = postRepository.save(tempPost);

        tempPost = new Post("Post1", "I am user 2", now.minusHours(1), user2);
        post3 = postRepository.save(tempPost);

        tempPost = new Post("Post2", "I am user 2", now.minusDays(1), user2);
        post4 = postRepository.save(tempPost);


        // Mock MVC
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .build();


        // Log in
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user1.getEmail(), null, Collections.emptyList()));
    }

    /**
     * Adds posts to the database.
     *
     * @param amount The amount of posts to add.
     */
    private void addPosts(int amount) {
        // Posts for pagination
        for (int i = 0; i < amount; i++) {
            Post tempPost = new Post("Paginated post " + i, "I love gardens", user1);
            postRepository.save(tempPost);
        }
    }

    /**
     * Get page number given conditions.
     *
     * @param input The test input.
     */
    private String getPage(String input) {
        return switch (input) {
            case "first" -> "1";
            case "last" -> String.valueOf(browseFeedDTO.getTotalPages());
            default -> "1";
        };
    }

    /**
     * Clears out the database after use.
     */
    @After("@U9002")
    @Transactional
    public void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Given("I am logged in to the system")
    public void i_am_logged_in_to_the_system() {
        requestBuilder = MockMvcRequestBuilders
                .get("/feed/browse");
    }

    @When("I hit the view feed button")
    public void i_hit_the_view_feed_button() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("I am taken to the post feed page")
    public void i_am_taken_to_the_post_feed_page() throws Exception {
        result.andExpectAll(
                status().is(200),
                view().name("posts/feedPage"));
    }

    @Given("I am viewing the Feed on page {string}")
    public void i_am_on_the_view_feed_page(String page) throws Exception {
        requestBuilder = MockMvcRequestBuilders
                .get("/feed/browse")
                .param("page", page);
    }

    @When("the page loads")
    public void the_page_loads() throws Exception {
        result = mockMvc.perform(requestBuilder);
        browseFeedDTO = (BrowseFeedDTO) result.andReturn().getModelAndView().getModel().get("browseFeedDTO");
    }

    @Then("I can see information about posts")
    public void i_can_see_information_about_posts() throws Exception {
        // Check request
        MvcResult response = result.andExpectAll(
                status().is(200),
                view().name("posts/feedPage")
        ).andReturn();

        // Check posts content (owner name, owner image, title, content, time)
        Assertions.assertEquals(post1.getTitle(), browseFeedDTO.getPosts().get(0).getTitle());
        Assertions.assertEquals(post1.getContent(), browseFeedDTO.getPosts().get(0).getContent());
        Assertions.assertEquals(post1.getTimeSincePosted(), browseFeedDTO.getPosts().get(0).getTimeSincePosted());
    }

    @Then("posts are displayed newest to oldest")
    public void posts_are_displayed_newest_to_oldest() throws Exception {
        // Check request
        MvcResult response = result.andExpectAll(
                status().is(200),
                view().name("posts/feedPage")
        ).andReturn();

        // Check post order (newest to oldest)
        Assertions.assertEquals(post1.getTitle(), browseFeedDTO.getPosts().get(0).getTitle());
        Assertions.assertEquals(post1.getContent(), browseFeedDTO.getPosts().get(0).getContent());

        Assertions.assertEquals(post2.getTitle(), browseFeedDTO.getPosts().get(1).getTitle());
        Assertions.assertEquals(post2.getContent(), browseFeedDTO.getPosts().get(1).getContent());

        Assertions.assertEquals(post3.getTitle(), browseFeedDTO.getPosts().get(2).getTitle());
        Assertions.assertEquals(post3.getContent(), browseFeedDTO.getPosts().get(2).getContent());

        Assertions.assertEquals(post4.getTitle(), browseFeedDTO.getPosts().get(3).getTitle());
        Assertions.assertEquals(post4.getContent(), browseFeedDTO.getPosts().get(3).getContent());
    }

    @When("there are more than {int} posts")
    public void when_there_are_more_than_posts(Integer amount) throws Exception {
        // Add posts
        addPosts(amount + 1);

        // Make request
        result = mockMvc.perform(requestBuilder);
    }

    @Then("the results are paginated with {int} posts per page")
    public void then_the_results_are_paginated_with_posts_per_page(Integer numberOfPosts) throws Exception {
        // Check request
        MvcResult response = result.andExpectAll(
                status().is(200),
                view().name("posts/feedPage")
            ).andReturn();

        // Check for just 10 posts
        Assertions.assertEquals(numberOfPosts, browseFeedDTO.getPosts().size());
    }

    @When("I click the {string} page button underneath the results")
    public void i_click_a_button_underneath_the_results(String input) {
        requestBuilder = MockMvcRequestBuilders
                .get("/feed/browse")
                .param("page", getPage(input));
    }

    @Then("I am taken to the {string} page")
    public void i_am_taken_to_a_page(String input) throws Exception {
        // Check request
        MvcResult response = result.andExpectAll(
                status().is(200),
                view().name("posts/feedPage")
        ).andReturn();

        // Check current page
        Assertions.assertEquals(getPage(input), Integer.toString(browseFeedDTO.getParsedPage()));
    }
}
