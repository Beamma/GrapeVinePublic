package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.LikeController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for liking and unliking a post
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class LikeControllerTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    LikeController likeController;
    @Autowired
    PostRepository postRepository;
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    PlantRepository plantRepository;
    @Autowired
    CommentLikeRepository commentLikeRepository;
    @Autowired
    CommentRepository commentRepository;

    private MockMvc mockMvc;

    MvcResult result;
    ResultActions response;

    Comment comment;

    User user;
    Post post;

    /**
     * Set up all data in database before class is run
     */
    @BeforeEach
    void setup() {
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        plantRepository.deleteAll();
        gardenRepository.deleteAll();
        likeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();


        User saveUser = new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user = userRepository.save(saveUser);
        Post savePost = new Post("TestPost", "TestContent", user);
        post = postRepository.save(savePost);
        Comment saveComment = new Comment(post, user, "TestComment");
        comment = commentRepository.save(saveComment);

        var authentication = new UsernamePasswordAuthenticationToken("postTester@gmail.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc = MockMvcBuilders
                .standaloneSetup(likeController)
                .build();

    }

    /**
     * Delete all data from the database after class is run
     */
    @AfterEach
    void cleanup() {
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();

        likeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void addLikeQuery_ValidPost_SuccessResponseReturned() throws Exception {
        this.mockMvc.perform((put("/post/" + post.getId() + "/like")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void addLikeQuery_PostAlreadyLiked_LikeCountDoesNotIncrease() throws Exception {
        this.mockMvc.perform((put("/post/" + post.getId() + "/like")))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void unlikePostQuery_ValidPost_SuccessResponseReturned() throws Exception {
        this.mockMvc.perform((delete("/post/" + post.getId() + "/like")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value("User has not liked this post"));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void unlikePostQuery_PostNotLiked_LikeCountDoesNotDecrease() throws Exception {
        this.mockMvc.perform((delete("/post/" + post.getId() + "/like")))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value("User has not liked this post"));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void getLikeCountQuery_ValidPost_SuccessResponseReturned() throws Exception {
        Likes saveLike = new Likes(post, user);
        likeRepository.save(saveLike);
        this.mockMvc.perform(get("/post/" + post.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeExists").value(true));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void getLikeCountQueryMultipleLikes_ValidPost_SuccessResponseReturned() throws Exception {
        User saveUser1 = new User("postTester1@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        User user1 = userRepository.save(saveUser1);
        User saveUser2 = new User("postTester2@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        User user2 = userRepository.save(saveUser2);

        Likes saveLike = new Likes(post, user);
        Likes saveLike2 = new Likes(post, user1);
        Likes saveLike3 = new Likes(post, user2);
        likeRepository.save(saveLike2);
        likeRepository.save(saveLike3);
        likeRepository.save(saveLike);
        this.mockMvc.perform(get("/post/" + post.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeExists").value(true));
        likeRepository.deleteAll();
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void getLikeInformationHaventLike_ValidPost_SuccessResponseReturned() throws Exception {
        this.mockMvc.perform(get("/post/" + post.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeExists").value(false));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void addLikeToComment_ValidComment_SuccessResponseReturned() throws Exception {
        this.mockMvc.perform(put("/comment/" + comment.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void addLikeToComment_CommentAlreadyLiked_LikeCountDoesNotIncrease() throws Exception {
        // Like the comment
        LikeComment saveLike = new LikeComment(comment, user);
        commentLikeRepository.save(saveLike);
        // Like the comment again
        this.mockMvc.perform(put("/comment/" + comment.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(1));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void removeLikeFromComment_ValidComment_SuccessResponseReturned() throws Exception {
        // Like the comment
        LikeComment saveLike = new LikeComment(comment, user);
        commentLikeRepository.save(saveLike);
        // Unlike the comment, check that the like count decreases
        this.mockMvc.perform(delete("/comment/" + comment.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(0));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void removeLikeFromComment_CommentNotLiked_LikeCountDoesNotDecrease() throws Exception {
        // No like on the comment
        // Unlike the comment, check that the like count does not change
        this.mockMvc.perform(delete("/comment/" + comment.getId() + "/like"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value("User has not liked this comment"));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void getCommentLikeCount_ValidComment_SuccessResponseReturned() throws Exception {
        LikeComment saveLike = new LikeComment(comment, user);
        commentLikeRepository.save(saveLike);
        this.mockMvc.perform(get("/comment/" + comment.getId() + "/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeExists").value(true));
    }

    @WithMockUser(username="postTester@gmail.com")
    @Test
    void getCommentLikeCountMultipleLikes_ValidComment_SuccessResponseReturned() throws Exception {
        // create 2 new users
        User saveUser1 =
                new User("postTester123", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        User user1 = userRepository.save(saveUser1);
        User saveUser2 =
                new User("postTester1234", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        User user2 = userRepository.save(saveUser2);
        // create 3 likes for the comment
        LikeComment saveLike = new LikeComment(comment, user);
        LikeComment saveLike2 = new LikeComment(comment, user1);
        LikeComment saveLike3 = new LikeComment(comment, user2);
        commentLikeRepository.save(saveLike);
        commentLikeRepository.save(saveLike2);
        commentLikeRepository.save(saveLike3);
        this.mockMvc.perform(get("/comment/"+comment.getId()+"/like"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeExists").value(true));
        commentLikeRepository.deleteAll();
    }





}
