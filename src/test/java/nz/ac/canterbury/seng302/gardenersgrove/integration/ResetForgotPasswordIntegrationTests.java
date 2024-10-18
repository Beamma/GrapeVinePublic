package nz.ac.canterbury.seng302.gardenersgrove.integration;

import static org.hamcrest.Matchers.containsString;

import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ForgotPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ResetPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.ForgotPasswordListener;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnForgotPasswordEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.RegistrationListener;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ResetForgotPasswordIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    // Create spy classes (For classes where methods should be run and checked)

    @SpyBean
    private UserAuthenticationService userAuthenticationService;

    // Create mocked classes (For classes run by program but wanting to be ignored)

    @MockBean
    private GardenController gardenController;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RegistrationListener registrationListener;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private SecurityContextHolder securityContextHolder;

    @MockBean
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private ForgotPasswordListener forgotPasswordListener;

    @MockBean
    private OnForgotPasswordEvent onForgotPasswordEvent;

    @MockBean
    private WeatherService weatherService;

    // Set test variables

    private ForgotPasswordDTO validForgotPasswordDTO;

    private ForgotPasswordDTO invalidForgotPasswordDTO;

    private ResetPasswordDTO validResetPasswordDTO;

    private ResetPasswordDTO invalidResetPasswordDTO;

    @BeforeEach
    public void before_each() {

        // Create a valid forgot password DTO
        validForgotPasswordDTO = new ForgotPasswordDTO();
        validForgotPasswordDTO.setEmail("Valid@Email.com");

        // Create an invalid forgot password DTO
        invalidForgotPasswordDTO = new ForgotPasswordDTO();
        invalidForgotPasswordDTO.setEmail("InvalidEmail.com");

        // Create a valid reset password DTO
        validResetPasswordDTO = new ResetPasswordDTO();
        validResetPasswordDTO.setToken("AToken");
        validResetPasswordDTO.setPassword("Password2!");
        validResetPasswordDTO.setPasswordRepeat("Password2!");

        // Create a invalid reset password DTO
        invalidResetPasswordDTO = new ResetPasswordDTO();
        invalidResetPasswordDTO.setToken("NotAToken");

        // Create user with valid token time
        User validUser = Mockito.mock(User.class);
        Mockito.when(validUser.getEmail()).thenReturn("Valid@Email.com");
        Mockito.when(validUser.getPassword()).thenReturn("Password1!");
        Mockito.when(validUser.getTokenExpiry()).thenReturn(LocalDateTime.now().plusMinutes(10));

        // Set up mocks for user service
        Mockito.when(userService.getUserByToken(validResetPasswordDTO.getToken())).thenReturn(validUser);
        Mockito.when(userService.resetPassword(validResetPasswordDTO)).thenReturn(validUser);

        Mockito.doNothing().when(applicationEventPublisher).publishEvent(Mockito.any());
    }

    @Test
    public void GetForgotPassword_AuthenticatedUser_ReturnForgotPasswordPage() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/auth/forgotPassword"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("auth/forgotPassword"));
    }

    @Test
    public void PostForgotPassword_ValidEmail_ReturnPageWithConfirmation() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/forgotPassword")
                        .with(csrf())
                        .flashAttr("forgotPasswordDTO", validForgotPasswordDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgotPassword"))
                .andExpect(content().string(containsString("An email was sent to the address if it was recognised.")));

        // Verify authenticate forgot password called
        Mockito.verify(userAuthenticationService).authenticateForgotPassword(validForgotPasswordDTO);

        // Verify forgot password called
        Mockito.verify(userService).forgotPassword(validForgotPasswordDTO);
    }

    @Test
    public void PostForgotPassword_InvalidEmail_ReturnPageWithErrors() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/forgotPassword")
                        .with(csrf())
                        .flashAttr("forgotPasswordDTO", invalidForgotPasswordDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgotPassword"))
                .andExpect(content().string(containsString("Email address must be in the form &#39;jane@doe.nz&#39;")));

        // Verify authenticate forgot password called
        Mockito.verify(userAuthenticationService).authenticateForgotPassword(invalidForgotPasswordDTO);

        // Verify forgot password not called
        Mockito.verify(userService, Mockito.times(0)).forgotPassword(invalidForgotPasswordDTO);
    }

    @Test
    public void GetResetPassword_ValidToken_ReturnResetPasswordPage() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/auth/resetPassword")
                        .param("token", validResetPasswordDTO.getToken()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("auth/resetPassword"));
    }

    @Test
    public void GetResetPassword_InvalidToken_RedirectLoginPage() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/auth/resetPassword")
                        .param("token", "invalidToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("auth/login"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    public void PostResetPassword_InvalidToken_ReturnResetPasswordPage() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/resetPassword")
                        .with(csrf())
                        .flashAttr("resetPasswordDTO", invalidResetPasswordDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/resetPassword"))
                .andExpect(content().string(containsString("The token is invalid or expired")));

        // Verify authenticate reset password called
        Mockito.verify(userAuthenticationService).authenticateResetPassword(invalidResetPasswordDTO);

        // Verify that password is not reset
        Mockito.verify(userService, Mockito.times(0)).resetPassword(invalidResetPasswordDTO);
    }

    @Test
    public void PostResetPassword_Valid_RedirectLoginPage() throws Exception {
        // Mock the request and check values
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/resetPassword")
                        .with(csrf())
                        .flashAttr("resetPasswordDTO", validResetPasswordDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        // Verify authenticate reset password called
        Mockito.verify(userAuthenticationService).authenticateResetPassword(validResetPasswordDTO);

        // Verify reset password called
        Mockito.verify(userService).resetPassword(validResetPasswordDTO);
    }
}