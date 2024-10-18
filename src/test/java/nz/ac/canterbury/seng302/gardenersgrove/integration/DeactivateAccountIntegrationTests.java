package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.controller.CommentController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeactivateAccountIntegrationTests {
    User user;
    User user1;
    Garden garden1;
    Post post;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    GardenController gardenController;
    @Autowired
    CommentController commentController;
    @MockBean
    EmailService emailService;
    @MockBean
    ProfanityFilterService profanityFilterService;
    private final AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);
    MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        user = new User("tagTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1 = userRepository.save(user);

        garden1 = new Garden("Test1", addressDTO, user1);
        garden1 = gardenRepository.save(garden1);

        post = new Post("", "Post Content", user);
        post = postRepository.save(post);

        mockMvc = MockMvcBuilders
                .standaloneSetup(gardenController, commentController)
                .build();
    }

    @BeforeEach
    void eachTest() throws JsonProcessingException {
        Mockito.when(profanityFilterService.isTextProfane("shit")).thenReturn(true);
    }

    @AfterAll
    public void cleanUp() {
        gardenRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    //Tests that the user can see the fifthInappropriateTag modal and that they receive a warning email when they add a fifth inappropriate tag
    @Test
    @WithMockUser(username = "tagTester@gmail.com")
    void add_fifth_inappropriate_tag() throws Exception {
        user1.setInappropriateWarningCount(4);
        user1 = userRepository.save(user1);
        mockMvc.perform(put("/garden/" + garden1.getGardenId() + "/tag")
                        .param("tag", "shit"))
                .andExpectAll(
                        status().is(302),
                        redirectedUrl("/garden/" + garden1.getGardenId()),
                        flash().attribute("fifthInappropriateSubmission", "true")
                ).andReturn();

        String recipient = "tagTester@gmail.com";
        String subject = "Fifth inappropriate content submission warning";
        String htmlTemplate = "fifth-inappropriate-content-warning";
        //Used to test functions called in await loop, help from Copilot (AI)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            Mockito.verify(emailService).sendEmail(eq(recipient),eq(subject),eq(htmlTemplate),Mockito.any())
        );
    }

    //Tests that the user can see the blocked modal and that they receive an email telling them they are blocked when they add a sixth inappropriate tag
    @Test
    @WithMockUser(username = "tagTester@gmail.com")
    void add_sixth_inappropriate_tag() throws Exception {
        user1.setInappropriateWarningCount(5);
        user1 = userRepository.save(user1);
        mockMvc.perform(put("/garden/" + garden1.getGardenId() + "/tag")
                        .param("tag", "shit"))
                .andExpectAll(
                        status().is(302),
                        redirectedUrl("/auth/login"),
                        flash().attribute("blocked", "true")
                ).andReturn();
        //Used to test functions called in await loop, help from Copilot (AI)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            Mockito.verify(emailService).sendEmail(eq("tagTester@gmail.com"),eq("Blocked Account"),eq("blocked-account-email"),Mockito.any())
        );
    }

    //Tests that the user can see the fifthInappropriateSubmission modal and that they receive a warning email when they add a fifth inappropriate submission
    @Test
    @WithMockUser(username = "tagTester@gmail.com")
    void add_fifth_inappropriate_comment() throws Exception {
        user1.setInappropriateWarningCount(4);
        user1 = userRepository.save(user1);
        mockMvc.perform(put("/post/" + post.getId() + "/comment")
                .contentType(MediaType.APPLICATION_JSON) // from ChatGPT
                .content("{\"comment\":\"shit\"}"))
                .andExpectAll(
                        status().is(400),
                        header().exists("Fifth-Inappropriate-Submission"),
                        header().stringValues("Fifth-Inappropriate-Submission", "true")
                );

        String recipient = "tagTester@gmail.com";
        String subject = "Fifth inappropriate content submission warning";
        String htmlTemplate = "fifth-inappropriate-content-warning";
        // Used to test functions called in await loop, help from Copilot (AI)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                Mockito.verify(emailService).sendEmail(eq(recipient),eq(subject),eq(htmlTemplate),Mockito.any())
        );
    }

}
