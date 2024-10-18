package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.AuthController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.UserController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnPasswordChangeEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.PasswordChangeListener;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.repository.VerificationTokenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.thymeleaf.context.Context;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
public class UpdatePasswordIntegrationTest {
    private static MockMvc mockMvc;
    private static UserService mockUserService;
    private static GardenService mockGardenService;
    private static AuthenticationManager mockAuthenticationManager;
    private static ApplicationEventPublisher mockEventPublisher;
    private static EmailService mockEmailService;
    private static PasswordEncoder spyPasswordEncoder;
    private static PasswordChangeListener spyRegistrationListener;
    private static VerificationTokenRepository mockVerificationTokenRepository;
    private static String emailRecipient;
    private static String emailTemplate;
    private static Context emailContext;

    private User user;


    @BeforeAll
    public static void setup() {
        mockUserService = Mockito.mock(UserService.class);
        mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        mockEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        mockEmailService = Mockito.mock(EmailService.class);
        spyPasswordEncoder = Mockito.spy(PasswordEncoder.class);
        spyRegistrationListener = Mockito.spy(PasswordChangeListener.class);
        spyRegistrationListener.setServices(mockEmailService, mockUserService);
        mockGardenService = Mockito.mock(GardenService.class);

        UserAuthenticationService userAuthenticationService = new UserAuthenticationService(mockUserService, mockAuthenticationManager, spyPasswordEncoder, mockVerificationTokenRepository);
        UserController userController = new UserController(userAuthenticationService, mockUserService, mockGardenService);
        userController.setEventPublisher(mockEventPublisher);

        // Method mocking
        Mockito.doAnswer(invocation -> {
            emailRecipient = invocation.getArgument(0);
            emailTemplate = invocation.getArgument(2);
            emailContext = invocation.getArgument(3);
            return null;
        }).when(mockEmailService).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Context.class));

        Mockito.doAnswer(invocation -> {    // ChatGPT generated from my incorrect code
            OnPasswordChangeEvent event = invocation.getArgument(0);
            spyRegistrationListener.onApplicationEvent(event);
            return null;
        }).when(mockEventPublisher).publishEvent(Mockito.any(OnPasswordChangeEvent.class));

        //Mockito.when(spyPasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    public void OldPasswordDifferentToCurrentPassword_submitsForm_NoUpdatesAndErrorShown() throws Exception {
        user = new User("malformedEmail", "2000-01-01", "Jack", "Reacher", false, "Password1!", "Password1!");
        user.grantAuthority("ROLE_USER");

        String oldPassword = "wrongpassword";
        String password = "password";
        String passwordRepeat = "passwordRepeat";
        String nonMatchPasswordErrorMessage = "Your old password is incorrect";
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(mockUserService.editUserPassword(Mockito.any())).thenReturn(user);
        Mockito.when(spyPasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        BindingResult result = (BindingResult) mockMvc.perform(post("/user/editPassword")
                        .param("oldPassword", oldPassword)
                        .param("password", password)
                        .param("passwordRepeat", passwordRepeat)
                        .flashAttr("user", user))
                .andExpect(model().attributeHasFieldErrorCode("user", "oldPassword", "401"))
                .andExpect(view().name("user/editPassword"))
                .andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT

        boolean errorMessageFound = result.getFieldErrors("oldPassword")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(nonMatchPasswordErrorMessage));
        Assertions.assertTrue(errorMessageFound);
        verify(mockEventPublisher, times(0)).publishEvent((OnRegistrationCompleteEvent.class));
    }

    @Test
    public void oldPasswordValid_NewPasswordFieldsNotSame_submitsForm_noUpdatesAndErrorShown() throws Exception {
        user = new User("malformedEmail", "2000-01-01", "Jack", "Reacher", false, "Password2!", "Password1!");
        user.grantAuthority("ROLE_USER");

        String oldPassword = "Password1!";
        String nonMatchPasswordErrorMessage = "The new passwords do not match";
        String password = "Password2!";
        String passwordRepeat = "passwordRepeat";
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(mockUserService.editUserPassword(Mockito.any())).thenReturn(user);
        Mockito.when(spyPasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        BindingResult result = (BindingResult) mockMvc.perform(post("/user/editPassword")
                        .param("oldPassword", oldPassword)
                        .param("password", password)
                        .param("passwordRepeat", passwordRepeat)
                        .flashAttr("user", user))
                .andExpect(model().attributeHasFieldErrorCode("user", "passwordRepeat", "401"))
                .andExpect(view().name("user/editPassword"))
                .andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT

        boolean errorMessageFound = result.getFieldErrors("passwordRepeat")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(nonMatchPasswordErrorMessage));
        Assertions.assertTrue(errorMessageFound);
        verify(mockEventPublisher, times(0)).publishEvent((OnRegistrationCompleteEvent.class));
    }

    @Test
    public void oldPasswordValid_newPasswordsMatch_newPasswordIsWeak_submitsForm_noUpdatesAndErrorShown() throws Exception {
        user = new User("malformedEmail", "2000-01-01", "Jack", "Reacher", false, "weakpassword", "weakpassword");
        user.grantAuthority("ROLE_USER");

        String oldPassword = "Password1!";
        String nonMatchPasswordErrorMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";
        String password = "password";
        String passwordRepeat = "passwordRepeat";
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(mockUserService.editUserPassword(Mockito.any())).thenReturn(user);
        Mockito.when(spyPasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        BindingResult result = (BindingResult) mockMvc.perform(post("/user/editPassword")
                        .param("oldPassword", oldPassword)
                        .param("password", password)
                        .param("passwordRepeat", passwordRepeat)
                        .flashAttr("user", user))
                .andExpect(model().attributeHasFieldErrorCode("user", "password", "401"))
                .andExpect(view().name("user/editPassword"))
                .andReturn().getModelAndView().getModel().get("org.springframework.validation.BindingResult.user"); // This line from chatGPT

        boolean errorMessageFound = result.getFieldErrors("password")
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(nonMatchPasswordErrorMessage));
        Assertions.assertTrue(errorMessageFound);
        verify(mockEventPublisher, times(0)).publishEvent((OnRegistrationCompleteEvent.class));
    }

    @Test
    public void oldPasswordNewPasswordValid_submitsForm_passwordUpdatedAndEmailSent() throws Exception{
        String oldPassword = "OldPassword1!";
        User requestingChangeUser = new User("test@email.com", "2000-01-01", "Joe", "Doe", false, "NewPassword1!", "NewPassword1!");
        requestingChangeUser.grantAuthority("ROLE_USER");

        String password = "Password1!";
        String passwordRepeat = "Password1!";
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(requestingChangeUser);
        Mockito.when(spyPasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockUserService.editUserPassword(Mockito.any())).thenReturn(requestingChangeUser);

        mockMvc.perform(post("/user/editPassword")
                        .param("oldPassword", oldPassword)
                        .param("password", password)
                        .param("passwordRepeat", passwordRepeat)
                        .flashAttr("user", requestingChangeUser))
                .andExpect(redirectedUrl("/user/profile/null"));

        verify(mockEventPublisher).publishEvent(any(OnPasswordChangeEvent.class));
        Assertions.assertEquals(requestingChangeUser.getEmail(), emailRecipient);
    }
}
