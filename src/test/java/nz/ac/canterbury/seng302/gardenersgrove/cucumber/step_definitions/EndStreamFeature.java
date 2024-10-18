package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AgoraController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LiveStreamController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseLiveStreamsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class EndStreamFeature {
    @Autowired
    UserRepository userRepository;
    @Autowired
    LiveStreamController liveStreamController;
    @Autowired
    AgoraController agoraController;
    @Autowired
    LivestreamRepository livestreamRepository;

    private User user;
    private User otherUser;
    private MockMvc mockMvc;
    private ResultActions result;
    private Livestream livestream;
    private Livestream otherLivestream;

    @Before("@U90022")
    public void setup() {
        user = userRepository.save(new User("test@email.com", "2002-12-12", "First", "", true, "password", "password"));
        otherUser = userRepository.save(new User("other@email.com", "2002-12-12", "First", "", true, "password", "password"));
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        mockMvc = MockMvcBuilders.standaloneSetup(liveStreamController, agoraController).build();
    }

    @After("@U90022")
    @Transactional
    public void tearDown() {
        livestreamRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Given("I have created a livestream with a title {string} and description {string}")
    public void i_have_created_a_livestream_with_a_title_and_description(String title, String desc) {
        livestream = livestreamRepository.save(new Livestream(user, title, desc, null));
    }
    @When("I click the end stream button and confirm on the modal to end the stream")
    public void i_click_the_end_stream_button_and_confirm_on_the_modal_to_end_the_stream() throws Exception {
        result = mockMvc.perform(delete("/livestream/end/" + livestream.getId()));
    }
    @Then("The stream is ended")
    public void the_stream_is_ended() throws Exception {
        result.andExpect(content().string("success"));
        Assertions.assertNull(livestreamRepository.findById(livestream.getId()).orElse(null));
    }
    @Then("I am taken back to the browse livestreams feed page")
    public void i_am_taken_back_to_the_browse_livestreams_feed_page() {
        // Manual Tested
    }

    @When("I create another livestream with a title {string} and description {string}")
    public void i_create_another_livestream_with_a_title_and_description(String title, String desc) throws Exception {
        // Create livestream
        LiveStreamDTO newLiveStream = new LiveStreamDTO(title, desc);

        // Send request
        result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", newLiveStream));
    }

    @Then("I receive an error message")
    public void i_receive_an_error_message() throws Exception {
        BindingResult bindingResult = (BindingResult) result.andReturn()
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT
        String existingLiveStreamId = bindingResult.getGlobalErrors().getFirst().getDefaultMessage();
        //Check that the error links to the correct stream
        Assertions.assertEquals(livestream.getId().toString(), existingLiveStreamId);
    }

    @Given("I am on the browse live stream feed page and I have not started a livestream")
    public void i_am_on_the_browse_live_stream_feed_page_and_i_have_not_started_a_livestream() {
        otherLivestream = livestreamRepository.save(new Livestream(otherUser, "other", "livestream", null));
    }

    @When("I click the watch stream button for any streamer")
    public void i_click_the_watch_stream_button_for_any_streamer() {
        // Manual tested
    }

    @Then("I cannot end their stream")
    public void i_cannot_end_their_stream() throws Exception {
        // Send request
        mockMvc.perform(delete("/livestream/end/" + otherLivestream.getId()))
                .andExpect(content().string("error"));

        // Check that other livestream has not been deleted
        Assertions.assertNotNull(livestreamRepository.findById(otherLivestream.getId()).orElse(null));
    }

    @When("I navigate to the browse livestream page")
    public void i_navigate_to_the_browse_livestream_page() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse/")
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Then("it is no longer visible on the browse livestreams page")
    public void it_is_no_longer_visible_on_the_browse_livestreams_page() {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        // Check correct livestream returned
        Assertions.assertTrue(livestreamList.isEmpty());
    }
}
