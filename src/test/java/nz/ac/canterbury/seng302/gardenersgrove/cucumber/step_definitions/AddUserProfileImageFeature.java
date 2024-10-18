package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.UserController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class AddUserProfileImageFeature {

    private static MockMvc mockMvc;

    private ResultActions resultActions;

    private static GardenService mockGardenService;

    private static UserService mockUserService;

    private static UserAuthenticationService userAuthenticationService;

    private Path path;

    private String button;

    private static User returnedUser;

    private static AuthenticationManager mockAuthenticationManager;
    @MockBean
    private static PasswordEncoder passwordEncoder;
    @MockBean
    private static VerificationTokenRepository verificationTokenRepository;

    @BeforeAll
    public static void before_or_after_all() {
        mockGardenService = Mockito.mock(GardenService.class);
        mockUserService = Mockito.mock(UserService.class);
        mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        userAuthenticationService = new UserAuthenticationService(
                mockUserService,
                mockAuthenticationManager,
                passwordEncoder,
                verificationTokenRepository);
        UserController userController = new UserController(userAuthenticationService, mockUserService, mockGardenService);

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setViewResolvers(viewResolver)
                .build();

        Mockito.when(mockUserService.validateUserId(Mockito.anyString())).thenReturn(true);
        returnedUser = new User();
        returnedUser.setId(1L);
        returnedUser.setEmail("john.doe@ebob.com");
        returnedUser.setFirstName("John");
        returnedUser.setLastName("Doe");
        returnedUser.grantAuthority("ROLE_USER");
        returnedUser.setProfileImagePath("");
        Mockito.when(mockUserService.editUser(Mockito.any(User.class))).thenReturn(returnedUser);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(returnedUser);
        Mockito.when(mockGardenService.getGardensByUserId(Mockito.anyLong())).thenReturn(null);
        Mockito.when(mockAuthenticationManager.authenticate(Mockito.any())).thenReturn(null);
    }

    @Given("I am on my user profile page")
    public void i_am_on_my_user_profile_page() throws Exception {
        resultActions = mockMvc.perform(get("/user/profile/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/profile"));
        path = Paths.get("src/main/resources/templates/user/profile.html");
    }

    @Given("I am on the edit profile page")
    public void i_am_on_the_edit_profile_page() throws Exception {
        resultActions = mockMvc.perform(get("/user/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/edit"));
        path = Paths.get("src/main/resources/templates/user/edit.html");
    }

    @When("I click the Edit Profile Picture button")
    public void i_click_the_edit_profile_picture_button() throws Exception {
        boolean containsButton = Files.lines(path)
                .anyMatch(line -> line.contains("id=\"profileImageEdit\""));
        Assertions.assertTrue(containsButton);
        Optional<String> button = Files.lines(path)
                .filter(line -> line.contains("id=\"profileImageEdit\""))
                .findFirst();
        Assertions.assertTrue(button.isPresent());
        this.button = button.get();

    }

    @Then("a file picker is shown")
    public void a_file_picker_is_shown() {
        Assertions.assertTrue(button.contains("type=\"file\""));
    }

    @Given("I choose a new valid profile picture")
    public void i_choose_a_new_valid_profile_picture() {
        path = Paths.get("src/test/resources/test-images/png_valid.png");
        Assertions.assertTrue(Files.exists(path));
    }

    @When("I submit the image")
    public void i_submit_the_image() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("rob@gmail.com");
        user.setFirstName("Rob");
        user.setLastName("Smith");
        String contentType = Files.probeContentType(path);
        byte[] image = Files.readAllBytes(path);
        MultipartFile multipartFile = new MockMultipartFile(path.getFileName().toString(),
                path.getFileName().toString(),
                contentType,
                image);
        user.setProfileImage(multipartFile);
        resultActions = mockMvc.perform(post("/user/edit")
                .flashAttr("user", user));
    }

    @Then("my profile picture is updated")
    public void my_profile_picture_is_updated() {
        Mockito.verify(mockUserService, Mockito.atLeastOnce()).editUser(Mockito.any(User.class));
    }

    @Given("I choose a new profile picture that is not png, jpg or svg")
    public void i_choose_a_new_profile_picture_that_is_not_png_jpg_or_svg() {
        path = Paths.get("src/test/resources/test-images/gif_valid.gif");
        Assertions.assertTrue(Files.exists(path));
    }

    @Then("an error message tells me {string}")
    public void an_error_message_tells_me(String error) {
        BindingResult result = (BindingResult) resultActions.andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user");
        Assertions.assertTrue(result.getFieldErrors().stream().anyMatch(fieldError -> fieldError.getDefaultMessage().equals(error)));
    }

    @Given("I choose a new profile picture that is larger than 10MB")
    public void i_choose_a_new_profile_picture_that_is_larger_than_10mb() {
        path = Paths.get("src/test/resources/test-images/jpg_too_big.jpg");
        Assertions.assertTrue(Files.exists(path));
    }

    @Then("I see my profile picture")
    public void i_see_my_profile_picture() throws Exception {
        boolean containsImage = Files.lines(path)
                .anyMatch(line -> line.contains("th:src=\"${user.getProfileImageBase64()}\""));
        Assertions.assertTrue(containsImage);
    }

    @Given("I have not uploaded a profile picture")
    public void i_have_not_uploaded_a_profile_picture() {
        returnedUser.setProfileImagePath("");
    }
    @Then("I see the default profile picture")
    public void i_see_the_default_profile_picture() throws Exception {
        boolean containsImage = Files.lines(path)
                .anyMatch(line -> line.contains("th:src=\"${profileImage}\""));
        Assertions.assertTrue(containsImage);
    }
}
