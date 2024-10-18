package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.HomeController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.ArrayList;

import static java.lang.reflect.Array.get;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HomeController.class)
public class HomeControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    UserService mockUserService;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private AuthenticationManager authenticationManager;

    private User user;

    @BeforeEach
    public void setUp() {

        // Set up the security context
        SecurityContextHolder.setContext(securityContext);

        // Set up an anonymous authentication
        Authentication anonymousToken = new AnonymousAuthenticationToken("anonymousUser", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        // Set the authentication
        SecurityContextHolder.getContext().setAuthentication(anonymousToken);

        // Set up a valid user to be used in tests
        user = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
    }

    @Test
    public void AccessesHome_UnauthenticatedUser_ReturnUnauthorizedRequest() throws Exception {

        // verify HTTP request matching
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void AccessHome_UnauthenticatedUser_ReturnIsUnauthorized() throws Exception {

        SecurityContextHolder.setContext(securityContext);
        // mock login user
        Authentication authentication = new UsernamePasswordAuthenticationToken("jane@doe.nz", null, new ArrayList<>());
        when(securityContext.getAuthentication()).thenReturn(null);

        User user = new User("John@email.com", "2000-01-01", "John", "Doe", false, "Password1!", "Password1!");
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
}