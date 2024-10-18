package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AuthController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.HomeController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ForgotPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ResetPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.ValidationException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static java.lang.reflect.Array.get;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = AuthController.class)
public class AuthControllerIntegrationTests {

    private MockMvc mockMvc;

    @MockBean
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpSession httpSession;

    @MockBean
    private SecurityContextHolder securityContextHolder;
    @MockBean
    private SecurityContext securityContext;
    @MockBean
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    private User user;
    private User user1;
    private HomeController homeController;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() throws ValidationException {

        userAuthenticationService = Mockito.mock(UserAuthenticationService.class);
        userService = Mockito.mock(UserService.class);
        securityContextHolder = Mockito.mock(SecurityContextHolder.class);

        // Set up a valid user to be used in tests
        user = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1 = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
        Mockito.when(userAuthenticationService.registerUser(Mockito.any(User.class))).thenReturn(user);

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");

        AuthController authController = new AuthController(userAuthenticationService);
        authController.setEventPublisher(eventPublisher);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

//    @WithMockUser()
//    @Test
//    public void GetAuthLoginPage_AuthenticatedUser_AccessGivenReturnLoginPage() throws Exception {
//
//        mockMvc.perform(MockMvcRequestBuilders
//                        .get("/auth/login"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(view().name("/auth/login"));
//    }

    @WithMockUser()
    @Test
    public void GetAuthRegisterPage_AuthenticatedUser_AccessGivenReturnRegisterPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/auth/register"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    public void PostRequestRegister_UserAttributesAuthenticated_ReturnProfile() throws Exception {

        user = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user.grantAuthority("ROLE_USER");

        String password = user.getPassword();
        String passwordRepeat = user.getPasswordRepeat();
        Authentication authentication = new UsernamePasswordAuthenticationToken("John@email.com", "Password1!", new ArrayList<>());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(userAuthenticationService.registerUser(Mockito.any())).thenReturn(user);
        Mockito.doAnswer(i -> i.getArgument(1)).when(userAuthenticationService).authenticateUserRegister(Mockito.any(User.class), Mockito.any(BindingResult.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .with(csrf()) // Include CSRF token
                        .param("password", password)
                        .param("passwordRepeat", passwordRepeat)
                        .flashAttr("user", user)) // Mocking user attribute
                .andExpect(redirectedUrl("/auth/registration-confirm"));
    }
}