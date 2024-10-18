package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.FriendController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friend;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.ForgotPasswordListener;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.repository.FriendRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SendFriendRequestFeature {

    private static MockMvc mockMvc;
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
    private Friend friend3;
    private Path path;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RegistrationListener registrationListener;
    @MockBean
    private ForgotPasswordListener forgotPasswordListener;

    private ResultActions request;
    private MockHttpServletRequestBuilder requestBuilder;


    private MvcResult mvcResult;
    private ModelMap modelMap;
    private List<User> friends;
    private List<Friend> sentRequests;
    private List<Friend> receivedRequests;
    private List<User> userSearchList;

    private ResultActions result;

    @Before
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

        user1 = Mockito.spy(User.class);
        Mockito.when(user1.getId()).thenReturn(1L);
        user1.setFirstName("Jane");
        user1.setLastName("Doe");
        user1.setEmail("Jane@email.com");

        user2 = Mockito.spy(User.class);
        Mockito.when(user2.getId()).thenReturn(2L);

        user3 = Mockito.spy(User.class);
        Mockito.when(user3.getId()).thenReturn(3L);

        user4 = Mockito.spy(User.class);
        Mockito.when(user4.getId()).thenReturn(4L);
        user4.setFirstName("Johnny");
        user4.setLastName("Smithy");
        user4.setEmail("JohnnySmith123@email.com");

        //requires Page result then getContents() retrieves a List<> result
        Page<User> userSearchResult = Mockito.mock(Page.class);
        Mockito.when(userService.searchUsers(Mockito.anyString(), Mockito.any())).thenReturn(userSearchResult);

        Mockito.when(userSearchResult.getTotalPages()).thenReturn(1);
        userSearchList = new ArrayList<>();
        userSearchList.add(user1);
        userSearchList.add(user4);
        Mockito.when(userSearchResult.getContent()).thenReturn(userSearchList);

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

        Friend friend2 = Mockito.spy(Friend.class);
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
        user1.setId(1L);
        Mockito.when(friendService.getCurrentUsersFriends(user1)).thenReturn(user1sFriends);

        List<User> user2sFriends = new ArrayList<>();
        user2.setId(2L);
        user2sFriends.add(user1);
        Mockito.when(friendService.getCurrentUsersFriends(user2)).thenReturn(user2sFriends);

        List<User> user3sFriends = new ArrayList<>();
        user3.setId(3L);
        Mockito.when(friendService.getCurrentUsersFriends(user3)).thenReturn(user3sFriends);

        List<Friend> user3sSentRequests = new ArrayList<>();
        user3sSentRequests.add(friend2);
        Mockito.when(friend3.getFriendId()).thenReturn(3L);
        user3sSentRequests.add(friend3);
        Mockito.when(friendService.getOutGoingRequests(3L)).thenReturn(user3sSentRequests);

        List<Friend> user1sPendingRequests = new ArrayList<>();
        user1sPendingRequests.add(friend2);
        Mockito.when(friendService.getIncomingRequests(1L)).thenReturn(user1sPendingRequests);

        FriendController friendController = new FriendController(friendService, userService, gardenService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(friendController)
                .build();
    }

    @Given("I am anywhere in the app")
    public void i_am_anywhere_in_the_app() {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        user1.grantAuthority("ROLE_USER");
        user1.setEnabled(true);
    }

    @When("I click the Friend button")
    public void i_click_the_friend_button() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/manage/friends"));
    }

    @Then("I am navigated to the manage friends page")
    public void i_am_navigated_to_the_manage_friends_page() throws Exception {
        request.andExpect(status().isOk())
                .andExpect(view().name("manageFriends/manageFriends"))
                .andReturn();
    }

    @Given("I am on the manage friends page")
    public void i_am_on_the_manage_friends_page() {
        requestBuilder = MockMvcRequestBuilders.get("/manage/friends");
        path = Paths.get("src/main/resources/templates/manageFriends/manageFriends.html");
    }

    @Then("I can see my friends details including their profile picture, names and a link to their gardens")
    public void i_can_see_my_friends_details_including_their_profile_picture_names_and_a_link_to_their_gardens() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        boolean containsFriendProfile = Files.lines(path)
                .anyMatch(line -> line.contains("class=\"profile-picture\""));
        boolean containsFriendName = Files.lines(path)
                .anyMatch(line -> line.contains("class=\"friend-details\""));
        boolean containsFriendGardenLink = Files.lines(path)
                .anyMatch(line -> line.contains("class=\"garden-list-link\""));
        Assertions.assertTrue(containsFriendProfile);
        Assertions.assertTrue(containsFriendName);
        Assertions.assertTrue(containsFriendGardenLink);
    }

    @When("I click the add friend button")
    public void i_click_the_add_friend_button() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        requestBuilder = MockMvcRequestBuilders.get("/manage/friends");
    }

    @Then("I see a search bar")
    public void i_see_a_search_bar() throws Exception {
        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
        boolean containsSearchBar = Files.lines(path)
                .anyMatch(line -> line.contains("id=\"search-button\" type=\"submit\""));
        Assertions.assertTrue(containsSearchBar);
    }

    @When("I enter {string} and {string}")
    public void i_enter_and(String firstName, String lastName) {
        requestBuilder.param("search", firstName + lastName);
    }

    @Then("a list of users matching the {string} and {string} are provided to me")
    public void a_list_of_users_matching_the_and_are_provided_to_me(String firstName, String lastName) throws Exception {
        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

        List<User> returnedUsers = (List<User>) modelMap.getAttribute("users");
        Boolean userFound = returnedUsers
                .stream()
                .anyMatch(user -> user.getFirstName().equals(firstName) || user.getLastName().equals(lastName));
        Assertions.assertTrue(userFound);
    }

    @When("I enter a valid email address {string}")
    public void i_enter_a_valid_email_address(String email) {
        requestBuilder.param("search", email);
    }

    @Then("a list of users matching the email {string} are provided to me")
    public void a_list_of_users_matching_the_email_are_provided_to_me(String email) throws Exception {
        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

        List<User> returnedUsers = (List<User>) modelMap.getAttribute("users");
        Boolean userFound = returnedUsers
                .stream()
                .anyMatch(user -> user.getEmail().equals(email));
        Assertions.assertTrue(userFound);
    }

    @When("I enter a search string with no matches")
    public void i_enter_a_search_string_with_no_matches() {
        String searchString = "NoMatches";
        requestBuilder.param("search", searchString);
    }

    @Then("A message saying {string} appears")
    public void a_message_saying_appears(String string) throws Exception {
        String searchString = "NoMatches";

        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        List<User> returnedUsers = (List<User>) modelMap.getAttribute("users");

        Boolean userFound = returnedUsers
                .stream()
                .anyMatch(user -> user.getEmail().equals(searchString) || user.getFirstName().equals(searchString) || user.getLastName().equals(searchString));
        Assertions.assertFalse(userFound);
    }

    @Given("A friend search provides a result")
    public void a_friend_search_provides_a_result() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user3);
        requestBuilder = MockMvcRequestBuilders.get("/manage/friends");
        String searchString = "NoMatches";
        requestBuilder.param("search", searchString);

        mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();
        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        List<User> returnedUsers = (List<User>) modelMap.getAttribute("users");
        Assertions.assertNotNull(returnedUsers);
    }

    @When("I click the invite as friend button")
    public void i_click_the_invite_as_friend_button() throws Exception {
        mockMvc.perform(post("/manage/friends/add")
                        .param("add-friend", String.valueOf(1L)))
                .andExpect(status().isFound())
                .andReturn();
    }

    @Then("The receiving user sees an invite request on their manage friends page")
    public void the_receiving_user_sees_an_invite_request_on_their_manage_friends_page() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        mvcResult = (MvcResult) mockMvc.perform(get("/manage/friends"))
                .andExpect((status().isOk()))
                .andReturn();

        modelMap = mvcResult.getModelAndView().getModelMap();
        List<Friend> receivedRequests = (List<Friend>) modelMap.getAttribute("requests");
        Boolean requestExists = receivedRequests
                .stream()
                .anyMatch(request -> request.getFriendId().equals("3"));
    }

    @Given("I have pending invites")
    public void i_have_pending_invites() throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/manage/friends");
    }

    @When("I accept an invite from a user")
    public void i_accept_an_invite_from_a_user() throws Exception {
        //move initial get: request/friends here to initialize mockUser in unique step
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        mvcResult = (MvcResult) mockMvc.perform(requestBuilder)
                .andReturn();

        modelMap = mvcResult.getModelAndView().getModelMap();
        List<Friend> receivedRequests = (List<Friend>) modelMap.getAttribute("requests");
        Assertions.assertNotNull(receivedRequests);
        Mockito.when(friendService.checkFriendRequest("2", "")).thenReturn(true);
    }

    @Then("the user is added to my friends list and I can view their profile")
    public void the_user_is_added_to_my_friends_list_and_i_can_view_their_profile() throws Exception {
        mvcResult = mockMvc.perform(post("/manage/friends/request")
                        .param("accept", "2")
                        .param("decline", ""))
                .andExpect(redirectedUrl("/manage/friends"))
                .andReturn();

        Mockito.verify(friendService, Mockito.times(1)).changeFriendStatus(2L, "accepted");
        Mockito.verify(friendService, Mockito.times(0)).changeFriendStatus(2L, "declined");
    }

    @Then("I am added to the users friends list and they can view my profile")
    public void i_am_added_to_the_users_friends_list_and_they_can_view_my_profile() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user2);
        mvcResult = (MvcResult) mockMvc.perform(get("/manage/friends"))
                .andExpect((status().isOk()))
                .andReturn();
        modelMap = mvcResult.getModelAndView().getModelMap();
        List<User> userFriends = (List<User>) modelMap.getAttribute("friends");
        Boolean friendExists = userFriends
                .stream()
                .anyMatch((friend -> friend.getId().equals(1L)));

        Assertions.assertTrue(friendExists);
    }

    @When("I decline an invite from a user")
    public void i_decline_an_invite_from_a_user() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        mvcResult = (MvcResult) mockMvc.perform(requestBuilder)
                .andReturn();

        modelMap = mvcResult.getModelAndView().getModelMap();
        List<Friend> receivedRequests = (List<Friend>) modelMap.getAttribute("requests");
        Assertions.assertNotNull(receivedRequests);
    }

    @Then("the user is not added to my friends list")
    public void the_user_is_not_added_to_my_friends_list() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user3);
        Mockito.when(friendService.checkFriendRequest("", "2")).thenReturn(true);

        mvcResult = mockMvc.perform(post("/manage/friends/request")
                        .param("accept", "")
                        .param("decline", "2"))
                .andExpect(redirectedUrl("/manage/friends"))
                .andReturn();

        Mockito.verify(friendService, Mockito.times(1)).changeFriendStatus(2L, "declined");
        Mockito.verify(friendService, Mockito.times(0)).changeFriendStatus(2L, "accepted");
    }

    @Then("they cannot invite me anymore")
    public void they_cannot_invite_me_anymore() throws Exception {
        Mockito.when(userService.getById("2")).thenReturn(user3);
        Mockito.when(friendService.addFriend(Mockito.any())).thenReturn(friend3);
        mvcResult = mockMvc.perform(post("/manage/friends/add")
                        .param("add-friend", "2"))
                .andExpect(redirectedUrl("/manage/friends"))
                .andReturn();
    }

    @Then("I can see the status of the invite as pending")
    public void i_can_see_the_status_of_the_invite_as_pending() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        mvcResult = mockMvc.perform(get("/manage/friends/"))
                .andExpect((status().isOk()))
                .andReturn();
        modelMap = mvcResult.getModelAndView().getModelMap();
        List<Friend> sentRequests = (List<Friend>) modelMap.getAttribute("sentRequests");
        Assertions.assertNotNull(sentRequests);
    }

    @Then("I can see the status of the invite as declined")
    public void i_can_see_the_status_of_the_invite_as_declined() throws Exception {
        Mockito.when(userService.getCurrentUser()).thenReturn(user3);

        mvcResult = mockMvc.perform(get("/manage/friends/"))
                .andExpect((status().isOk()))
                .andReturn();
        modelMap = mvcResult.getModelAndView().getModelMap();
        List<Friend> sentRequests = (List<Friend>) modelMap.getAttribute("sentRequests");
        Optional<Friend> rejectedFriend = sentRequests
                .stream()
                .filter(friend -> friend.getFriendId().equals(3L))
                .findFirst();

        Assertions.assertEquals(rejectedFriend.get().getStatus(), "declined");
    }
}
