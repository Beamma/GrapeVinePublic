package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.GardenersGroveApplication;
import nz.ac.canterbury.seng302.gardenersgrove.controller.FriendController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friend;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.ForgotPasswordListener;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(classes = GardenersGroveApplication.class)
@SpringBootTest
@ActiveProfiles("test")
public class FriendSearchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private PlantRepository plantRepository;

    @MockBean
    private SecurityContext securityContext;

    private User user;

    private User user1;
    private User user2;
    private User user3;
    private User user4;
    private User user5;
    private User user6;
    private User user7;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();

        SecurityContextHolder.setContext(securityContext);
        user = new User("email@email.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user = userRepository.save(user);

        // Add other users
        user1 = userRepository.save(new User("user1@email.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));
        user2 = userRepository.save(new User("user2@email.com", "2001-01-01", "John", null, true, "Password1!", "Password1!"));
        user3 = userRepository.save(new User("user3@email.com", "2001-01-01", "Ben", "John", false, "Password1!", "Password1!"));
        user4 = userRepository.save(new User("user4@email.com", "2001-01-01", "Tom", "Johnson", false, "Password1!", "Password1!"));
        user5 = userRepository.save(new User("user5@email.com", "2001-01-01", "John", "Doesenberg", false, "Password1!", "Password1!"));
        user6 = userRepository.save(new User("user6@email.com", "2001-01-01", "Little John", "Doe", false, "Password1!", "Password1!"));
        user7 = userRepository.save(new User("user7@email.com", "2001-01-01", "Little John", null, true, "Password1!", "Password1!"));


        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void cleanUp() {
        gardenRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    private boolean sameNameAndId(User expected, User actual) {
        return expected.getId().equals(actual.getId()) &&
                expected.getFirstName().equals(actual.getFirstName()) &&
                expected.getNoLastName() == actual.getNoLastName() &&
                (expected.getNoLastName() || expected.getLastName().equals(actual.getLastName()));
    }

    @Test
    void gettingFriendsPage_noSearchQuery_returnsEmptyFriendsPage() throws Exception {
        mockMvc.perform(get("/manage/friends"))
                .andExpect(status().isOk());
    }

    @Test
    void gettingFriendsPage_withFirstNameOnly_ReturnsMatchingFirstNamesWithNoLastNames() throws Exception {
        String query = "John";
        int expectedLength = 1;

        List<User> users = (List<User>) mockMvc.perform(get("/manage/friends?search=" + query + "&page=1"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("users");

        Assertions.assertEquals(expectedLength, users.size());
        Assertions.assertTrue(sameNameAndId(user2, users.get(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Nomatchingnames", "Nomatching Names", "nomatching@emails.com"})
    void gettingFriendsPage_withNoMatchingUsers_returnsSearchError(String query) throws Exception {
        mockMvc.perform(get("/manage/friends?search=" + query + "&page=1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchError", "There is nobody with that name or email in Gardener's Grove."));

    }

    @Test
    void gettingFriendsPage_withSameNameAsUser_ReturnsAllMatchingNamesExcludingUser() throws Exception {
        String query = user.getFirstName() + " " + user.getLastName();
        int expectedLength = 1;

        List<User> users = (List<User>) mockMvc.perform(get("/manage/friends?search=" + query + "&page=1"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("users");

        Assertions.assertEquals(expectedLength, users.size());
        Assertions.assertFalse(users.stream().anyMatch(u -> sameNameAndId(user, u)));
        Assertions.assertTrue(sameNameAndId(user1, users.get(0)));
    }

    @Test
    void gettingFriendsPage_withSpaceInFirstNameNoLastName_ReturnsAllMatchingFirst() throws Exception {
        String query = user7.getFirstName();
        int expectedLength = 1;

        List<User> users = (List<User>) mockMvc.perform(get("/manage/friends?search=" + query + "&page=1"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("users");

        Assertions.assertEquals(expectedLength, users.size());
        Assertions.assertTrue(sameNameAndId(user7, users.get(0)));
    }

    @Test
    void gettingFriendsPage_withSpaceInFirstNameAndLastName_ReturnsAllMatchingFirstAndLast() throws Exception {
        String query = user6.getFirstName() + " " + user6.getLastName();
        int expectedLength = 1;

        List<User> users = (List<User>) mockMvc.perform(get("/manage/friends?search=" + query + "&page=1"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("users");

        Assertions.assertEquals(expectedLength, users.size());
        Assertions.assertTrue(sameNameAndId(user6, users.get(0)));
    }
}
