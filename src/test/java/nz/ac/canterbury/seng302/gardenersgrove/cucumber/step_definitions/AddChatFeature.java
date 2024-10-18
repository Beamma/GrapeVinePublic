package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ChatController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Chat;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ChatRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class AddChatFeature {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ChatRepository chatRepository;
    @Autowired
    LivestreamRepository livestreamRepository;
    @Autowired
    ChatController chatController;

    MockMvc mockMvc;
    MockHttpServletRequestBuilder requestBuilder;
    ResultActions result;

    private Livestream livestream;
    private User user;

    @Before("@U90023")
    public void setUp() {
        user = userRepository.save(new User("liam@email.com",
                "2000-01-01",
                "Liam",
                "Ceelen",
                false,
                "Password1!",
                "Password1!"));

        livestream = livestreamRepository.save(new Livestream(user, "Livestream", "", "fake/path"));

        // Auth user
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @After("@U90023")
    @Transactional
    public void tearDown() {
        chatRepository.deleteAll();
        livestreamRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Given("I am watching a livestream")
    public void i_am_watching_a_livestream() {
        requestBuilder = MockMvcRequestBuilders.post("/chat/add")
                .param("streamId", livestream.getId().toString());
    }
    @Given("I have typed a chat message {string}")
    public void i_have_typed_a_chat_message_blue_sky_chat(String chatMessage) {
        Map<String, String> requestBody = Map.of("streamId", livestream.getId().toString(), "name", user.getFirstName(), "message", chatMessage, "timePosted", LocalDateTime.now().toString());

        requestBuilder.content(String.valueOf(new JSONObject(requestBody)))
                .contentType(MediaType.APPLICATION_JSON);
    }
    @When("I submit the chat")
    public void i_submit_the_chat() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }
    @Then("The chat is added to the database")
    public void the_chat_is_added_to_the_database() {
        Assertions.assertEquals(1, ((List<Chat>) chatRepository.findAll()).size());
    }

    @Then("I see a chat error saying {string}")
    public void i_see_a_chat_error_saying(String errorMessage) throws Exception {
        result.andExpectAll(
                status().is4xxClientError(),
                jsonPath("$").value(errorMessage)
        );
    }
    @Then("The chat is not added to the database")
    public void the_chat_is_not_added_to_the_database() {
        Assertions.assertEquals(0, ((List<Chat>) chatRepository.findAll()).size());
    }

    @Given("I have typed a chat message that is {int} chars long")
    public void i_have_typed_a_chat_message_that_is_chars_long(Integer chatlength) {
        String message = "A".repeat(chatlength);
        Map<String, String> requestBody = Map.of("streamId", livestream.getId().toString(), "name", user.getFirstName(), "message", message, "timePosted", LocalDateTime.now().toString());
        requestBuilder.content(String.valueOf(new JSONObject(requestBody)))
                .contentType(MediaType.APPLICATION_JSON);
    }

}
