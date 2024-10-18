package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AuthController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomeController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LogInFeature {
    private static MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;
    private ResultActions request;
    private static UserService userService;
    private static AuthenticationManager mockAuthenticationManager;
    private static PasswordEncoder mockPasswordEncoder;
    private static VerificationTokenRepository mockVerificationTokenRepository;
    private static UserRepository mockUserRepository;
    private static User john;

    @Before
    public static void setUp() {

        // Set up a valid user
        User jane = new User();
        jane.setEmail("jane@email.com");
        jane.setPassword("Password1!");
        jane.setEnabled(true);
        jane.grantAuthority("ROLE_USER");

        // Set up a blocked user
        john = new User();
        john.setEmail("john@email.com");
        john.setPassword("Password1!");
        john.grantAuthority("ROLE_USER");
        john.setEnabled(true);
        john.setBlocked(true);
        john.setBlockedEndDate(LocalDateTime.now().plusDays(7));

        // Create all neccesary mocks for
        mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        mockPasswordEncoder = Mockito.mock(PasswordEncoder.class);
        Mockito.when(mockPasswordEncoder.matches("Password1!", "Password1!")).thenReturn(true);
        mockVerificationTokenRepository = Mockito.mock(VerificationTokenRepository.class);
        mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.findByEmail("jane@email.com")).thenReturn(Optional.of(jane));
        Mockito.when(mockUserRepository.findByEmail("john@email.com")).thenReturn(Optional.of(john));
        Mockito.when(mockUserRepository.findByEmail("fake@email.com")).thenReturn(Optional.empty());

        userService = new UserService(mockUserRepository, mockPasswordEncoder);

        UserAuthenticationService userAuthService = new UserAuthenticationService(userService, mockAuthenticationManager, mockPasswordEncoder, mockVerificationTokenRepository);
        AuthController authController = new AuthController(userAuthService);

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Given("I connect to the system's main URL")
    public void i_connect_to_the_systems_main_url() throws Exception {
        HomeController homeController = new HomeController();
        MockMvc mockMvc2 = MockMvcBuilders.standaloneSetup(homeController)
                .build();
        request = mockMvc2.perform(MockMvcRequestBuilders.get("/"));
    }


    @Then("it includes a button labelled {string}")
    public void it_includes_a_button_labelled(String phrase) throws IOException  {
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/landing.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);
    }

    @Given("I am on the login form")
    public void i_am_on_the_login_form() throws Exception {
        requestBuilder = MockMvcRequestBuilders.post("/auth/login");
    }

    @And("I enter and email address: {string} and its corresponding password: {string} for an account that exists on the system")
    public void i_enter_an_email_address_and_its_corresponding_password_for_an_account_that_exists_on_the_system(String email, String password) {
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);
        requestBuilder
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("user", mockUser)
                .param("password", password);
    }

    @And("the account has been blocked for {int} days")
    public void the_account_has_been_blocked(Integer days) {
        john.setBlocked(true);
        john.setBlockedEndDate(LocalDateTime.now().plusDays(days - 1).plusHours(23).plusMinutes(59));
    }

    @When("I click the “Sign In” button")
    public void i_click_the_sign_in_button() throws Exception {
        request = mockMvc.perform(requestBuilder);
    }

    @Then("I am taken to the main page of the application")
    public void iAmTakenToTheMainPageOfTheApplication() throws Exception {
        request.andExpectAll(
                status().is(302),
                redirectedUrl("/user/home")
        );

    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/auth/login"));
        request.andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Then("it contains the text {string} which is highlighted as a link.")
    public void itContainsTheTextNotRegisteredCreateAnAccountWhichIsHighlightedAsALink(String phrase) throws IOException {
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/auth/login.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);
    }

    @When("I click the {string} link")
    public void i_click_the_not_registered_create_an_account_link(String phrase) throws IOException {
        Path path = Paths.get("src/main/resources/templates/auth/login.html");
        boolean containsPhrase = Files.lines(path)
                .anyMatch(l -> l.contains(phrase));
        boolean containsLink = Files.lines(path)
                .anyMatch(l -> l.contains("th:href=\"@{/auth/register}\""));
        Assertions.assertTrue(containsPhrase);
        Assertions.assertTrue(containsLink);
    }
    @Then("I am taken to the registration page.")
    public void i_am_taken_to_the_registration_page() throws Exception {
        request = mockMvc.perform(MockMvcRequestBuilders.get("/auth/register"));
        request.andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @And("I enter a malformed email {string} address or empty email address")
    public void i_enter_a_malformed_email_address_or_empty_email_address(String email) {
        String password = "Password1!";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);
        requestBuilder
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("user", mockUser)
                .param("password", password);
    }
    @When("I hit the login button")
    public void i_hit_the_login_button() throws Exception {
        request = mockMvc.perform(requestBuilder);
    }
    @Then("an {string} error message tells me {string}.")
    public void an_error_message_tells_me_email_address_must_be_in_the_form_jane_doe_nz(String errorName, String phrase) throws Exception {
        BindingResult result = (BindingResult) request.andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT
        boolean errorMessageFound = result.getFieldErrors(errorName)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(phrase));
        Assertions.assertTrue(errorMessageFound);
    }

    @And("I enter an email address that is unknown to the system {string}")
    public void iEnterAnEmailAddressThatIsUnknownToTheSystem(String email) {
        String password = "Password1!";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);
        requestBuilder
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("user", mockUser)
                .param("password", password);
    }

    @And("I enter a {string} for the corresponding email address {string}")
    public void i_enter_a_wrong_password(String password, String email) {
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);
        requestBuilder
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("user", mockUser)
                .param("password", password);
    }

    @When("I click the “Cancel” button")
    public void iClickTheCancelButton() throws IOException {
        Path path = Paths.get("src/main/resources/templates/auth/login.html");
        boolean containsPhrase = Files.lines(path)
                .anyMatch(l -> l.contains("Cancel"));
        boolean containsLink = Files.lines(path)
                .anyMatch(l -> l.contains("th:href=\"@{/}\""));
        Assertions.assertTrue(containsPhrase);
        Assertions.assertTrue(containsLink);
    }

    @Then("I am taken back to the system’s home page")
    public void iAmTakenBackToTheSystemSHomePage() throws Exception {
        HomeController homeController = new HomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .build();
        request = mockMvc.perform(MockMvcRequestBuilders.get("/"));
        request.andExpect(status().isOk())
                .andExpect(view().name("landing"));
    }
}
