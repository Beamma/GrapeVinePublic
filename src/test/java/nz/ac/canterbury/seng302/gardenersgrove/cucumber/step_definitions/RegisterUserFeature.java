package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AuthController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomeController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.UserController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegisterUserFeature {

    public static MockMvc homeMockMvc;

    public static MockMvc authMvc;

    public static MockMvc userMockMvc;

    private static UserService mockUserService;

    private static GardenService mockGardenService;

    private static UserAuthenticationService userAuthenticationService;

    private static UserAuthenticationService mockUserAuthenticationService2;
    private static AuthenticationManager mockAuthenticationManager;
    private static PasswordEncoder spyPasswordEncoder;
    private static VerificationTokenRepository mockVerificationTokenRepository;

    private static User user;
    private ResultActions response;

    private ResultActions request;

    @BeforeAll
    public static void before_or_after_all() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");

        InternalResourceViewResolver viewResolver2 = new InternalResourceViewResolver();
        viewResolver2.setPrefix("/WEB-INF/jsp/view/");
        viewResolver2.setSuffix(".jsp");

        mockUserService = Mockito.mock(UserService.class);
        Mockito.when(mockUserService.validateUserId(Mockito.any())).thenReturn(true);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);
        mockGardenService = Mockito.mock(GardenService.class);

        userAuthenticationService = new UserAuthenticationService(mockUserService, mockAuthenticationManager, spyPasswordEncoder, mockVerificationTokenRepository);
        mockUserAuthenticationService2 = Mockito.mock(UserAuthenticationService.class);

        homeMockMvc = MockMvcBuilders.standaloneSetup(new HomeController()).build();
        authMvc = MockMvcBuilders.standaloneSetup(new AuthController(userAuthenticationService)).setViewResolvers(viewResolver).build();
        userMockMvc = MockMvcBuilders.standaloneSetup(new UserController(mockUserAuthenticationService2, mockUserService, mockGardenService)).setViewResolvers(viewResolver2).build();

    }

    @Then("it includes a button labeled Register")
    public void it_includes_a_button_labeled_register() throws IOException {
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/landing.html"))
                .anyMatch(l -> l.contains("Register"));
        Assertions.assertTrue(containsPhrase);
    }


    @Given("I am on the registration page")
    public void i_am_on_the_registration_page() throws Exception {
        authMvc.perform(MockMvcRequestBuilders.get("/auth/register"))
                .andExpect(status().isOk());
    }

    @When("I fill in valid details of name {string} last name {string} email address {string} password {string} password repeat {string} and DOB {string}")
    public void i_fill_in_valid_details_of_name_last_name_email_address_password_password_repeat_and_DOB(String firstName, String lastName, String email, String password, String passwordRepeat, String birthday) {
        user = new User(email, birthday, firstName, lastName, false, password, passwordRepeat);
    }

    @When("I fill in valid details of name {string} email address {string} password {string} password repeat {string} and DOB {string}")
    public void i_fill_in_valid_details_of_name_last_name_email_address_password_password_repeat_and_DOB(String firstName, String email, String password, String passwordRepeat, String birthday) {
        user = new User(email, birthday, firstName, null, true, password, passwordRepeat);
    }

    @And("I click the sign up button")
    public void i_click_the_signup_button() {
        user.grantAuthority("ROLE_USER");
        user.setEnabled(true);
        User mockUser = Mockito.spy(user);
        Mockito.when(mockUser.getId()).thenReturn(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(mockUser);
    }

    @And("I am directed to the profile page")
    public void i_am_directed_to_the_page_user_profile() throws Exception {
        userMockMvc.perform(MockMvcRequestBuilders.get("/user/profile/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

    @Then("I am signed in")
    public void i_am_signed_in() {
        Assertions.assertTrue(mockUserService.getCurrentUser().getEnabled());
    }

    @And("I check the box for having no last name")
    public void i_check_the_box() throws Exception {
        //Mockito.when(userAuthenticationService.authenticateUserRegister(Mockito.any())).thenReturn(false);
        authMvc.perform(MockMvcRequestBuilders.post("/auth/register").param("noLastName", "true"));
    }

    @Then("The lastName field is disabled")
    public void the_last_name_field_is_disabled() {
        // Manual Test
    }

    @And("I am registered without a lastName after I click the Sign up button")
    public void i_am_registered_without_a_last_name_after_i_click_the_sign_up_button() {
        Assertions.assertTrue(mockUserService.getCurrentUser().getNoLastName());
        Assertions.assertNull(mockUserService.getCurrentUser().getLastName());
    }

    @When("I enter invalid input for {string} {string} {string} {string} {string} {string}")
    public void i_enter_invalid_input_for_last_name(String email, String firstName, String lastName, String dob, String password, String passwordRepeat) {
        user = new User(email, dob, firstName, lastName, false, password, passwordRepeat);
    }
    @When("I click the {string} button to submit the form")
    public void i_click_the_button_to_submit_the_form(String buttonName) throws Exception {
        user.grantAuthority("ROLE_USER");
        user.setEnabled(true);
        User mockUser = Mockito.spy(user);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(mockUser);
        Mockito.when(mockUser.getId()).thenReturn(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(mockUser);
        Mockito.when(mockUser.getEnabled()).thenReturn(true);

        response = authMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .param("password", user.getPassword())
                .param("passwordRepeat", user.getPasswordRepeat())
                .flashAttr("user", user));
    }

    @Then("I am shown an error {string} in the {string} field")
    public void i_am_shown_an_error_in_the_field(String errorMessage, String errorName) {
        BindingResult result = (BindingResult) response.andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT
        boolean errorMessageFound = result.getFieldErrors(errorName)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));
        Assertions.assertTrue(errorMessageFound);
    }

    @When("I click the Cancel Button")
    public void i_click_the_cancel_button() throws Exception {
        response = homeMockMvc.perform(MockMvcRequestBuilders.get("/"));
    }

    @Then("I am redirected to the home page")
    public void i_am_redirected_to_the_home_page() throws Exception {
        response.andExpect(status().isOk());
    }

    @When("I see the home page")
    public void iSeeTheHomePage() {
    }
}
