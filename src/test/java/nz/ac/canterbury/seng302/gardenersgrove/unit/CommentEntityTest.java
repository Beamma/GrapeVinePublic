package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Tests for the Comment Entity
 */
class CommentEntityTest {
    Post post;
    Comment comment;
    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        post = new Post("TestPost", "TestContent", user);
        comment = new Comment(post, user, "Test Comment");
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

}

