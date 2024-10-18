package nz.ac.canterbury.seng302.gardenersgrove.integration;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class RegistrationConfirmIntegrationTest {

    private static MockMvc mockMvc;
    private static UserService mockUserService;
    private static AuthenticationManager mockAuthenticationManager;
    private static VerificationTokenRepository mockVerificationTokenRepository;
    private static ApplicationEventPublisher mockEventPublisher;
    private static EmailService mockEmailService;
    private static PasswordEncoder spyPasswordEncoder;
    private static RegistrationListener spyRegistrationListener;
    private static VerificationToken mockVerificationToken;
    private static String tokenCode;
    private static String emailRecipient;
    private static EnvironmentUtils mockEnvironmentUtils;
    private static String emailTemplate;
    private static Context emailContext;

    private User user;


    @BeforeAll
    public static void setup() {
        mockUserService = Mockito.mock(UserService.class);
        mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        mockVerificationTokenRepository = Mockito.mock(VerificationTokenRepository.class);
        mockEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        mockEmailService = Mockito.mock(EmailService.class);
        mockEnvironmentUtils = Mockito.mock(EnvironmentUtils.class);
        spyPasswordEncoder = Mockito.spy(PasswordEncoder.class);
        spyRegistrationListener = Mockito.spy(RegistrationListener.class);
        spyRegistrationListener.setServices(mockEmailService, mockUserService, mockEnvironmentUtils);
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

    @Test
    public void newValidUser_submitsRegistration_redirectedAndConfirmationEmailIsSent() throws Exception {
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(null);

        mockMvc.perform(post("/auth/register")
                        .param("password", user.getPassword())
                        .param("passwordRepeat", user.getPasswordRepeat())
                        .flashAttr("user", user))
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/auth/registration-confirm"));

        verify(mockEventPublisher).publishEvent(any(OnRegistrationCompleteEvent.class));
        Assertions.assertEquals(user.getEmail(), emailRecipient);
    }

    @Test
    public void invalidUser_submitsRegistration_noConfirmationEmailIsSentAndErrorShown() throws Exception {
        String malformedEmailErrorMessage = "Email address must be in the form 'jane@doe.nz'";
        user = new User("malformedEmail", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");

        BindingResult result = (BindingResult) mockMvc.perform(post("/auth/register")
                        .param("password", user.getPassword())
                        .param("passwordRepeat", user.getPasswordRepeat())
                        .flashAttr("user", user))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "401"))
                .andExpect(view().name("auth/register"))
                .andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT

        boolean errorMessageFound = result.getFieldErrors("email")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(malformedEmailErrorMessage));
        Assertions.assertTrue(errorMessageFound);
        verify(mockEventPublisher, times(0)).publishEvent((OnRegistrationCompleteEvent.class));
    }

    @Test
    public void registeredUserHasNotConfirmedAccount_attemptsValidLogin_ErrorMessageDisplayed() throws Exception {
        String accountNotEnabledMessage = "Please confirm your email address";
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");

        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);

        BindingResult result = (BindingResult) mockMvc.perform(post("/auth/login")
                        .param("password", user.getPassword())
                        .flashAttr("user", user))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "401"))
                .andExpect(view().name("auth/login"))
                .andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT

        boolean errorMessageFound = result.getFieldErrors("password")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(accountNotEnabledMessage));
        Assertions.assertTrue(errorMessageFound);
    }

    @Test
    public void registeredUserHasConfirmedAccount_attemptsValidLogin_RedirectedToHome() throws Exception {
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");
        user.setEnabled(true);
        user.grantAuthority("ROLE_USER");

        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);

        mockMvc.perform(post("/auth/login")
                        .param("password", user.getPassword())
                        .flashAttr("user", user))
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/user/home"));
    }

    @Test
    public void registeredUser_confirmsAccountWithValidToken_redirectedToLogin() throws Exception {
        String successMessage = "Your account has been activated, please log in";
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");

        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockVerificationToken.hasExpired()).thenReturn(false);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);

        mockMvc.perform(post("/auth/registration-confirm")
                        .param("token", tokenCode))
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attributeExists("accountActivationSuccessMessage"))
                .andExpect(flash().attribute("accountActivationSuccessMessage", successMessage));

    }

    @Test
    public void registeredUser_confirmsAccountWithInvalidToken_errorMessageShown() throws Exception {
        String errorMessage = "Signup code invalid";
        String invalidTokenCode = "000000";
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");

        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockVerificationToken.hasExpired()).thenReturn(false);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);

        mockMvc.perform(post("/auth/registration-confirm")
                        .param("token", invalidTokenCode))
                .andExpect(status().is(400))
                .andExpect(view().name("auth/registrationConfirm"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", errorMessage));
    }

    @Test
    public void registeredUser_confirmsAccountWithExpiredToken_errorMessageShown() throws Exception {
        String errorMessage = "Signup code invalid";
        String invalidTokenCode = "000000";
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");

        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockVerificationToken.hasExpired()).thenReturn(true);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);

        mockMvc.perform(post("/auth/registration-confirm")
                        .param("token", invalidTokenCode))
                .andExpect(status().is(400))
                .andExpect(view().name("auth/registrationConfirm"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("message", errorMessage));
    }

    @Test
    public void registeredAndDisabledUserWithLivingToken_reRegisters_errorMessageShown() throws Exception {
        String checkEmailErrorMessage = "This email address is already in use";
        user = new User("test@email.com", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");

        Mockito.when(mockVerificationToken.getUser()).thenReturn(user);
        Mockito.when(mockVerificationToken.hasExpired()).thenReturn(false);
        Mockito.when(mockUserService.getUserByEmail(Mockito.anyString())).thenReturn(user);
        Mockito.when(mockVerificationTokenRepository.findByUser(Mockito.any(User.class))).thenReturn(mockVerificationToken);

        BindingResult result = (BindingResult) mockMvc.perform(post("/auth/register")
                        .param("password", user.getPassword())
                        .param("passwordRepeat", user.getPasswordRepeat())
                        .flashAttr("user", user))
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "401"))
                .andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT

        boolean errorMessageFound = result.getFieldErrors("email")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(checkEmailErrorMessage));
        Assertions.assertTrue(errorMessageFound);
        ;
    }
}
