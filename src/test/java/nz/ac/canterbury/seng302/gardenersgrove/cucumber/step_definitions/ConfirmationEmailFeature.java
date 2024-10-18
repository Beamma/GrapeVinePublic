package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AuthController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.EnvironmentUtils;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ConfirmationEmailFeature {

    private User user;
    private static String tokenCode;
    private static String emailRecipient;
    private static String emailTemplate;
    private static Context emailContext;
    private static MockMvc mockMvc;
    private static UserService mockUserService;
    private static AuthenticationManager mockAuthenticationManager;
    private static VerificationTokenRepository mockVerificationTokenRepository;
    private static PasswordEncoder spyPasswordEncoder;
    private static ApplicationEventPublisher mockEventPublisher;
    private static EmailService mockEmailService;
    private static EnvironmentUtils mockEnvironmentUtils;
    private static RegistrationListener spyRegistrationListener;
    private static VerificationToken mockVerificationToken;


    private ResultActions response;

    @Before
    public static void before_or_after_all() {
        mockUserService = Mockito.mock(UserService.class);
        mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        mockVerificationTokenRepository = Mockito.mock(VerificationTokenRepository.class);
        mockEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        mockEmailService = Mockito.mock(EmailService.class);
        mockEnvironmentUtils = Mockito.mock(EnvironmentUtils.class);
        spyPasswordEncoder = Mockito.spy(PasswordEncoder.class);
        spyRegistrationListener = Mockito.spy(RegistrationListener.class);
        spyRegistrationListener.setServices(mockEmailService,mockUserService, mockEnvironmentUtils);
        mockVerificationToken = Mockito.mock(VerificationToken.class);


        UserAuthenticationService userAuthenticationService = new UserAuthenticationService(mockUserService, mockAuthenticationManager, spyPasswordEncoder, mockVerificationTokenRepository);
        AuthController authController = new AuthController(userAuthenticationService);
        authController.setEventPublisher(mockEventPublisher);
        authController.setUserService(mockUserService);

        tokenCode = "123456";

        // Method mocking
        Mockito.when(spyRegistrationListener.getRandomNumberString()).thenReturn(tokenCode);

        Mockito.when(mockEnvironmentUtils.getBaseUrl()).thenReturn("https://localhost:8080");

        Mockito.doAnswer(invocation -> {
            emailRecipient = invocation.getArgument(0);
            emailTemplate = invocation.getArgument(2);
            emailContext = invocation.getArgument(3);
            return null;
        }).when(mockEmailService).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Context.class));



        Mockito.doAnswer(invocation -> {    // ChatGPT generated from my incorrect code
            OnRegistrationCompleteEvent event = invocation.getArgument(0);
            spyRegistrationListener.onApplicationEvent(event);
            return null;
        }).when(mockEventPublisher).publishEvent(Mockito.any(OnRegistrationCompleteEvent.class));

        Mockito.when(mockUserService.getVerificationToken(tokenCode)).thenReturn(mockVerificationToken);

        Mockito.when(spyPasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);


        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Given("I fill in a valid registration form with the name {string} {string}, dob {string}, email {string} and password {string}")
    public void i_fill_in_a_valid_registration_form_with_the_name_dob_email_and_password(String firstName, String lastName, String dob, String email, String password) {
        user = new User(email, dob, firstName, lastName, false, password, password);
    }

    @When("I click the register button")
    public void i_click_the_register_button() throws Exception {
        response = mockMvc.perform(post("/auth/register")
                .param("password", user.getPassword())
                .param("passwordRepeat", user.getPasswordRepeat())
                .flashAttr("user", user));
    }

    @Then("An email is sent to my address, {string}")
    public void an_email_is_sent_to_my_address(String emailAddress) {
        verify(mockEventPublisher).publishEvent(any(OnRegistrationCompleteEvent.class));
        Assertions.assertEquals(emailAddress, emailRecipient);
    }

    @Then("The email includes the token generated for my account confirmation")
    public void the_email_includes_the_token_generated_for_my_account_confirmation() {
        Assertions.assertTrue(emailContext.containsVariable("token"));
        Assertions.assertEquals(tokenCode, emailContext.getVariable("token"));
    }

    @Then("I am redirected to {string}")
    public void i_am_redirected_to(String path) throws Exception {
        response.andExpect(status().is(302))
                .andExpect(redirectedUrl(path));
    }

    @Given("A sign-up code has been generated for a user")
    public void a_sign_up_code_has_been_generated_for_a_user() {
        user = new User("test@email.com", "2000-10-10", "Inigo", "Montoya", false, "Password1!", "Password1!");
        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockVerificationToken.hasExpired()).thenReturn(false);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);
    }

    @When("The code has expired")
    public void the_code_has_expired() {
        Mockito.when(mockVerificationToken.hasExpired()).thenReturn(true);
    }

    @When("I try to use the sign-up code")
    public void i_try_to_use_the_sign_up_code() throws Exception {
        response = mockMvc.perform(post("/auth/registration-confirm")
                .param("token", tokenCode));
    }

    @Then("An error message {string} is displayed")
    public void an_error_message_is_displayed(String errorMessage) throws Exception {
        response.andExpect(status().is(400))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", errorMessage));
    }

    @When("I try to log in")
    public void i_try_to_log_in() throws Exception {
        response = mockMvc.perform(post("/auth/login")
                .param("password", user.getPassword())
                .flashAttr("user", user));
    }

    @Then("An error {string} is displayed")
    public void an_error_is_displayed(String errorMessage) throws Exception {
        response.andExpect(model().attributeHasFieldErrorCode("user", "password", "401"));

        BindingResult result = (BindingResult) response.andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT
        boolean errorMessageFound = result.getFieldErrors("password")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));
        Assertions.assertTrue(errorMessageFound);
    }

    @Then("There is a message that says {string}")
    public void there_is_a_message_that_says(String message) throws Exception {
        response.andExpect(flash().attributeExists("accountActivationSuccessMessage"))
                .andExpect(flash().attribute("accountActivationSuccessMessage", message));
    }

    @Given("The account has been verified")
    public void the_account_has_been_verified() {
        user.setEnabled(true);
        user.grantAuthority("ROLE_USER");
    }

    @When("An email is wrongly sent to my address, {string}")
    public void an_email_is_wrongly_sent_to_my_address(String emailAddress) {
        mockEventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, Locale.ENGLISH, "AppURL"));
        Assertions.assertEquals(emailAddress, emailRecipient);
    }

    @Then("The email contains the phrase {string}")
    public void the_email_contains_the_phrase(String phrase) throws IOException {
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/emails/email-template.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);

    }
}
