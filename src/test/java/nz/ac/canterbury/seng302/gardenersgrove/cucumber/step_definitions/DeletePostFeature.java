package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class DeletePostFeature {

    @Autowired
    PostController postController;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProfanityFilterService profanityFilterService;

    MockMvc mockMvc;
    ResultActions result;

    User user;
    Post post;
    Comment comment1;
    Comment comment2;
    Comment comment3;

    /**
     * Set up the required state in the database
     */
    @Before("@U9007")
    public void setUp() throws JsonProcessingException {

        // Create user
        user = userRepository.save(new User("user@example.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Auth user
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create mock MVC
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .build();
    }

    /**
     * Clears out the database after use.
     */
    @After("@U9007")
    @Transactional
    public void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Given("I have created a post with comments")
    public void i_have_created_a_post_with_comments() {
        // Create post
        post = postRepository.save(new Post("Post", "TestContent", user));

        // Create comments
        comment1 = commentRepository.save(new Comment(post, user, "Comment1"));
        comment2 = commentRepository.save(new Comment(post, user, "Comment2"));
        comment3 = commentRepository.save(new Comment(post, user, "Comment3"));
    }

    @When("I click delete")
    public void i_click_delete() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + post.getId()));
    }

    @Then("the post and it's comments have been deleted")
    public void the_post_and_it_s_comments_have_been_deleted() throws Exception {
        // Check request code
        result.andExpect(status().isOk());

        // Check deleted
        Assertions.assertNull(postRepository.findById(post.getId()).orElse(null));
        Assertions.assertNull(commentRepository.findById(comment1.getId()).orElse(null));
        Assertions.assertNull(commentRepository.findById(comment2.getId()).orElse(null));
        Assertions.assertNull(commentRepository.findById(comment3.getId()).orElse(null));
    }
}
