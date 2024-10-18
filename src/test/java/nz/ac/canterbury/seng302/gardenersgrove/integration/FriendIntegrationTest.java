package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.FriendController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friend;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.ForgotPasswordListener;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.exception.FriendNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class FriendIntegrationTest {

    MockMvc mockMvc;
    private UserService userService;

    private FriendRepository friendRepository;

    private FriendService friendService;

    private GardenService gardenService;

    private FriendController friendController;

    private List<Friend> allFriends;

    private UserRepository userRepository;

    private User user1;

    private User user2;

    private User user3;

    private User user4;
    private Friend friend1;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RegistrationListener registrationListener;
    @MockBean
    private ForgotPasswordListener forgotPasswordListener;
    @MockBean
    private WeatherService weatherService;

    @BeforeEach
    public void setup() {
        userService = Mockito.mock(UserService.class);
        gardenService = Mockito.mock(GardenService.class);
        friendService = Mockito.mock(FriendService.class);
        friendRepository = Mockito.mock(FriendRepository.class);

        // User1 <------Friends---------> User 2
        //   ^                                 ^
        //   |                                 |
        //   pending                           request declined
        //   |_____________________________    |
        //                                 |   |
        //           User4                 User3
        //
        // User1 and User2 are friends
        // User 2 declined a friend request from user3
        // User 1 has not yet accepted a friend request from user3
        // User4 has no friends, and no pending requests

        user1 = Mockito.mock(User.class);
        Mockito.when(user1.getId()).thenReturn(1L);

        user2 = Mockito.mock(User.class);
        Mockito.when(user2.getId()).thenReturn(2L);

        user3 = Mockito.mock(User.class);
        Mockito.when(user3.getId()).thenReturn(3L);

        user4 = Mockito.mock(User.class);
        Mockito.when(user4.getId()).thenReturn(4L);

        Mockito.when(userService.getById("1")).thenReturn(user1);
        Mockito.when(userService.getById("2")).thenReturn(user2);
        Mockito.when(userService.getById("3")).thenReturn(user3);
        Mockito.when(userService.getById("4")).thenReturn(user4);
        Mockito.when(userService.getById("99")).thenReturn(null);

        friend1 = Mockito.mock(Friend.class);
        Mockito.when(friend1.getFriendId()).thenReturn(1L);
        Mockito.when(friend1.getSender()).thenReturn(user1);
        Mockito.when(friend1.getRecipient()).thenReturn(user2);
        Mockito.when(friendService.getFriendRequestsByUser1User2(user1.getId(), friend1.getFriendId())).thenReturn(List.of(friend1));
        Mockito.when(friend1.getStatus()).thenReturn("accepted");

        Friend friend2 = Mockito.mock(Friend.class);
        Mockito.when(friend2.getFriendId()).thenReturn(2L);
        Mockito.when(friend2.getSender()).thenReturn(user3);
        Mockito.when(friend2.getRecipient()).thenReturn(user1);
        Mockito.when(friend2.getStatus()).thenReturn("pending");

        Friend friend3 = Mockito.mock(Friend.class);
        Mockito.when(friend3.getFriendId()).thenReturn(3L);
        Mockito.when(friend3.getSender()).thenReturn(user3);
        Mockito.when(friend3.getRecipient()).thenReturn(user2);
        Mockito.when(friend3.getStatus()).thenReturn("declined");

        allFriends = new ArrayList<>();
        allFriends.add(friend1);
        allFriends.add(friend2);
        allFriends.add(friend3);
        Mockito.when(friendService.getFriends()).thenReturn(allFriends);
        Mockito.when(friendService.getByFriendshipId(Mockito.any(Long.class))).thenAnswer(i -> {
            int index = (int) (long) i.getArgument(0) - 1;
            return Optional.of(allFriends.get(index));
        });

        List<User> user1sFriends = new ArrayList<>();
        user1sFriends.add(user2);
        Mockito.when(friendService.getCurrentUsersFriends(user1)).thenReturn(user1sFriends);

        List<User> user2sFriends = new ArrayList<>();
        user2sFriends.add(user1);
        Mockito.when(friendService.getCurrentUsersFriends(user2)).thenReturn(user2sFriends);

        List<User> user3sFriends = new ArrayList<>();
        Mockito.when(friendService.getCurrentUsersFriends(user3)).thenReturn(user3sFriends);

        List<Friend> user3sSentRequests = new ArrayList<>();
        user3sSentRequests.add(friend2);
        user3sSentRequests.add(friend3);
        Mockito.when(friendService.getOutGoingRequests(3L)).thenReturn(user3sSentRequests);

        List<Friend> user1sPendingRequests = new ArrayList<>();
        user1sPendingRequests.add(friend2);
        Mockito.when(friendService.getIncomingRequests(1L)).thenReturn(user1sPendingRequests);

        FriendController friendController = new FriendController(friendService, userService, gardenService);

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(friendController)
                .build();
    }

    @Test
    public void GivenUser1CurrentUser_ReturnValidProperties () {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        try {
            MvcResult mvcResult = mockMvc.perform(get("/manage/friends"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

            List<User> friends = (List<User>) modelMap.getAttribute("friends");
            List<Friend> sentRequests = (List<Friend>) modelMap.getAttribute("sentRequests");
            List<Friend> recievedRequests = (List<Friend>) modelMap.getAttribute("requests");

            assertEquals(2L, friends.get(0).getId());
            assertEquals(1, friends.size());
            assertEquals(0, sentRequests.size());
            assertEquals(1, recievedRequests.size());
            assertEquals(2L, recievedRequests.get(0).getFriendId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser2CurrentUser_ReturnValidProperties () {
        Mockito.when(userService.getCurrentUser()).thenReturn(user2);
        try {
            MvcResult mvcResult = mockMvc.perform(get("/manage/friends"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

            List<User> friends = (List<User>) modelMap.getAttribute("friends");
            List<Friend> sentRequests = (List<Friend>) modelMap.getAttribute("sentRequests");
            List<Friend> recievedRequests = (List<Friend>) modelMap.getAttribute("requests");

            assertEquals(1L, friends.get(0).getId());
            assertEquals(1, friends.size());
            assertEquals(0, sentRequests.size());
            assertEquals(0, recievedRequests.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser3CurrentUser_ReturnValidProperties () {
        Mockito.when(userService.getCurrentUser()).thenReturn(user3);
        try {
            MvcResult mvcResult = mockMvc.perform(get("/manage/friends"))
                    .andExpect(status().isOk())
                    .andReturn();

            ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

            List<User> friends = (List<User>) modelMap.getAttribute("friends");
            List<Friend> sentRequests = (List<Friend>) modelMap.getAttribute("sentRequests");
            List<Friend> recievedRequests = (List<Friend>) modelMap.getAttribute("requests");

            assertEquals(0, friends.size());
            assertEquals(2, sentRequests.size());
            assertEquals(2L, sentRequests.get(0).getFriendId());
            assertEquals(3L, sentRequests.get(sentRequests.size() - 1).getFriendId());
            assertEquals(0, recievedRequests.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenValidRequestUser4To1_CallsAddFriend () throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user4);
        try {
            mockMvc.perform(post("/manage/friends/add")
                            .param("add-friend", String.valueOf(1L)))
                    .andExpect(status().isFound())
                    .andReturn();


            Mockito.verify(friendService).addFriend(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenInValidRequestUser4ToNonExistId_ThrowsError () throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user4);
        try {
            mockMvc.perform(post("/manage/friends/add")
                            .param("add-friend", String.valueOf(99L)))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "Sorry that user no longer exists"))
                    .andReturn();


            Mockito.verify(friendService, Mockito.times(0)).addFriend(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenValidRequestUser3User1ExisitingRequest_ThrowsError () throws FriendNotFoundException {
        Mockito.when(userService.getCurrentUser()).thenReturn(user3);
        // Mock add friend to throw error when request between user3 and user1
        Mockito.when(friendService.addFriend(argThat(new FriendMatcher(new Friend(user3, user1))))).thenThrow(new FriendNotFoundException(("A request has previously existed between you and this user")));
        try {
            mockMvc.perform(post("/manage/friends/add")
                            .param("add-friend", String.valueOf(1L)))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "A request has previously existed between you and this user"))
                    .andReturn();


            Mockito.verify(friendService, Mockito.times(1)).addFriend(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", " ", "-1"})
    public void GivenInvalidAcceptIds_ReturnError (String acceptId) {
        FriendService  friendService = new FriendService(friendRepository);

        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", acceptId)
                            .param("decline", ""))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "Invalid Friend Id"))
                    .andReturn();


            Mockito.verify(friendRepository, Mockito.times(0)).save(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", " ", "-1"})
    public void GivenInvalidDeclineIds_ReturnError (String declineId) {
        FriendService  friendService = new FriendService(friendRepository);

        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", "")
                            .param("decline", declineId))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "Invalid Friend Id"))
                    .andReturn();


            Mockito.verify(friendRepository, Mockito.times(0)).save(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @CsvSource({"a,a", "a,1", "1,a", "a,", ",a", "1,1", "-1,-1", "-1,", ",-1", ","})
    public void GivenInvlalidAcceptAndDeclineIds (String acceptId, String declineId) {
        FriendService  friendService = new FriendService(friendRepository);

        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", acceptId)
                            .param("decline", declineId))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "Invalid Friend Id"))
                    .andReturn();


            Mockito.verify(friendRepository, Mockito.times(0)).save(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenAcceptIdDontExist () throws FriendNotFoundException {
        Mockito.when(friendService.checkFriendRequest("99", "")).thenReturn(true);
        Mockito.when(friendService.changeFriendStatus(99L, "accepted")).thenThrow(new FriendNotFoundException("Friend with the id 99 does not exist"));
        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", "99")
                            .param("decline", ""))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "Friend with the id 99 does not exist"))
                    .andReturn();


            Mockito.verify(friendRepository, Mockito.times(0)).save(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenDeclineIdDontExist () throws FriendNotFoundException {
        Mockito.when(friendService.checkFriendRequest("", "99")).thenReturn(true);
        Mockito.when(friendService.changeFriendStatus(99L, "declined")).thenThrow(new FriendNotFoundException("Friend with the id 99 does not exist"));
        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", "")
                            .param("decline", "99"))
                    .andExpect(status().isFound())
                    .andExpect(flash().attribute("error", "Friend with the id 99 does not exist"))
                    .andReturn();


            Mockito.verify(friendRepository, Mockito.times(0)).save(Mockito.any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenValidAcceptId_CallChangeFriendStatus () {
        Mockito.when(friendService.checkFriendRequest("2", "")).thenReturn(true);
        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", "2")
                            .param("decline", ""))
                    .andExpect(status().isFound())
                    .andReturn();


            Mockito.verify(friendService, Mockito.times(1)).changeFriendStatus(2L, "accepted");
            Mockito.verify(friendService, Mockito.times(0)).changeFriendStatus(2L, "declined");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenValidDeclineId_CallChangeFriendStatus () {
        Mockito.when(friendService.checkFriendRequest("", "2")).thenReturn(true);
        try {
            mockMvc.perform(post("/manage/friends/request")
                            .param("accept", "")
                            .param("decline", "2"))
                    .andExpect(status().isFound())
                    .andReturn();


            Mockito.verify(friendService, Mockito.times(1)).changeFriendStatus(2L, "declined");
            Mockito.verify(friendService, Mockito.times(0)).changeFriendStatus(2L, "accepted");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenIHaveSentFriendRequest_whenICancelIt_theRequestIsNoLongerExistent() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user3);
            mockMvc.perform(delete("/manage/friends/request/cancel")
                            .param("friendId", "2"))
                    .andExpect(status().isFound());

            Assertions.assertFalse(allFriends.stream().anyMatch(f -> f.getSender() == user3 && f.getSender() == user1));
    }

    @Test
    public void givenUsersAreFriends_whenOneDeletesFriendship_thenFriendshipIsDeleted() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
            mockMvc.perform(post("/friend")
                            .param("friendId", "1"))
                    .andExpect(status().isFound());
            Mockito.when(friend1.getStatus()).thenReturn("removed");
            Assertions.assertEquals("removed", friendService.getByFriendshipId(1L).get().getStatus());
    }

}

/*
 * Sourced https://www.baeldung.com/mockito-argument-matchers
 */

class FriendMatcher implements ArgumentMatcher<Friend> {

    private Friend left;

    // constructors
    FriendMatcher(Friend left) {
        this.left = left;
    }

    @Override
    public boolean matches(Friend right) {
        return left.getSender().equals(right.getSender()) &&
                left.getRecipient().equals(right.getRecipient());
    }
}