package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LiveStreamController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class CreateLiveStreamFeature {
    @Autowired
    LiveStreamController liveStreamController;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LivestreamRepository livestreamRepository;
    private static final String TEST_IMG_DIR = "src/test/resources/test-images/";

    private static final String IMG_SAVE_DIR = "src/main/resources/public/";
    private MockHttpServletRequestBuilder requestBuilder;
    MockMvc mockMvc;
    ResultActions result;
    private LiveStreamDTO liveStreamDTO;
    @Before("@U90019")
    public void setup() {
        User saveUser = new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        User user = userRepository.save(saveUser);
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        liveStreamDTO = new LiveStreamDTO("title", "description");

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(liveStreamController).setViewResolvers(viewResolver).build();
    }

    /**
     * Clears out the database after use.
     */
    @After("@U90019")
    @Transactional
    public void tearDown() {
        livestreamRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Reads a file from the specified directory and returns it as a MultipartFile.
     *
     * @param fileName The name of the file to be read
     * @return The file as a MultipartFile
     * @throws IOException if file can not be read
     */
    public MultipartFile readFile(String fileName) throws IOException {
        File file = new File(TEST_IMG_DIR + File.separator + fileName);
        try (FileInputStream input = new FileInputStream(file)) {
            return new MockMultipartFile(fileName, file.getName(), URLConnection.guessContentTypeFromName(file.getName()), input);
        }
    }

    @Given("I am on the create live stream form")
    public void i_am_on_the_create_live_stream_form() {
        requestBuilder = MockMvcRequestBuilders.post("/livestream/create");
    }
    @Given("I enter the title {string}")
    public void i_enter_the_title(String title) {
        liveStreamDTO.setTitle(title);
        requestBuilder.flashAttr("livestreamDTO", liveStreamDTO);
    }
    @When("I select the create live stream button")
    public void i_select_the_create_live_stream_button() throws Exception {
        result = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    @Then("I see an error message that tells me that {string} in the {string} field")
    public void i_see_an_error_message_that_tells_me_that_in_the_field(String message, String field) throws Exception {
        BindingResult bindingResult = (BindingResult) result.andReturn()
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT

        String providedErrorMessage = bindingResult.getFieldErrors(field).getFirst().getDefaultMessage();
        Assertions.assertEquals(message, providedErrorMessage);
    }

    @Given("I put a title of length {int}")
    public void i_put_a_title_of_length(Integer titleLength) {
        liveStreamDTO.setTitle("a".repeat(titleLength));
        requestBuilder.flashAttr("livestreamDTO", liveStreamDTO);
    }
    @Given("I enter the description {string}")
    public void i_enter_the_description(String description) {
        liveStreamDTO.setDescription(description);
        requestBuilder.flashAttr("livestreamDTO", liveStreamDTO);
    }
    @Given("I put a description of length {int}")
    public void i_put_a_description_of_length(Integer descriptionLength) {
        liveStreamDTO.setDescription("a".repeat(descriptionLength));
        requestBuilder.flashAttr("livestreamDTO", liveStreamDTO);
    }

    @Given("I have created a stream with a valid title and valid description")
    public void i_have_created_a_stream_with_a_valid_title_and_valid_description() {}

    @And("add the thumbnail {string}")
    public void add_the_thumbnail(String filename) throws IOException {
        liveStreamDTO.setImage(readFile(filename));
    }

    @When("I click Start Stream")
    public void i_click_start_stream() throws Exception {
        requestBuilder = MockMvcRequestBuilders.post("/livestream/create");
        requestBuilder.flashAttr("livestreamDTO", liveStreamDTO);

        result = mockMvc.perform(requestBuilder);
    }

    @Then("the stream and the thumbnail are saved")
    public void the_stream_and_the_thumbnail_are_saved() throws IOException {
        Livestream livestream = livestreamRepository.findLivestreamByTitle(liveStreamDTO.getTitle()).get(0);
        Path filePath = Paths.get(IMG_SAVE_DIR, livestream.getImagePath());

        Assertions.assertEquals(liveStreamDTO.getTitle(), livestream.getTitle());
        Assertions.assertTrue(Files.exists(filePath));
        // Remove image
        Files.delete(filePath);
    }

    @Then("i am shown the error message {string}")
    public void i_am_shown_the_message(String expectedMessage) {
        String field = "image";
        BindingResult bindingResult = (BindingResult) result.andReturn()
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT

        String providedErrorMessage = bindingResult.getFieldErrors(field).getFirst().getDefaultMessage();
        Assertions.assertEquals(expectedMessage, providedErrorMessage);
    }
}
