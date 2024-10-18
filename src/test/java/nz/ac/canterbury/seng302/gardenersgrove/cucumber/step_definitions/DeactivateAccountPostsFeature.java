package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AuthController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.CommentController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.UserController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class DeactivateAccountPostsFeature {

    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostController postController;
    @Autowired
    private CommentController commentController;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private AuthController authController;
    @Autowired
    private UserController userController;

    private MockMvc mockMvc;
    private ResultActions result;
    private MvcResult mvcResult;
    private User user;
    private Post post;

    @Before("@U90012")
    public void startup() {
        user = userRepository.save(new User("test@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));
        post = postRepository.save(new Post("Title", "Content", user));


        mockMvc = MockMvcBuilders
                .standaloneSetup(postController, commentController, authController, userController)
                .build();
    }

    @After("@U90012")
    @Transactional
    public void cleanup() {
        userRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Given("I have entered {int} inappropriate submissions")
    public void i_have_entered_inappropriate_submissions(Integer count) {
        var authentication = new UsernamePasswordAuthenticationToken("test@gmail.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        user.setInappropriateWarningCount(count);
        userRepository.save(user);
    }
    @When("I enter another inappropriate submission")
    public void i_enter_another_inappropriate_submission() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.post("/post/add")
                .flashAttr("postDTO", new PostDTO("valid", "ass")));
    }
    @Then("I see a popup message that I have added five inappropriate submissions")
    public void i_see_a_popup_message_that_i_have_added_five_inappropriate_submissions() throws Exception {
        String fifthCheck = (String) result.andReturn().getModelAndView().getModel().get("fifthInappropriateSubmission");
        Assertions.assertTrue(Boolean.parseBoolean(fifthCheck));
    }
    @Then("I receive an email warning me that I may get blocked for one week")
    public void i_receive_an_email_warning_me_that_i_may_get_blocked_for_one_week() {
        String recipient = "test@gmail.com";
        String subject = "Fifth inappropriate content submission warning";
        String htmlTemplateName = "fifth-inappropriate-content-warning";
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                Mockito.verify(emailService).sendEmail(eq(recipient), eq(subject),eq(htmlTemplateName), Mockito.any())
        );
    }



    @When("I enter a fifth inappropriate comment")
    public void i_enter_a_fifth_inappropriate_comment() throws Exception {
        result = mockMvc.perform(put("/post/" + post.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON) // from ChatGPT
                        .content("{\"comment\":\"shit\"}"))
                .andExpectAll(
                        status().is(400),
                        header().exists("Fifth-Inappropriate-Submission"),
                        header().stringValues("Fifth-Inappropriate-Submission", "true")
                );
    }

    @Then("I see a popup message that I have added five inappropriate comments")
    public void i_see_a_popup_message_that_i_have_added_five_inappropriate_comments() throws Exception {
        result.andExpectAll(
                status().is(400),
                header().exists("Fifth-Inappropriate-Submission"),
                header().stringValues("Fifth-Inappropriate-Submission", "true")
        );
    }

    // AC 2

    @When("I enter a sixth inappropriate submission")
    public void i_enter_a_sixth_inappropriate_submission() throws Exception {
        result = mockMvc.perform(put("/post/" + post.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON) // from ChatGPT
                        .content("{\"comment\":\"shit\"}"));

        mvcResult = result.andReturn();


    }

    @Then("I am logged out of the system")
    public void i_am_logged_out_of_the_system() throws UnsupportedEncodingException, JsonProcessingException {
        Assertions.assertEquals(302, mvcResult.getResponse().getStatus());
        String responseBody = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>(){});

        // Assert body
        Assertions.assertEquals(true, responseMap.get("redirected"));
        Assertions.assertEquals("../auth/logout-banned", responseMap.get("url"));
    }

    @And("I see a popup message that I have been blocked for one week")
    public void i_see_a_popup_message_that_i_have_been_blocked_for_one_week() {
        // Needs to be verified on the frontend
    }
    @And("I receive an email that I have been blocked for one week")
    public void i_receive_an_email_that_i_have_been_blocked_for_one_week() {
        String recipient = "test@gmail.com";
        String subject = "Blocked Account";
        String htmlTemplateName = "blocked-account-email";
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                Mockito.verify(emailService).sendEmail(eq(recipient), eq(subject),eq(htmlTemplateName), Mockito.any())
        );
    }

}
