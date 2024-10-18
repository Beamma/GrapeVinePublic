package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LikeController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Likes;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Step definitions for checking the post liking and unliking system
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class LikeAPostFeature {
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LikeController likeController;

    MockMvc mockMvc;
    MockHttpServletRequestBuilder requestBuilder;
    ResultActions result;

    User user;
    Post post;

    /**
     * Create the required state in the database
     */
    @Before("@U9003")
    public void setUp() {

        User saveUser = new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user = userRepository.save(saveUser);
        Post savePost = new Post("TestPost", "TestContent", user);
        post = postRepository.save(savePost);

        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders
                .standaloneSetup(likeController)
                .build();
    }

    /**
     * Clears out the database after use.
     */
    @After("@U9003")
    @Transactional
    public void tearDown() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Given("I am able to like a post")
    public void iAmAbleToLikePost() throws Exception {
        requestBuilder = MockMvcRequestBuilders.put("/post/" + post.getId() + "/like");
    }

    @Given("I am able to unlike a post")
    public void iAmAbleToUnlikePost() throws Exception {
        requestBuilder = MockMvcRequestBuilders.delete("/post/" + post.getId() + "/like");
    }


    @When("I interact with the like element on a post I have not liked before")
    public void iInteractWithTheLikeElementOnAPostIHaveNotLikedBefore() throws Exception {
        likeRepository.deleteAll();
        result = mockMvc.perform(requestBuilder);
    }

    @Then("I can see that I have liked the post")
    public void iCanSeeThatIHaveLikedThePost() throws Exception {
        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
    }

    @When("I interact with the like element on a post I have already liked")
    public void iInteractWithTheLikeElementOnAPostIHaveAlreadyLiked() throws Exception {
        Likes like = new Likes(post, user);
        likeRepository.save(like);
        result = mockMvc.perform(requestBuilder);

    }

    @Then("I can see that I no longer like the post")
    public void iCanSeeThatINoLongerLikeThePost() throws Exception {
        result
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
    }

    @Given("I am able to see the like count")
    public void i_am_on_the_post_feed_page() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/post/" + post.getId()+ "/like");
    }

    @When("I am looking at a post")
    public void looking_at_a_post() throws Exception {
        Likes like = new Likes(post, user);
        likeRepository.save(like);

        result = mockMvc.perform(requestBuilder);
    }

    @Then("I can see how many likes a post has")
    public void see_how_many_likes_a_post_has() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeExists").value(true));
    }
}
