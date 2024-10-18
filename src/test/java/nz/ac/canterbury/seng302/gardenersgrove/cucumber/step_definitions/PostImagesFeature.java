package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Test class for post images feature.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class PostImagesFeature {
    @Autowired
    private PostController postController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private static final String TEST_IMG_DIR = "src/test/resources/test-images/";

    private static final String IMG_SAVE_DIR = "src/main/resources/public/";

    private MockMvc mockMvc;

    private MvcResult result;

    private User user;

    private PostDTO postDTO;

    @Before("@U90010")
    public void setup() {
        // Save user
        user = userRepository.save(new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList()));

        // Create MVC
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .build();
    }

    /**
     * Cleanup user after each test to prevent duplicate users and posts causing errors
     */
    @After("@U90010")
    @Transactional
    public void cleanup() {
        postRepository.deleteAll();
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

    @Given("I have created a valid post")
    public void i_have_created_a_valid_post() {
        postDTO = new PostDTO("Title", "Content");
    }

    @Given("add the image {string}")
    public void add_a_valid_image(String filename) throws IOException {
        postDTO.setImage(readFile(filename));
    }

    @When("I click post")
    public void i_click_post() throws Exception {
        result = mockMvc.perform(post("/post/add").flashAttr("postDTO", postDTO)).andReturn();
    }

    @Then("the post and the image are saved")
    public void the_post_and_the_image_are_saved() throws IOException {
        // Get post
        Post post = postRepository.findPostByTitle(postDTO.getTitle()).get(0);
        Path filePath = Paths.get(IMG_SAVE_DIR, post.getImagePath());

        // Check added to DB
        Assertions.assertEquals(postDTO.getTitle(), post.getTitle());

        // Check image added
        Assertions.assertTrue(Files.exists(filePath));

        // Remove image
        Files.delete(filePath);
    }

    @Then("i am shown the message {string}")
    public void i_am_shown_the_message(String message) {
        // Invalid Checks
        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        // Check has errors
        Assertions.assertTrue(bindingResult.hasErrors());

        // Check correct error message
        Assertions.assertEquals(message, bindingResult.getFieldError("image").getDefaultMessage());

        // Check not added to DB
        Assertions.assertEquals(0, postRepository.findPostByTitle(postDTO.getTitle()).size());
    }
}