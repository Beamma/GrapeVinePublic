package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
public class FriendServiceTest {

    FriendService friendService;
    FriendRepository friendRepository;

    @BeforeEach
    public void setUp() {
        friendRepository = Mockito.mock(FriendRepository.class);
        friendService = new FriendService(friendRepository);

        // Set up a valid user to be used in tests
        User user1 = new User("Jake@email.com", "2000-01-01", "Jake", "Dalton", false, "Password1!", "Password1!");
        User user2 = new User("Yunu@email.com", "2000-01-01", "Yunu", "Cho", false, "Password1!", "Password1!");
    }

    @Test
    public void GivenBothIdsValid_ReturnsTrue () {
        boolean validRequest = friendService.checkFriendRequest("1", "2");
        Assertions.assertTrue(validRequest);
    }

    @Test
    public void GivenBothId1IsValidAndId2IsChar_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("1", "s");
        Assertions.assertFalse(validRequest);
    }

    @Test
    public void GivenBothId2IsValidAndId1IsChar_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("a", "1");
        Assertions.assertFalse(validRequest);
    }

    @Test
    public void GivenBothAreSameNum_ReturnsTrue () {
        boolean validRequest = friendService.checkFriendRequest("1", "1");
        Assertions.assertTrue(validRequest);
    }

    @Test
    public void GivenBothAreChar_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("a", "a");
        Assertions.assertFalse(validRequest);
    }

    @Test
    public void GivenBothAreEmptyString_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("", "");
        Assertions.assertFalse(validRequest);
    }

    @Test
    public void GivenId1EmptyString_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("", "1");
        Assertions.assertTrue(validRequest);
    }

    @Test
    public void GivenId2EmptyString_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("1", "");
        Assertions.assertTrue(validRequest);
    }

    @Test
    public void GivenId1NegNum_ReturnsFalse () {
        boolean validRequest = friendService.checkFriendRequest("-1", "2");
        Assertions.assertFalse(validRequest);
    }


}