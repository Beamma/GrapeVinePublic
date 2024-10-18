package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.CommentController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.CommentDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for post profanity checking feature.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class PostProfanityCheckingFeature {
    @Autowired
    private PostController postController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentController commentController;

    private MockMvc mockMvc;
    private MockMvc commentMockMvc;

    private CommentDTO commentDTO;

    private Post post;

    private ResultActions result;

    private User user;

    private PostDTO postDTO;

    @Before("@U90011")
    public void setup() {
        // Save user
        user = userRepository.save(new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList()));

        // Setup PostDTO
        postDTO = new PostDTO("", "");
        post = postRepository.save(new Post("title", "content", user));

        // Create MVC
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .build();

        commentMockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .build();
    }

    /**
     * Cleanup user after each test to prevent duplicate users and posts causing errors
     */
    @After("@U90011")
    @Transactional
    public void cleanup() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @Given("I input a valid post title")
    public void i_input_a_valid_post_title() {
        postDTO.setTitle("Valid");
    }

    @Given("I input a valid post content")
    public void i_input_a_valid_post_content() {
        postDTO.setContent("Valid");
    }

    @When("I click create")
    public void i_click_create() throws Exception {
        result = mockMvc.perform(post("/post/add").flashAttr("postDTO", postDTO));
    }

    @Then("The post is successfully created")
    public void the_post_is_successfully_created() throws Exception {
        result.andExpect(redirectedUrl("/feed/browse"));

        List<Post> repoPosts = postRepository.findAll();

        boolean postExists = repoPosts.stream()
                .anyMatch(post -> "Valid".equals(post.getTitle()) && "Valid".equals(post.getContent()));

        Assertions.assertTrue(postExists, "Post with title 'Valid' and content 'Valid' should exist");
    }

    @Given("I input a profane word for my post title")
    public void i_input_a_profane_word_for_my_post_title() {
        postDTO.setTitle("fuck");
    }

    @Then("an error message tells me that the submitted post title is not appropriate")
    public void an_error_message_tells_me_that_the_submitted_post_title_is_not_appropriate_and_the_post_is_not_posted() throws Exception {
        result.andExpect(model().
                attributeHasFieldErrorCode("postDTO", "title", "401"));
    }

    @Then("the user’s count of inappropriate submissions is increased by {int}.")
    public void the_user_s_count_of_inappropriate_submissions_is_increased_by(Integer int1) {
        Assertions.assertEquals((int) int1, userRepository.findById(user.getId()).get().getInappropriateWarningCount());
    }

    @Given("I input a profane word for my post content")
    public void i_input_a_profane_word_for_my_post_content() {
        postDTO.setContent("fuck");
    }

    @Then("an error message tells me that the submitted post content is not appropriate")
    public void an_error_message_tells_me_that_the_submitted_post_content_is_not_appropriate_and_the_post_is_not_posted() throws Exception {
        BindingResult bindingResult = (BindingResult) result.andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(error -> {
            Assertions.assertEquals("401", error.getCode());
            Assertions.assertTrue(error.getDefaultMessage().contains("does not match the language standards of the app."));
        });
    }

    @Then("the post is not posted")
    public void post_is_not_created() {
        Assertions.assertEquals(1, postRepository.findAll().size());
    }

    @Then("I see an error message saying {string}")
    public void i_see_an_error_message_saying(String string) throws Exception {
        BindingResult bindingResult = (BindingResult) result.andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        boolean errorExists = bindingResult.getFieldErrors().stream()
                .anyMatch(error -> "401".equals(error.getCode()) && error.getDefaultMessage().contains(string));

        Assertions.assertTrue(errorExists, "Expected error message containing: " + string);
    }

    @Given("I input a profane word for my comment")
    public void i_input_a_profane_word_for_my_comment() {
        commentDTO = new CommentDTO("ass", user, post);
    }

    @When("I click post comment")
    public void i_click_post_comment() throws Exception {
        result = commentMockMvc.perform(
                put("/post/" + post.getId() + "/comment")
                        .content("{\"comment\": \"" + commentDTO.getComment()+"\"}").contentType(MediaType.APPLICATION_JSON));
    }

    @Then("the comment is not posted")
    public void the_comment_is_not_posted() {
        Assertions.assertEquals(0, commentRepository.findAll().size());
    }

    @Then("I see an error message on my comment saying {string}")
    public void i_see_an_error_message_on_my_comment_saying(String message) throws UnsupportedEncodingException {
        Assertions.assertEquals(message, result.andReturn().getResponse().getContentAsString());
    }
}
