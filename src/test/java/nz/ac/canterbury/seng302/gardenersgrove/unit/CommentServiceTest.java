package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.CommentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class CommentServiceTest {

    CommentService commentService;
    Post post;
    Post noCommentsPost;
    Comment comment;
    User user;
    CommentRepository mockCommentRepository;

    int INITIAL_PAGE_SIZE = 13;
    private static final int COMMENT_PAGE_SIZE = 10;


    @BeforeEach
    void setUp() {
        mockCommentRepository = Mockito.mock(CommentRepository.class);
        commentService = new CommentService(mockCommentRepository);
        user = new User();
        post = new Post("TestPost", "TestContent", user);
        post.setId(1L);

        noCommentsPost = new Post("TestPostWithNoEngagement", "BadContent", user);
        noCommentsPost.setId(2L);
        comment = new Comment(post, user, "Test Comment");

        List<Comment> initialCommentList = new ArrayList<>();
        for (int i = 0; i < INITIAL_PAGE_SIZE; i++) {
            initialCommentList.add(new Comment(post, user, "a comment"));
        }

        List<Comment> shortCommentList = new ArrayList<>();
        for (int k = 0; k < 5; k++) {
            shortCommentList.add(new Comment(post, user, "a comment"));
        }
        List<Comment> emptyCommentList = new ArrayList<>();

        int pageTwoOffset = COMMENT_PAGE_SIZE + 13 ;
        Mockito.when(mockCommentRepository.findInitialPagedCommentsByPostId(post.getId())).thenReturn(initialCommentList);
        Mockito.when(mockCommentRepository.findPagedCommentsByPostId(post.getId(), pageTwoOffset)).thenReturn(shortCommentList);
        Mockito.when(mockCommentRepository.findInitialPagedCommentsByPostId(noCommentsPost.getId())).thenReturn(emptyCommentList);


    }

    @Test
    void testGetTimeSinceCommented_LessThanAMinute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusSeconds(2);
        comment.setDate(thirtyMinutesAgo);

        Assertions.assertEquals("Less Than A Minute", comment.getTimeSincePosted());
    }

    @Test
    void testGetTimeSinceCommented_LessThanAnHour() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);
        comment.setDate(thirtyMinutesAgo);

        Assertions.assertEquals("30 Minutes", comment.getTimeSincePosted());
    }

    @Test
    void testGetTimeSinceCommented_MoreThanAnHour_LessThanADay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeHoursAgo = now.minusHours(3);
        comment.setDate(threeHoursAgo);

        Assertions.assertEquals("3 Hours", comment.getTimeSincePosted());
    }

    @Test
    void testGetTimeSinceCommented_MoreThanADay_LessThanAWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        comment.setDate(threeDaysAgo);

        Assertions.assertEquals("3 Days", comment.getTimeSincePosted());
    }

    @Test
    void testGetTimeSinceCommented_MoreThanAWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);
        comment.setDate(tenDaysAgo);

        Assertions.assertEquals("1 Week", comment.getTimeSincePosted());
    }

    @Test
    void testGetTimeSinceCommented_MoreThanTwoWeeks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenDaysAgo = now.minusDays(15);
        comment.setDate(fifteenDaysAgo);

        Assertions.assertEquals("2 Weeks", comment.getTimeSincePosted());
    }

    @Test
    void GetComments_ValidIdPageZero_InitialSizeReturned() {
        List<Comment> comments = commentService.getInitialCommentsByPostId(post.getId());

        Assertions.assertFalse(comments.isEmpty());
        Assertions.assertEquals(INITIAL_PAGE_SIZE, comments.size());
    }

    @Test
    void GetComments_ValidIdPageTwo_ShortSizeReturned() {
        List<Comment> comments = commentService.getCommentsByPostId(post.getId(), 2);

        Assertions.assertEquals(5, comments.size());
    }

    @Test
    void GetComments_InvalidId_NoneReturned() {
        List<Comment> comments = commentService.getInitialCommentsByPostId(noCommentsPost.getId());

        Assertions.assertTrue(comments.isEmpty());
    }
}
