package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;//package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;



import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.bs.A;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PostService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class NewTextPostFeature {

    PostDTO postValidTitleValidContent;
    PostDTO postValidTitleInvalidContent;
    PostDTO postInvalidTitleValidContent;

    PostDTO postNoContent;

    PostDTO postNoTitle;

    PostDTO postNoTitleNoContent;

    PostDTO postTitleTooLong;

    PostDTO postContentTooLong;

    PostDTO postTitleProfane;

    PostDTO postContentProfane;
    private MockHttpServletRequestBuilder requestBuilder;

    User user;
    User user1;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    ProfanityFilterService profanityFilterService;

    @Autowired
    PostRepository postRepository;

    PostController postController;

    @Autowired
    GardenService gardenService;

    @Autowired
    PostService postService;

    @PersistenceContext
    private EntityManager entityManager;

    MockMvc mockMvc;

    @Autowired
    UserService userService;

    ResultActions result;


    @Before("@U9001")
    public void setup() throws JsonProcessingException {

        // Set up authenticated user

        User mockUser = new User("mockUser@gmail.com", "2001-01-01", "Mock", "User", false, "password", "password");
        userService.addUser(mockUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(mockUser.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create Post DTOs

        postValidTitleValidContent = new PostDTO("Valid Title", "Valid Content");

        postValidTitleInvalidContent = new PostDTO("Valid Title", "???!!");

        postInvalidTitleValidContent = new PostDTO("???!!", "Valid Content");

        postNoContent = new PostDTO("Valid Title", "");

        postNoTitle = new PostDTO("", "Valid Content");

        postNoTitleNoContent = new PostDTO("", "");

        postTitleTooLong = new PostDTO("A".repeat(65), "Valid Content");

        postContentTooLong = new PostDTO("Valid Title", "A".repeat(513));

        postTitleProfane = new PostDTO("badWord", "Valid Content");

        postContentProfane = new PostDTO("Valid Title", "badWord");

        profanityFilterService = Mockito.mock(ProfanityFilterService.class);


        postController = new PostController(userService, gardenService, postService, profanityFilterService, eventPublisher);
        // Mock the profanity filter service

        Mockito.when(profanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));

        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();

    }

    @After("@U9001")
    @Transactional
    public void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.createNativeQuery("DELETE FROM POST").executeUpdate();

    }

    //AC1

    @Given("I am on the post feed page,")
    public void i_am_on_the_post_feed_page() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/feed/browse"))
                .andExpect(status().isOk());
    }

    @When("I select the \"New Text Post\" button,")
    public void i_select_the_new_post_button() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/post/add");
    }

    @Then("I am taken to the new text post page.")
    public void i_am_taken_to_the_new_text_post_page() throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    //AC2

    @Given("I am on the new text post page")
    public void i_am_on_the_new_text_post_page() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/post/add"))
                .andExpect(status().isOk());
    }

    @And("I enter valid values \\(including emojis) for the content and optionally a Title")
    public void i_enter_valid_values_for_the_content_and_optionally_a_title() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postValidTitleValidContent);
    }

    @When("I click the \"Post\" button,")
    public void i_click_the_post_button() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("a new text post is created and is set to public")
    public void a_new_text_post_is_created_and_is_set_to_public() {
        Post post = postRepository.findAll().get(0);
        Assertions.assertEquals(postValidTitleValidContent.getTitle(), post.getTitle());
        Assertions.assertEquals(postValidTitleValidContent.getContent(), post.getContent());
    }

    @And("I am taken to the post feed page.")
    public void i_am_taken_to_the_post_feed_page() throws Exception {
        result.andExpect(redirectedUrl("/feed/browse"));
    }

    //AC 3

    @And("I have not entered any details,")
    public void i_have_not_entered_any_details() {
        // Needs to be done in a manual test
    }

    @When("I click \"Cancel\",")
    public void i_click_cancel() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/feed/browse");
        result = mockMvc.perform(requestBuilder);
    }

    @Then("my post is discarded")
    public void my_post_is_discarded() {
        Assertions.assertEquals(0, postRepository.findAll().size());
    }

    @And("I get taken to the post feed page.")
    public void i_get_taken_to_post_feed_page_() throws Exception {
        result.andExpect(status().isOk());
    }

    // AC 4

    @And("I have entered values for the Title or Content,")
    public void i_have_entered_values_for_the_title_or_content() {
        // Needs to be manual tested / E2E - requires javascript
    }

    @When("I click the Cancel button")
    public void i_click_the_cancel_button() throws Exception {
        // Needs to be manual tested / E2E - requires javascript
    }

    @Then("a popup appears prompting me to confirm my action.")
    public void a_popup_appears_prompting_me_to_confirm_my_action() {
        // Needs to be manual tested / E2E - requires javascript
    }

    // AC 5

    @And("I enter an invalid \\(i.e. non-alphanumeric characters other than spaces, dots, commas, hyphens, or apostrophes) title")
    public void i_enter_an_invalid_title() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postInvalidTitleValidContent);
    }

    @Then("an error message tells me \"Post title must only include letters, numbers, spaces, dots, hyphens, or apostrophes\".")
    public void an_error_message_tells_me_post_title_must_only_include_letters_numbers_spaces_dots_hyphens_or_apostrophes() throws Exception {
        result.andExpect(model().

                attributeHasFieldErrorCode("postDTO", "title", "401"));
    }

    // AC 6

    @And("I enter an invalid \\(i.e. non-alphanumeric characters other than spaces, dots, commas, hyphens, or apostrophes) content,")
    public void i_enter_an_invalid_content() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postValidTitleInvalidContent);
    }

    @Then("an error message tells me \"Post content must only include letters, numbers, spaces, dots, hyphens, or apostrophes\".")
    public void an_error_message_tells_me_post_content_must_only_include_letters_numbers_spaces_dots_hyphens_or_apostrophes() throws Exception {
        result.andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"));
    }

    // AC 7

    @And("I enter a title that is longer than 64 characters,")
    public void i_enter_a_title_that_is_longer_than_64_characters() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postTitleTooLong);
    }

    @Then("an error message tells me “Post title must be 64 characters long or less”")
    public void an_error_message_tells_me_post_title_must_be_64_characters_long_or_less_and_the_post_is_not_created() throws Exception {
        result.andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"));
        Assertions.assertTrue(postRepository.findAll().isEmpty());
    }

    // AC 8

    @And("I enter a content that is longer than 512 characters,")
    public void i_enter_content_that_is_longer_than_512_characters() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postContentTooLong);
    }

    @Then("an error message tells me \"Post content must be 512 characters long or less\",")
    public void an_error_message_tells_me_post_content_must_be_512_characters_long_or_less() throws Exception {
        result.andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"));
    }

    @And("the post is not created.")
    public void the_post_is_not_created() {
        Assertions.assertTrue(postRepository.findAll().isEmpty());
    }

    // AC 9

    @Given("I enter Content that contains inappropriate words,")
    public void i_enter_a_title_that_contains_inappropriate_words() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postContentProfane);
    }

    @Then("an error message tells me that \"The content does not match the language standards of the app.\"")
    public void an_error_message_tells_me_post_title_contains_inappropriate_words_and_the_post_is_not_created() throws Exception {
        result.andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"));
        Assertions.assertTrue(postRepository.findAll().isEmpty());
    }

    // AC 10

    @Given("I enter a Title that contains inappropriate words,")
    public void i_enter_content_that_contains_inappropriate_words() {
        requestBuilder = MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", postTitleProfane);
    }

    @Then("an error message tells me that \"The title does not match the language standards of the app.\"")
    public void an_error_message_tells_me_post_content_contains_inappropriate_words_and_the_post_is_not_created() throws Exception {
        result.andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"));
        Assertions.assertTrue(postRepository.findAll().isEmpty());
    }

}

