package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ImageController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ImageControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    ImageController imageController;

    @Autowired
    GardenRepository gardenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() throws JsonProcessingException {
        gardenRepository.deleteAll();
        userRepository.deleteAll();

        // Save user
        User user = userRepository.save(new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList()));

        // Create MVC
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(imageController)
                .build();
    }

    /**
     * Cleanup user after each test to prevent duplicate users and posts causing errors
     */
    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    /**
     * Helper function for determining mimetype of file name.
     * @param fileName name of file
     * @return the mime type
     */
    private String determineMimeType(String fileName) {
        // Return the MIME type based on the file extension
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream"; // Default type for unknown formats
    }

    @ParameterizedTest
    @ValueSource(strings = {"gif_valid.gif", "jpg_valid.jpg", "png_valid.png", "svg_valid.svg"})
    void GetExistingImages_NoError(String filename) throws Exception {
        Path testImagesPath = Paths.get(ResourceUtils.getFile("classpath:test-images").toURI());

        File testImage = new File(testImagesPath.toFile(), filename);

        try (FileInputStream inputStream = new FileInputStream(testImage)) {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    testImage.getName(),
                    null,
                    inputStream
            );

            // Call the saveImage method
            String savedFileName = imageService.saveImage(multipartFile);

            this.mockMvc.perform(get("/images/" + savedFileName))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", determineMimeType(savedFileName))
                    );

            Path rootLocation = Paths.get("src/main/resources/public/");
            Path savedFilePath = Paths.get(rootLocation.toString(), savedFileName);
            Files.deleteIfExists(savedFilePath);

        }
    }

    @Test
    void GetNonExistingImages_Error() throws Exception {
        this.mockMvc.perform(get("/images/" + "fake-image-path.png"))
                .andExpect(status().isNotFound())
                .andReturn();

    }
}
