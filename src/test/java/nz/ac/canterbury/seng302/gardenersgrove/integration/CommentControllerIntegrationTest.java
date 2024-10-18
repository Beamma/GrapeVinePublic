package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.CommentController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CommentControllerIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentController commentController;
    @Autowired
    CommentRepository commentRepository;


    MockMvc mockMvc;

    User user;
    Post post;
    Comment comment;
    @MockBean
    ProfanityFilterService profanityFilterService;
    @Autowired
    private UserService userService;

    int page;

    /**
     * Create the required state in the database
     */
    @BeforeEach
    public void setUp() throws JsonProcessingException {

        // Create user
        user = userRepository.save(new User("user@example.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Create post
        post = postRepository.save(new Post("Post", "TestContent", user));
        comment = commentRepository.save(new Comment(post, user, "Test Post"));

        commentRepository.save(new Comment(post, user, "this is a comment"));
        //saves 17 comments, since pagination requires at least  10 comments to be tested
        for (int i = 0; i < 17; i++) {
            commentRepository.save(new Comment(post, user, "this is a comment"));
        }
        // Mock API calls for profanity
        Mockito.when(profanityFilterService.isTextProfane(Mockito.any())).thenReturn(false);
        Mockito.when(profanityFilterService.isTextProfane("Fuck")).thenReturn(true);

        // Auth user
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create mock MVC
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .build();
    }

    /**
     * Clears out the database after use.
     */
    @AfterEach
    @Transactional
    public void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void CreateValidComment_AddedToDB() throws Exception {
        // Create comment
        String text = "this is a comment";

        // Make request
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                    .content("{\"comment\": \"" + text + "\"}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // Get comment
        List<Comment> comments = commentRepository.findCommentsByPostId(post.getId());
        int createdCommentId = Integer.parseInt(result.getResponse().getContentAsString());
        boolean commentExists = comments.stream()
                .anyMatch(c -> c.getId() == createdCommentId);
        Assertions.assertTrue(commentExists, "Comment with the created ID should exist in the list");
        //fix up here
//        Comment comment = commentRepository.findPagedCommentsByPostId(post.getId(), pageable).getContent().get(0);
        Comment comment = commentRepository.findInitialPagedCommentsByPostId(post.getId()).get(0);

        // Check comment
        Assertions.assertEquals(post.getId(), comment.getPost().getId());
    }

    @Test
    void CreateValidComment_PostNotExist_ThrowError() throws Exception {
        // Create comment
        String text = "this is a comment";

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.put("/post/999/comment")
                        .content("{\"comment\": \"" + text + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().is4xxClientError(),
                                content().string("The post you are commenting on no longer exists"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "    "})
    void WhiteSpaceComment_ThrowError(String text) throws Exception {

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                        .content("{\"comment\": \"" + text + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().is4xxClientError(),
                        content().string("Comment must not be empty"));
    }

    @Test
    void CommentTooLong_ThrowError() throws Exception {

        String text = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                        .content("{\"comment\": \"" + text + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isBadRequest(),
                        content().string("Comments must be 512 characters or less"));
    }

    @Test
    void CommentProfane_ThrowError() throws Exception {

        String text = "Fuck";
        int profanityCount = user.getInappropriateWarningCount();

        // Make request
        mockMvc.perform(MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                        .content("{\"comment\": \"" + text + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isBadRequest(),
                        content().string("The comment does not match the language standards of the app."));
        Assertions.assertEquals(profanityCount + 1, userService.getCurrentUser().getInappropriateWarningCount());
    }

    @Test
    void CommentExists_InjectsCommentAndPost() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/post/" + post.getId() + "/comment/" + comment.getId()))
                .andReturn();
        Comment returnedComment = (Comment) Objects.requireNonNull(result.getModelAndView()).getModel().get("comment");
        Post returnedPost = (Post) Objects.requireNonNull(result.getModelAndView()).getModel().get("post");
        Assertions.assertEquals(returnedPost.getId(), post.getId());
        Assertions.assertEquals(returnedComment.getId(), comment.getId());
    }

    @Test
    void CommentDoesntExist_ReturnsNull() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get("/post/" + post.getId() + "/comment/" + 1000))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertNull(result.getModelAndView());
    }

    @Test
    void getComments_ValidIdPageOne_ReturnSuccess() throws Exception {
        page = 0;
        mockMvc.perform(MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                .param("page", String.valueOf(page)))
            .andExpectAll(status().isOk(),
                MockMvcResultMatchers.jsonPath("$.message").doesNotExist(),
                MockMvcResultMatchers.jsonPath("$.comments.length()").value(10),
                MockMvcResultMatchers.jsonPath("$.comments[0].text").value("this is a comment"));
    }

    @Test
    void getComments_ValidIdPageTwo_ReturnSmallerPage() throws Exception {
        page = 1;
        mockMvc.perform(MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                        .param("page", String.valueOf(page)))
                .andExpectAll(status().isOk(),
                    MockMvcResultMatchers.jsonPath("$.length()").value(1),
                    MockMvcResultMatchers.jsonPath("$.comments.length()").value(6),
                    MockMvcResultMatchers.jsonPath("$.message").doesNotExist(),
                    MockMvcResultMatchers.jsonPath("$.comments[0].text").value("this is a comment"));
    }

    @Test
    void getCommentsExcludingCommentId_ReturnsWithoutComment () throws Exception {
        page = 0;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                .param("page", String.valueOf(page)))
                .andReturn();

        //Get list of comment ids returned by page 0 query, Copilot helped with Json parsing
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode comments = objectMapper.readTree(result.getResponse().getContentAsString()).path("comments");
        Assertions.assertFalse(comments.isEmpty());

        List<Long> commentIds = new ArrayList<>();
        for (JsonNode comment : comments) {
            commentIds.add(comment.path("id").asLong());
        }

        //Call endpoint again, this time with valid excluded id
        Long excludedId = commentIds.get(0);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                        .header("excludedIdsHeader", excludedId.toString())
                        .param("page", String.valueOf(page)))
                .andReturn();

        comments = objectMapper.readTree(result.getResponse().getContentAsString()).path("comments");
        commentIds = new ArrayList<>();
        for (JsonNode comment : comments) {
            commentIds.add(comment.path("id").asLong());
        }
        //Check that excluded id is not present in second query for comments
        Assertions.assertTrue(commentIds.stream().noneMatch(id -> id.equals(excludedId)));

    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 100})
    void getComments_ValidIdPageOne_ReturnEmpty(int pageNumber) throws Exception {
        var message = "No comments found";
        mockMvc.perform(MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                        .param("page", String.valueOf(pageNumber)))
                .andExpectAll(status().isOk(),
                    MockMvcResultMatchers.jsonPath("$.length()").value(1),
                    MockMvcResultMatchers.jsonPath("$.comments").doesNotExist(),
                        MockMvcResultMatchers.jsonPath("$.message").exists(),
                        MockMvcResultMatchers.jsonPath("$.message").value(message));
    }

    @Test
    void getComments_InvalidPostIdPageOne_ReturnEmpty() throws Exception {
        page = 0;
        var message = "No comments found";

        Post noCommentsPost = postRepository.save(new Post("Post", "nocompost", user));
        noCommentsPost.setId(2L);

        mockMvc.perform(MockMvcRequestBuilders.get("/post/" + noCommentsPost.getId() + "/comments")
                        .param("page", String.valueOf(page)))
                .andExpectAll(status().isOk(),
                    MockMvcResultMatchers.jsonPath("$.comments").doesNotExist(),
                    MockMvcResultMatchers.jsonPath("$.message").value(message));
    }

    @Test
    void getComments_ValidIdLargePage_ReturnEmpty() throws Exception {
        page = 99;
        var message = "No comments found";
        mockMvc.perform(MockMvcRequestBuilders.get("/post/" + post.getId() + "/comments")
                        .param("page", String.valueOf(page)))
                .andExpectAll(status().isOk(),
                    MockMvcResultMatchers.jsonPath("$.comments").doesNotExist(),
                    MockMvcResultMatchers.jsonPath("$.message").value(message));
    }

    @Test
    void UserHasFiveWarnings_Blocked() throws Exception {
        user.setInappropriateWarningCount(5);
        userRepository.save(user);

        String text = "Fuck";

        // Make request
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/post/" + post.getId() + "/comment")
                        .content("{\"comment\": \"" + text + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Assertions.assertEquals(302, mvcResult.getResponse().getStatus());
        Assertions.assertEquals("application/json", mvcResult.getResponse().getContentType());

        // Parse response body
        String responseBody = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>(){});

        Assertions.assertEquals(true, responseMap.get("redirected"));
        Assertions.assertEquals("../auth/logout-banned", responseMap.get("url"));
    }
}
