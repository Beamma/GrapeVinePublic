package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PostController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseFeedDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private ProfanityFilterService profanityFilterService;

    @Autowired
    PostController postController;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private GardenRepository gardenRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    private User user;

    private static final String TEST_IMG_DIR = "src/test/resources/test-images/";
    private static final String IMG_SAVE_DIR = "src/main/resources/public/";

    // Test arguments
    static Stream<Arguments> invalidImageData() {
        return Stream.of(
                Arguments.of("gif_valid.gif", "Image must be of type png, jpg or svg"),
                Arguments.of("jpg_too_big.jpg", "Image must be smaller than 10MB"),
                Arguments.of("png_too_big.png", "Image must be smaller than 10MB"),
                Arguments.of("svg_too_big.svg", "Image must be smaller than 10MB")
                );
    }

    static Stream<Arguments> validImageData() {
        return Stream.of(
                Arguments.of("jpg_valid.jpg"),
                Arguments.of("png_valid.png"),
                Arguments.of("svg_valid.svg")
                );
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setup() throws JsonProcessingException {
        // Save user
        user = userRepository.save(new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList()));

        // Mock the profanity service
        Mockito.when(profanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));

        // Create MVC
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .build();
    }

    /**
     * Cleanup user after each test to prevent duplicate users and posts causing errors
     */
    @AfterEach
    void cleanup() {
        commentRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
        gardenRepository.deleteAll();
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

    @Test
    void addPostQueried_addPostFormReturned() throws Exception {
        userRepository.findByEmail(user.getEmail());
        this.mockMvc.perform((get("/post/add")))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/addPostForm"));
    }

    @Test
    void addPostQueried_queryContainsAutofillContent_filledPostFormReturned() throws Exception {
        PostDTO postDTO = new PostDTO("test title", "test content");
        MvcResult result = this.mockMvc.perform((get("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        PostDTO returnedPostDTO = (PostDTO) result.getModelAndView().getModel().get("postDTO");
        Assertions.assertEquals("test title", returnedPostDTO.getTitle());
        Assertions.assertEquals("test content", returnedPostDTO.getContent());

    }

    @Test
    void addPostWithTooLongTitle_PostNotAdded_errorMessageCorrect() throws Exception {
        PostDTO postDTO = new PostDTO("a".repeat(65), "test content");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("Post title must be 64 characters long or less", bindingResult.getFieldError("title").getDefaultMessage());
        Assertions.assertTrue(postRepository.findPostByTitle("a".repeat(65)).isEmpty());
    }

    @Test
    void addPostWithEmptyTitle_PostIsAdded() throws Exception {
        PostDTO postDTO = new PostDTO("", "test content");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(view().name("redirect:/feed/browse"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertNull(bindingResult);

        Assertions.assertEquals("test content",postRepository.findPostByTitle("").get(0).getContent());
    }

    @Test
    void addPostWithEmptyContent_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title", "");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("Post content must not be empty", bindingResult.getFieldError("content").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("test title").isEmpty());
    }

    @Test
    void addPostWithTooLongContent_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title", "a".repeat(513));
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("Post content must be 512 characters long or less", bindingResult.getFieldError("content").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("test title").isEmpty());
    }

    @Test
    void addPostWithInvalidTitle_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title!", "test content");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("Post title must only include letters, numbers, spaces, dots, hyphens, or apostrophes", bindingResult.getFieldError("title").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("test title!").isEmpty());
    }

    @Test
    void addPostWithInvalidContent_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title", "test content!");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("Post content must only include letters, numbers, spaces, dots, hyphens, or apostrophes", bindingResult.getFieldError("content").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("test title").isEmpty());
    }

    @Test
    void addPostWithInvalidTitleAndContent_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title!", "test content!");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("Post title must only include letters, numbers, spaces, dots, hyphens, or apostrophes", bindingResult.getFieldError("title").getDefaultMessage());
        Assertions.assertEquals("Post content must only include letters, numbers, spaces, dots, hyphens, or apostrophes", bindingResult.getFieldError("content").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("test title!").isEmpty());
    }

    @Test
    void addPostWithValidTitleAndContent_PostAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title", "test content");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(view().name("redirect:/feed/browse"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertNull(bindingResult);
        Assertions.assertEquals("test content",postRepository.findPostByTitle("test title").get(0).getContent());
    }

    @Test
    void addPostWithProfaneTitle_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("badWord", "test content");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("The title does not match the language standards of the app.", bindingResult.getFieldError("title").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("badWord").isEmpty());

        Assertions.assertEquals(1, userRepository.findById(user.getId()).get().getInappropriateWarningCount());
    }

    @Test
    void addPostWithProfaneContent_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("test title", "badWord");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("The content does not match the language standards of the app.", bindingResult.getFieldError("content").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("test title").isEmpty());

        Assertions.assertEquals(1, userRepository.findById(user.getId()).get().getInappropriateWarningCount());
    }

    @Test
    void addPostWithProfaneTitleAndContent_PostNotAdded() throws Exception {
        PostDTO postDTO = new PostDTO("badWord", "badWord");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "title", "401"))
                .andExpect(model().attributeHasFieldErrorCode("postDTO", "content", "401"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertEquals("The title does not match the language standards of the app.", bindingResult.getFieldError("title").getDefaultMessage());
        Assertions.assertEquals("The content does not match the language standards of the app.", bindingResult.getFieldError("content").getDefaultMessage());

        Assertions.assertTrue(postRepository.findPostByTitle("badWord").isEmpty());

        Assertions.assertEquals(1, userRepository.findById(user.getId()).get().getInappropriateWarningCount());
    }

    @Test
    void addFifthPostWithProfaneTitleAndContent_PostNotAdded_WarningPopupAppears() throws Exception {
        user.setInappropriateWarningCount(4);
        userRepository.save(user);
        PostDTO postDTO = new PostDTO("badWord", "badWord");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(model().attribute("fifthInappropriateSubmission", "true"))
                .andExpect(view().name("posts/addPostForm"))
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        Assertions.assertTrue(bindingResult.hasErrors());
        Assertions.assertTrue(postRepository.findPostByTitle("badWord").isEmpty());
        Assertions.assertEquals(5, userRepository.findById(user.getId()).get().getInappropriateWarningCount());
    }

    @Test
    void addSixthPostWithProfaneTitleAndContent_PostNotAdded_UserBanned() throws Exception {
        user.setInappropriateWarningCount(5);
        userRepository.save(user);
        PostDTO postDTO = new PostDTO("badWord", "badWord");
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO)))
                .andExpect(view().name("redirect:/auth/login"))
                .andExpect(flash().attribute("blocked", "true"))
                .andReturn();

        Assertions.assertTrue(postRepository.findPostByTitle("badWord").isEmpty());
        Assertions.assertEquals(6, userRepository.findById(user.getId()).get().getInappropriateWarningCount());
        Assertions.assertNotNull(userRepository.findById(user.getId()).get().getBlockedEndDate());
    }

    @Test
    void DeleteUserPost_Allowed() throws Exception {
        // Create post
        Post post = postRepository.save(new Post("Post", "TestContent", user));

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + post.getId()))
                .andExpectAll(status().isOk());

        // Check deleted
        Assertions.assertNull(postRepository.findById(post.getId()).orElse(null));
    }

    @Test
    void DeleteOtherUserPost_ThrowError() throws Exception {
        // Create other user post
        User tempUser = userRepository.save(new User("tempuser@example.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));
        Post post = postRepository.save(new Post("Post", "TestContent", tempUser));

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isForbidden());

        // Check not deleted
        Assertions.assertNotNull(postRepository.findById(post.getId()));
    }

    @Test
    void DeletePost_NoPost_ThrowError() throws Exception {
        // Create post
        Post post = postRepository.save(new Post("Post", "TestContent", user));

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + (post.getId() + 1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound());

        // Check not deleted
        Assertions.assertNotNull(postRepository.findById(post.getId()));
    }

    @Test
    void DeleteUserPost_WithComments_PostAndCommentsDeleted() throws Exception {
        // Create post, like and comments
        Post post = postRepository.save(new Post("Post", "TestContent", user));
        Likes like = likeRepository.save(new Likes(post, user));
        Comment comment1 = commentRepository.save(new Comment(post, user, "Comment1"));
        Comment comment2 = commentRepository.save(new Comment(post, user, "Comment2"));
        Comment comment3 = commentRepository.save(new Comment(post, user, "Comment3"));
        LikeComment likeComment = commentLikeRepository.save(new LikeComment(comment1, user));

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.delete("/post/" + post.getId()))
                .andExpectAll(status().isOk());

        // Check deleted
        Assertions.assertNull(postRepository.findById(post.getId()).orElse(null));
        Assertions.assertNull(likeRepository.findById(like.getId()).orElse(null));
        Assertions.assertNull(commentRepository.findById(comment1.getId()).orElse(null));
        Assertions.assertNull(commentRepository.findById(comment2.getId()).orElse(null));
        Assertions.assertNull(commentRepository.findById(comment3.getId()).orElse(null));
        Assertions.assertNull(commentLikeRepository.getByUserAndComment(user.getId(), comment1.getId()).orElse(null));
    }

    @ParameterizedTest
    @MethodSource("invalidImageData")
    void AddPost_WithImages(String filename, String expectedMessage) throws Exception {
        // Create post DTO
        PostDTO postDTO = new PostDTO("PostTitle", "Description");

        // Get file
        postDTO.setImage(readFile(filename));

        // Send request
        MvcResult result = this.mockMvc.perform(post("/post/add").flashAttr("postDTO", postDTO))
                .andReturn();

        // Invalid Checks
        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.postDTO");

        // Check has errors
        Assertions.assertTrue(bindingResult.hasErrors());

        // Check correct error message
        Assertions.assertEquals(expectedMessage, bindingResult.getFieldError("image").getDefaultMessage());

        // Check not added to DB
        Assertions.assertEquals(0, postRepository.findPostByTitle(postDTO.getTitle()).size());
    }

    @ParameterizedTest
    @MethodSource("validImageData")
    void AddPost_ValidImage_NoErrors(String filename) throws Exception {
        // Create post DTO
        PostDTO postDTO = new PostDTO("PostTitle", "Description");

        // Get file
        postDTO.setImage(readFile(filename));

        // Send request
        MvcResult result = this.mockMvc.perform(post("/post/add").flashAttr("postDTO", postDTO))
                .andReturn();

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

    @Test
    void UserHasFiveInappropriateWarnings_UserSubmitsProfanity_UserIsBlockedAndRedirected() throws Exception {
        // Create post DTO
        PostDTO postDTO = new PostDTO("badWord", "test content");

        // User has 5 inappropriate warnings
        user.setInappropriateWarningCount(5);
        userRepository.save(user);

        // Send request
        MvcResult result = this.mockMvc.perform((post("/post/add")
                .flashAttr("postDTO", postDTO))).andReturn();

        // Check redirected from ResponseEntity
        Assertions.assertTrue(result.getResponse().getRedirectedUrl().contains("/auth/login"));
        Assertions.assertTrue(userRepository.findById(user.getId()).get().isBlocked());

    }

    @Test
    void GetPostWithLinkedGarden_ReturnsGardenIdAndName() throws Exception {
        AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);
        Garden garden = new Garden("My garden", addressDTO, user);
        garden = gardenRepository.save(garden);

        PostDTO postDTO = new PostDTO("", "test content");
        postDTO.setGarden(garden);

        Post post = new Post(postDTO, null, user);
        post = postRepository.save(post);

        MvcResult result = this.mockMvc.perform((get("/feed/browse")
                     ))
                .andExpect(view().name("posts/feedPage"))
                .andReturn();

        BrowseFeedDTO browseFeedDTO = (BrowseFeedDTO) result.getModelAndView().getModel().get("browseFeedDTO");

        Assertions.assertEquals(garden.getGardenId(), browseFeedDTO.getPosts().get(0).getLinkedGarden().getGardenId());
        Assertions.assertEquals("My garden", browseFeedDTO.getPosts().get(0).getLinkedGarden().getName());
    }
    @Test
    void GetPostWithoutLinkedGarden_ReturnsNoGardenIdOrName() throws Exception {
        PostDTO postDTO = new PostDTO("", "test content");

        Post post = new Post(postDTO, null, user);
        post = postRepository.save(post);

        MvcResult result = this.mockMvc.perform((get("/feed/browse")
                     ))
                .andExpect(view().name("posts/feedPage"))
                .andReturn();

        BrowseFeedDTO browseFeedDTO = (BrowseFeedDTO) result.getModelAndView().getModel().get("browseFeedDTO");

        Assertions.assertNull(browseFeedDTO.getPosts().get(0).getLinkedGarden());
    }


}