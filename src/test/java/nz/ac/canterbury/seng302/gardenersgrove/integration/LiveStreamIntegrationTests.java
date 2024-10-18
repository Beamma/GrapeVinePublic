package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ImageController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.LiveStreamController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseLiveStreamsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LiveStreamIntegrationTests {

    @Autowired
    private LiveStreamController liveStreamController;

    @Autowired
    private ImageController imageController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LivestreamRepository livestreamRepository;

    // Test Variables
    private User hostUser;

    private User audienceUser;

    private LiveStreamDTO liveStreamDTO;

    private Livestream livestream;

    private MockMvc mockMvc;
    private static final String TEST_IMG_DIR = "src/test/resources/test-images/";


    String pngValidFilePath = "src/test/resources/test-images/png_valid.png";
    Path pngValidPath = Paths.get(pngValidFilePath);
    String pngValidName = "png_valid";
    String pngValidOriginalFileName = "png_valid.png";
    String pngValidContentType = "image/png";
    byte[] pngValidContent;
    {
        try {
            pngValidContent = Files.readAllBytes(pngValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultipartFile mockPngValid = new MockMultipartFile(pngValidName,
            pngValidOriginalFileName,
            pngValidContentType,
            pngValidContent);

static String svgTooBigFilePath = "src/test/resources/test-images/svg_too_big.svg";
    static Path svgTooBigPath = Paths.get(svgTooBigFilePath);
    static String svgTooBigName = "svg_too_big";
    static String svgTooBigOriginalFileName = "svg_too_big.svg";
    static String svgTooBigContentType = "image/svg+xml";
    static byte[] svgTooBigContent;
    static {
        try {
            svgTooBigContent = Files.readAllBytes(svgTooBigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static MultipartFile mockSvgTooBig = new MockMultipartFile(svgTooBigName,
            svgTooBigOriginalFileName,
            svgTooBigContentType,
            svgTooBigContent);

    @BeforeEach
    void setup() throws JsonProcessingException {
        // Save user
        hostUser = userRepository.save(new User("host@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));
        audienceUser = userRepository.save(new User("audiance@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Create livestream
        livestream = livestreamRepository.save(new Livestream(hostUser, "title", "description", "fake/file/path"));
        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(hostUser.getEmail(), null, Collections.emptyList()));

        // Create MVC
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(liveStreamController)
                .build();
    }

    /**
     * Cleanup user after each test to prevent duplicate users and posts causing errors
     */
    @AfterEach
    void cleanup() {
        // Clear DB
        livestreamRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void browseLiveStream_CorrectLiveStreams() throws Exception {
        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        // Check correct livestream returned
        Assertions.assertEquals(livestream.getId(), livestreamList.get(0).getId());
        Assertions.assertEquals(livestream.getTitle(), livestreamList.get(0).getTitle());
        Assertions.assertEquals(livestream.getOwner().getId(), livestreamList.get(0).getOwner().getId());
    }

    @Test
    void browseLiveStream_NoLiveStreams() throws Exception {
        // Remove current livestream
        livestreamRepository.deleteById(livestream.getId());

        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Get the livestreams
        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        // Check correct livestream returned
        Assertions.assertEquals(0, livestreamList.size());
    }

    @Test
    void createLiveStream_NoErrors_LiveStreamPersisted() throws Exception {
        // Remove existing livestream
        livestreamRepository.deleteById(livestream.getId());

        // Create livestream DTO
        LiveStreamDTO newLiveStream = new LiveStreamDTO("new title", "new description");

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", newLiveStream))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("browse/" + livestreamRepository.findLivestreamByTitle(newLiveStream.getTitle()).get(0).getId()));
    }

    @Test
    void viewLiveStream_IsOwner_IsHost() throws Exception {
        // Make request
        mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse/" + livestream.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isHost", true));
    }

    @Test
    void viewLiveStream_IsNotOwner_IsNotHost() throws Exception {
        // Set current audience as user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(audienceUser.getEmail(), null, Collections.emptyList()));

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse/" + livestream.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isHost", false));
    }

    @Test
    void viewLiveStream_IsNull_Return404() throws Exception {
        // Make request
        mockMvc.perform(MockMvcRequestBuilders.get("/livestream/browse/" + (livestream.getId() + 1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(view().name("error"));
    }

    @Test
    void createLiveStream_titleAndDescriptionEmpty_ErrorsShown() throws Exception {
        String expectedTitleError = "Title must be 256 characters or less and contain some text";
        String expectedDescriptionError = "Description must be 512 characters or less and contain some text";

        LiveStreamDTO newLiveStream = new LiveStreamDTO("", "");

        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", newLiveStream))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT
        String providedTitleMessage = bindingResult.getFieldErrors("title").getFirst().getDefaultMessage();
        String providedDescriptionMessage = bindingResult.getFieldErrors("description").getFirst().getDefaultMessage();

        Assertions.assertEquals(expectedTitleError, providedTitleMessage);
        Assertions.assertEquals(expectedDescriptionError, providedDescriptionMessage);
    }

    @Test
    void createLiveStream_titleAndDescriptionTooLong_ErrorsShown() throws Exception {
        String expectedTitleError = "Title must be 256 characters or less and contain some text";
        String expectedDescriptionError = "Description must be 512 characters or less and contain some text";

        LiveStreamDTO newLiveStream = new LiveStreamDTO("a".repeat(257), "a".repeat(513));

        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", newLiveStream))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT
        String providedTitleMessage = bindingResult.getFieldErrors("title").getFirst().getDefaultMessage();
        String providedDescriptionMessage = bindingResult.getFieldErrors("description").getFirst().getDefaultMessage();

        Assertions.assertEquals(expectedTitleError, providedTitleMessage);
        Assertions.assertEquals(expectedDescriptionError, providedDescriptionMessage);
    }

    @Test
    void createLiveStream_titleAndDescriptionInvalidChars_ErrorsShown() throws Exception {
        String expectedTitleError = "Title must only include alphanumeric characters, spaces, emojis and #, ', \", :, !, ,, ., $, ?, -";
        String expectedDescriptionError = "Description must only include alphanumeric characters, spaces, emojis and #, ', \", :, !, ,, ., $, ?, -";

        LiveStreamDTO newLiveStream = new LiveStreamDTO("[]", "[]");

        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", newLiveStream))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT
        String providedTitleMessage = bindingResult.getFieldErrors("title").getFirst().getDefaultMessage();
        String providedDescriptionMessage = bindingResult.getFieldErrors("description").getFirst().getDefaultMessage();

        Assertions.assertEquals(expectedTitleError, providedTitleMessage);
        Assertions.assertEquals(expectedDescriptionError, providedDescriptionMessage);
    }

    @Test
    void savedLiveStreamThumbnail_hasValidThumbnail_returnsTrue() throws Exception {
        livestreamRepository.deleteById(livestream.getId());


        liveStreamDTO = new LiveStreamDTO( "liveStreamWithThumbnail", "description");
        liveStreamDTO.setImage(mockPngValid);

        mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", liveStreamDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("browse/" + livestreamRepository.findLivestreamByTitle(liveStreamDTO.getTitle()).get(0).getId()));

        Livestream savedLivedStream = livestreamRepository.findLivestreamByTitle(liveStreamDTO.getTitle()).get(0);
        String liveStreamThumbnailPath = savedLivedStream.getImagePath();

        Assertions.assertNotNull(liveStreamThumbnailPath);

    }

    @Test
    void savedLiveStreamThumbnail_noThumbnail_returnsValidLiveStream() throws Exception {
        livestreamRepository.deleteById(livestream.getId());

        liveStreamDTO = new LiveStreamDTO( "liveStreamNoThumbnail", "description");

        mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", liveStreamDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("browse/" + livestreamRepository.findLivestreamByTitle(liveStreamDTO.getTitle()).get(0).getId()));

        String liveStreamThumbnailPath = livestreamRepository.findLivestreamByTitle(liveStreamDTO.getTitle()).get(0).getImagePath();
        Assertions.assertNull(liveStreamThumbnailPath);
    }

    @Test
    void savedLiveStreamThumbnail_thumbnailTooBig_returnsImageSizeException() throws Exception {
        String expectedMessage = "Image must be smaller than 10MB";

        liveStreamDTO = new LiveStreamDTO( "liveStreamLargeThumbnail", "description");
        liveStreamDTO.setImage(mockSvgTooBig);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", liveStreamDTO))
                .andExpect(status().isOk())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT

        String imageError = bindingResult.getFieldErrors("image").getFirst().getDefaultMessage();
        Assertions.assertEquals(expectedMessage, imageError);
    }

    @Test
    void savedLiveStreamThumbnail_contentTypeIsInvalid_returnsImageContentException() throws Exception {
        String expectedMessage = "Image must be of type png, jpg, jpeg or svg";

        liveStreamDTO = new LiveStreamDTO( "liveStreamLargeThumbnail", "description");

        MultipartFile mockSvgInvalidContent = new MockMultipartFile(
                "invalidContentType",
                "invalidContentType.pdf",
                "pdf",
                pngValidContent);
        liveStreamDTO.setImage(mockSvgInvalidContent);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", liveStreamDTO))
                .andExpect(status().isOk())
                .andReturn();

        BindingResult bindingResult = (BindingResult) result
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT

        String imageError = bindingResult.getFieldErrors("image").getFirst().getDefaultMessage();
        Assertions.assertEquals(expectedMessage, imageError);
    }

    @Test
    void createLiveStream_userLiveStreamAlreadyExists_ErrorWithLinkShown() throws Exception {
        LiveStreamDTO newLiveStream = new LiveStreamDTO("valid", "valid");

        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/livestream/create/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .flashAttr("livestreamDTO", newLiveStream))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result
                .getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.livestreamDTO"); // This line from chatGPT
        String existingLiveStreamId = bindingResult.getGlobalErrors().getFirst().getDefaultMessage();
        //Check that the error links to the correct stream
        Assertions.assertEquals(livestream.getId().toString(), existingLiveStreamId);
    }

    @Test
    void viewLiveStreams_lessThan11Exist() throws Exception {
        for (int i = 0; i < 9; i++) { // 1 Already exists in the before all
            livestreamRepository.save(new Livestream(hostUser, "Livestream" + i, "" + i, "fake/path"));
        }

        MvcResult result = mockMvc.perform(get("/livestream/browse"))

                .andExpectAll(
                        status().is(200),
                        view().name("livestream/browse")
                ).andReturn();

        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        Assertions.assertEquals(10, livestreamList.size());
        Assertions.assertEquals(1, browseLiveStreamsDTO.getTotalPages());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getNumberOfLivestreams());
        Assertions.assertEquals(1, browseLiveStreamsDTO.getParsedPage());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getPageSize());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "a", "!", "1", "2"})
    void viewLiveStreams_lessThan11Exist_specifyPage1(String page) throws Exception {
        for (int i = 0; i < 9; i++) { // 1 Already exists in the before all
            livestreamRepository.save(new Livestream(hostUser, "Livestream" + i, "" + i, "fake/path"));
        }

        MvcResult result = mockMvc.perform(get("/livestream/browse")
                        .param("page", page))

                .andExpectAll(
                        status().is(200),
                        view().name("livestream/browse")
                ).andReturn();

        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        Assertions.assertEquals(10, livestreamList.size());
        Assertions.assertEquals(1, browseLiveStreamsDTO.getTotalPages());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getNumberOfLivestreams());
        Assertions.assertEquals(1, browseLiveStreamsDTO.getParsedPage());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getPageSize());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "3"})
    void viewLiveStreams_twoPagesExist(String page) throws Exception {
        for (int i = 0; i < 15; i++) { // 1 Already exists in the before all
            livestreamRepository.save(new Livestream(hostUser, "Livestream" + i, "" + i, "fake/path"));
        }

        MvcResult result = mockMvc.perform(get("/livestream/browse")
                        .param("page", page))

                .andExpectAll(
                        status().is(200),
                        view().name("livestream/browse")
                ).andReturn();

        BrowseLiveStreamsDTO browseLiveStreamsDTO = (BrowseLiveStreamsDTO) result.getModelAndView().getModel().get("browseLiveStreamsDTO");

        // Get the livestreams
        List<Livestream> livestreamList  = browseLiveStreamsDTO.getLivestreams();

        Assertions.assertEquals(6, livestreamList.size());
        Assertions.assertEquals(2, browseLiveStreamsDTO.getTotalPages());
        Assertions.assertEquals(16, browseLiveStreamsDTO.getNumberOfLivestreams());
        Assertions.assertEquals(2, browseLiveStreamsDTO.getParsedPage());
        Assertions.assertEquals(10, browseLiveStreamsDTO.getPageSize());
    }
}
