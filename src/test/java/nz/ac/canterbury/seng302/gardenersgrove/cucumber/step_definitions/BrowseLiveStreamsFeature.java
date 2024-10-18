package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LiveStreamController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseFeedDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseLiveStreamsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
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
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class BrowseLiveStreamsFeature {

    @Autowired
    LiveStreamController liveStreamController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LivestreamRepository livestreamRepository;
    private MockHttpServletRequestBuilder requestBuilder;
    MockMvc mockMvc;
    ResultActions result;

    User currentUser;
    User streamingUser;
    Livestream livestream1;
    LiveStreamDTO livestreamDTO;

    @Before("@U90020")
    public void setup() {
        User saveUser = new User("livestreamTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        currentUser = userRepository.save(saveUser);

        //Active streaming user
        User saveActiveUser = new User("livestreamStreamer@gmail.com", "2001-01-01", "Jane", "Doe", false, "Password1!", "Password1!");
        streamingUser = userRepository.save(saveActiveUser);

        var authentication = new UsernamePasswordAuthenticationToken(currentUser.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Create a livestream for AC2
        Livestream tempLivestream = new Livestream(streamingUser, "Livestream: Jane","Watch me grow some carrots","");
        livestream1 = livestreamRepository.save(tempLivestream);

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(liveStreamController).setViewResolvers(viewResolver).build();
    }

    /**
     * Clears out the database after use.
     */
    @After("@U90020")
    @Transactional
    public void tearDown() {
        livestreamRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Given("I am logged in and anywhere on the system")
    public void i_am_logged_in_and_anywhere_on_the_system() {
        requestBuilder = MockMvcRequestBuilders
                .get("/livestream/browse");
    }

    @When("I hit the browse livestreams button")
    public void i_hit_the_browse_livestreams_button() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("I am taken to the browse livestreams page")
    public void i_am_taken_to_the_browse_livestreams_page() throws Exception {
        result.andExpectAll(
                status().is(200),
                view().name("livestream/browse"));
    }

    @When("I hit the browse livestreams button and a streamer is active")
    public void i_hit_the_browse_livestreams_button_and_a_streamer_is_active() throws Exception {
        result = mockMvc.perform(requestBuilder);
        livestreamDTO = (LiveStreamDTO) result.andReturn().getModelAndView().getModel().get("livestreamDTO");
    }

    @Then("I can see information about active livestreams")
    public void i_can_see_information_about_active_livestreams() throws Exception {
        result.andExpectAll(
                status().is(200),
                view().name("livestream/browse")
        ).andReturn();

        // Check livestream information (title, description)
        Assertions.assertEquals(livestream1.getTitle(), livestreamDTO.getTitle());
        Assertions.assertEquals(livestream1.getDescription(), livestreamDTO.getDescription());
    }

    @Given("There are {int} current live-streams")
    public void there_current_live_streams(int totalLivestreams) {
        // -1 because the livestream repository already contains a live stream (AC2, Jane)
        for (int i = 0; i < totalLivestreams - 1; i++) {
            livestreamRepository.save(new Livestream(currentUser, "Livestream" + i, "" + i, "fake/path"));
        }
        Assertions.assertEquals(totalLivestreams, livestreamRepository.findAll().size());
    }
    @When("I got to the browse livestreams page")
    public void i_got_to_the_browse_livestreams_page() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse"));
    }
    @Then("I only see the first {int} livestreams")
    public void i_only_see_the_first_livestreams(int numLivestreams ) {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        Assertions.assertEquals(numLivestreams, livestreamList.size());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getPageSize());
    }

    @When("I hit enter on the URL with a page number {string}")
    public void i_hit_enter_on_the_url(String page) throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse").param("page", page));
    }
    @Then("I am taken to the first page")
    public void i_am_taken_to_the_first_page() {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");

        Assertions.assertEquals(1, browseLiveStreamsDTO.getParsedPage());
    }

    @Then("I am taken to the last page")
    public void i_am_taken_to_the_last_page() {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        Assertions.assertEquals(5, livestreamList.size());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getPageSize());
        Assertions.assertEquals(2, browseLiveStreamsDTO.getParsedPage());
    }

    @Then("I see {int} livestreams")
    public void i_see_livestreams(int numOfLivestreams) {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        Assertions.assertEquals(numOfLivestreams, livestreamList.size());

    }
    @Then("it says I am on page {int}")
    public void it_says_i_am_on_page(Integer pageNum) {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");
        Assertions.assertEquals(pageNum, browseLiveStreamsDTO.getParsedPage());
    }

    @Then("I see the text saying Showing results {int} to {int} of {int}")
    public void i_see_the_text_saying(int first, int last, int total) {
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.andReturn().getModelAndView().getModel().get("browseLiveStreamsDTO");

        // This line is taken from the html template, which is used to calculate the last item on the page
        int calculatedLastOfPage = ((browseLiveStreamsDTO.getParsedPage() * browseLiveStreamsDTO.getPageSize()) < browseLiveStreamsDTO.getLivestreams().size() ? (browseLiveStreamsDTO.getParsedPage() * browseLiveStreamsDTO.getPageSize()) : (((browseLiveStreamsDTO.getParsedPage() - 1) * browseLiveStreamsDTO.getPageSize()) + browseLiveStreamsDTO.getLivestreams().size()));

        Assertions.assertEquals(total, browseLiveStreamsDTO.getNumberOfLivestreams());
        Assertions.assertEquals(first, ((browseLiveStreamsDTO.getParsedPage() - 1) * browseLiveStreamsDTO.getPageSize()) + 1);
        Assertions.assertEquals(last, calculatedLastOfPage);
    }
}
