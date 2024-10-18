package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentLikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.LikeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

/**
 * Unit tests for testing the LikeService Class
 */
class LikeServiceTest {
    LikeService likeService;
    Likes likes;

    LikeComment likeComment;
    Post post1;
    Post post2;
    User user1;
    User user2;
    Comment comment;
    Comment comment2;
    LikeRepository mockLikeRepository;


    CommentLikeRepository mockCommentLikeRepository;


    /**
     * Set up and save the mocks before class is run
     */
    @BeforeEach
    public void setUp() {
        user1 = new User("Tester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1.setId(1L);

        post1 = new Post("TestPost", "TestContent", user1);
        post1.setId(1L);

        likes = new Likes(post1, user1);

        user2 = new User("Tester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user2.setId(2L);

        post2 = new Post("NoLikesPost", "TestContent", user1);
        post2.setId(2L);



        comment = new Comment(post1, user1, "TestComment");
        comment.setId(1L);

        likeComment = new LikeComment(comment, user1);

        comment2 = new Comment(post1, user1, "TestComment2");
        comment2.setId(2L);


        List<Likes> likeList = Collections.singletonList(likes);

        mockLikeRepository = Mockito.mock(LikeRepository.class);
        Mockito.when(mockLikeRepository.getByPostAndUser(1L, 1L)).thenReturn(Optional.ofNullable(likes));
        Mockito.when(mockLikeRepository.getByPostAndUser(2L, 2L)).thenReturn(Optional.empty());
        Mockito.when(mockLikeRepository.getByPostId(1L)).thenReturn(Optional.of(likeList));
        Mockito.when(mockLikeRepository.getByPostId(3L)).thenReturn(Optional.empty());
        Mockito.when(mockLikeRepository.save(Mockito.any(Likes.class))).thenReturn(likes);

        mockCommentLikeRepository = Mockito.mock(CommentLikeRepository.class);
        Mockito.when(mockCommentLikeRepository.save(Mockito.any(LikeComment.class))).thenReturn(likeComment);
        Mockito.when(mockCommentLikeRepository.getByUserAndComment(1L, 1L)).thenReturn(Optional.ofNullable(likeComment));
        Mockito.when(mockCommentLikeRepository.getByUserAndComment(1L, 2L)).thenReturn(Optional.empty());
        Mockito.when(mockCommentLikeRepository.getByCommentId(1L)).thenReturn(Optional.of(Collections.singletonList(likeComment)));


        likeService = new LikeService(mockLikeRepository, mockCommentLikeRepository, Mockito.mock(CommentRepository.class));
    }

    @Test
    void GivenLikeExists_ReturnsTrue() {
        boolean likeExists = likeService.likeExists(post1, user1);
        Assertions.assertTrue(likeExists);
    }

    @Test
    void GivenLikeDoesNotExist_ReturnsFalse() {
        boolean likeExists = likeService.likeExists(post2, user2);
        Assertions.assertFalse(likeExists);
    }

    @Test
    void GivenPostHasOneLike_ReturnsCorrectLikeCount() {
        //works since we have saved a like object for 'user'
        int likeCount = likeService.getLikeCountByPostId(1L);
        Assertions.assertEquals(1, likeCount);
    }

    @Test
    void GivenPostHasNoLikes_ReturnsZero() {
        //post2 exists but has no likes
        int likeCount = likeService.getLikeCountByPostId(2L);
        Assertions.assertEquals(0, likeCount);
    }

    @Test
    void GivenLikeDTOHasPostLike_AddLike_CallsCorrectRepository() {
        likeService.addLike(post1, user1);
        Mockito.verify(mockLikeRepository).save(Mockito.any(Likes.class));
    }

    @Test
    void GivenLikeDTOHasPostLike_RemoveLike_CallsCorrectRepository() {
        likeService.removeLike(post1, user1);
        Mockito.verify(mockLikeRepository).removeByUserAndPost(1L, 1L);

    }

    @Test
    void GivenCommentLikeExists_ReturnsTrue() {
        boolean likeExists = likeService.commentLikeExists(user1, comment);
        Assertions.assertTrue(likeExists);
    }

    @Test
    void GivenCommentLikeDoesNotExist_ReturnsFalse() {
        boolean likeExists = likeService.commentLikeExists(user1, comment2);
        Assertions.assertFalse(likeExists);
    }

    @Test
    void GivenCommentHasOneLike_ReturnsCorrectLikeCount() {
        int likeCount = likeService.getCommentLikeCountByCommentId(comment.getId());
        Assertions.assertEquals(1, likeCount);
    }

    @Test
    void GivenCommentHasNoLikes_ReturnsZero() {
        int likeCount = likeService.getCommentLikeCountByCommentId(comment2.getId());
        Assertions.assertEquals(0, likeCount);
    }

    @Test
    void GivenLikeDTOHasCommentLike_AddCommentLike_CallsCorrectRepository() {
        likeService.addCommentLike(user1, comment);
        Mockito.verify(mockCommentLikeRepository).save(Mockito.any(LikeComment.class));
    }

    @Test
    void GivenLikeDTOHasCommentLike_RemoveCommentLike_CallsCorrectRepository() {
        likeService.removeCommentLike(user1, comment);
        Mockito.verify(mockCommentLikeRepository).removeByUserAndComment(1L, 1L);
    }

}
