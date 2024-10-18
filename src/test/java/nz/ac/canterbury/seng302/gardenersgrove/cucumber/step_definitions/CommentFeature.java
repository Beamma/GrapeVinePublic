package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.CommentController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LikeController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Likes;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentLikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LikeService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class CommentFeature {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentController commentController;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentLikeRepository commentLikeRepository;
    @Autowired
    LikeService likeService;
    @Autowired
    LikeController likeController;
    MockMvc mockMvc;
    MockHttpServletRequestBuilder requestBuilder;
    ResultActions result;

    User user;
    User user2;

    Comment comment;
    Post post;
    @Autowired
    ProfanityFilterService profanityFilterService;

    /**
     * Set up the required state in the database
     */
    @Before("@U9005")
    public void setUp() {

        // Create user
        user = userRepository.save(new User("user@example.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Create other user
        user2 = userRepository.save(new User("user2@example.com", "2001-01-01", "Jane", "Doe", false, "Password1!", "Password1!"));

        // Create post
        post = postRepository.save(new Post("Post", "TestContent", user));

        // Create a comment from user2 on the post
        comment = new Comment(post, user2, "TestComment");
        commentRepository.save(comment);

        likeService.addCommentLike(user, comment);

        //saves 17 comments, since pagination requires at least  10 comments to be tested
        for (int i = 0; i < 17; i++) {
            commentRepository.save(new Comment(post, user, "this is a comment"));
        }

        Comment lastComment = new Comment(post, user2, "LastComment");
        commentRepository.save(lastComment);

        // Auth user
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create mock MVC
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController, likeController)
                .build();
    }

    /**
     * Clears out the database after use.
     */
    @After("@U9005")
    @Transactional
    public void tearDown() {
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void eachTest() throws JsonProcessingException {
        Mockito.when(profanityFilterService.isTextProfane("Fuck")).thenReturn(true);
    }

    @Given("I type a comment with the message {string}")
    public void iTypeAComment(String message){
        requestBuilder = MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                .content("{\"comment\": \"" + message + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
    }

    @When("I submit the comment")
    public void iSubmitTheComment() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("I can see an error message {string}")
    public void errorIsThrown (String errorMessage) throws Exception {
        result.andExpectAll(
                MockMvcResultMatchers.status().is4xxClientError(),
                MockMvcResultMatchers.content().string(errorMessage)
        );
    }
    @Then("the comment has been added to the post")
    public void the_comment_has_been_added_to_the_post() throws Exception {
        result.andExpect(
                MockMvcResultMatchers.status().isCreated());
    }

    // AC3 Stepdefs

    @Given("I am on the view post page")
    public void iAmOnTheViewPostPage() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/feed/browse");
        result = mockMvc.perform(requestBuilder);
    }

    @And("I can see a comment from another user")
    public void iCanSeeAPostFromAnotherUser(){
        // comment is added in the setup
    }

    @When("I click the like button")
    public void iClickTheLikeButton() throws Exception {
        requestBuilder =
                MockMvcRequestBuilders.put("/comment/"+ comment.getId() + "/like");
        result = mockMvc.perform(requestBuilder);
    }

    @Given("I can see more than three comments on a post")
    public void i_can_see_more_than_three_comments_on_a_post() {
        requestBuilder = MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                .param("page", String.valueOf(0));
    }

    @When("I click the See more comments button")
    public void i_click_the_see_more_comments_button() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("I can see an additional {int} comments")
    public void i_can_see_an_additional_comments(int expectedSize) throws Exception {
        result
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.comments.length()").value(expectedSize));
    }

    @Then("the like counter for the comment increases by one")
    public void theLikeCounterForTheCommentIncreasesByOne() throws Exception {
        result.andExpect(status().isOk());
        Assertions.assertEquals(1, likeService.getCommentLikeCountByCommentId(comment.getId()));
    }

    @Given("I have typed an inappropriate comment {string}")
    public void i_have_typed_an_inappropriate_commentString (String profaneComment) {
        requestBuilder = MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                .content("{\"comment\": \"" + profaneComment + "\"}")
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Then("I am informed the comment is inappropriate {string} and my profanity count increases")
    public void i_am_informed_the_comment_is_inappropriate_and_my_profanity_count_increases(String errorMessage) throws Exception {
        result.andExpectAll(
                MockMvcResultMatchers.status().is4xxClientError(),
                MockMvcResultMatchers.content().string(errorMessage));

        //change value to 1 after U90012 is implemented
        Assertions.assertEquals(0, user.getInappropriateWarningCount());
    }

}
