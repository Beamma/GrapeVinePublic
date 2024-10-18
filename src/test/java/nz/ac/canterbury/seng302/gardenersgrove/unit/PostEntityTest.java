package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class PostEntityTest {

    Post post;
    @BeforeEach
    void setUp() {
        post = new Post("TestPost", "TestContent", new User());
    }

    @Test
    void testGetTimeSincePosted_LessThanAMinute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusSeconds(2);
        post.setDate(thirtyMinutesAgo);

        Assertions.assertEquals("Less Than A Minute", post.getTimeSincePosted());
    }

    @Test
    void testGetTimeSincePosted_LessThanAnHour() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);
        post.setDate(thirtyMinutesAgo);

        Assertions.assertEquals("30 Minutes", post.getTimeSincePosted());
    }

    @Test
    void testGetTimeSincePosted_MoreThanAnHour_LessThanADay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeHoursAgo = now.minusHours(3);
        post.setDate(threeHoursAgo);

        Assertions.assertEquals("3 Hours", post.getTimeSincePosted());
    }

    @Test
    void testGetTimeSincePosted_MoreThanADay_LessThanAWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysAgo = now.minusDays(3);
        post.setDate(threeDaysAgo);

        Assertions.assertEquals("3 Days", post.getTimeSincePosted());
    }

    @Test
    void testGetTimeSincePosted_MoreThanAWeek() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);
        post.setDate(tenDaysAgo);

        Assertions.assertEquals("1 Week", post.getTimeSincePosted());
    }

    @Test
    void testGetTimeSincePosted_MoreThanTwoWeeks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenDaysAgo = now.minusDays(15);
        post.setDate(fifteenDaysAgo);

        Assertions.assertEquals("2 Weeks", post.getTimeSincePosted());
    }

}
