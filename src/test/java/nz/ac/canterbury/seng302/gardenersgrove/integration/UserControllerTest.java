package nz.ac.canterbury.seng302.gardenersgrove.integration;

import io.cucumber.java.Before;
import nz.ac.canterbury.seng302.gardenersgrove.controller.UserController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserController userController;

    private MockMvc mockMvc;

    private MockMultipartFile imageOver2mb;

    User user;

    @BeforeEach
    public void setUp() throws IOException {
        Resource fileResource = new ClassPathResource("test-images/png_over_2mb.png");

        imageOver2mb = new MockMultipartFile("ProfileImage", "over2mb.png", "image/png", fileResource.getInputStream());

        userRepository.deleteAll();
        User saveUser = new User("postTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user = userRepository.save(saveUser);

        var authentication = new UsernamePasswordAuthenticationToken("postTester@gmail.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    /**
     * Delete all data from the database after class is run
     */
    @AfterEach
    void cleanup() throws IOException {
        userRepository.deleteAll();
        Files.delete(Path.of("src/main/resources/user-images/"+user.getId()+".png"));
    }

    @Test
    void testUserUpload2MbImage_imageResized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/user/profile/" + user.getId())
                .file(imageOver2mb)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is(302));

        // Check that the image is smaller than 2mb
        Assertions.assertTrue( Files.size(Path.of("src/main/resources/user-images/"+user.getId()+".png")) < 2000000);
    }


}
