package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AgoraController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LiveStreamEndIntegrationTests {

    @Autowired
    AgoraController agoraController;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LivestreamRepository livestreamRepository;

    private User hostUser;
    private User audienceUser;
    private Livestream livestream;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() throws JsonProcessingException {
        // Save user
        hostUser = userRepository.save(new User("host@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));
        audienceUser = userRepository.save(new User("audiance@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));

        // Create livestream
        livestream = livestreamRepository.save(new Livestream(hostUser, "title", "description", "fake/file/path"));

        // Create MVC
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(agoraController)
                .build();
    }

    /**
     * Cleanup user after each test to prevent duplicate users and posts causing errors
     */
    @AfterEach
    void cleanup() {
        // Clear DB
        livestreamRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    void givenAudience_DoesntDelete () throws Exception {
        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(audienceUser.getEmail(), null, Collections.emptyList()));

        this.mockMvc.perform(delete("/livestream/end/" + livestream.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("error"));

        Optional<Livestream> livestream1 = livestreamRepository.findById(livestream.getId());

        Assertions.assertFalse(livestream1.isEmpty());
    }

    @Test
    void givenOwner_DoesDelete() throws Exception {
        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(hostUser.getEmail(), null, Collections.emptyList()));

        this.mockMvc.perform(delete("/livestream/end/" + livestream.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Optional<Livestream> livestream1 = livestreamRepository.findById(livestream.getId());

        Assertions.assertTrue(livestream1.isEmpty());
    }

    @Test
    void givenDoesntExist() throws  Exception {
        // Auth user
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(hostUser.getEmail(), null, Collections.emptyList()));

        this.mockMvc.perform(delete("/livestream/end/999"))
                .andExpect(status().isOk())
                .andExpect(content().string("error"));
    }
}
