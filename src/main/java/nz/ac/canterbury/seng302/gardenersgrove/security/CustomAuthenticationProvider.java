package nz.ac.canterbury.seng302.gardenersgrove.security;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Custom Authentication Provider class, to allow for handling authentication in any way we see fit.
 * In this case using our existing {@link User}
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    @Autowired
    public CustomAuthenticationProvider(@Lazy UserService userService) {
        super();
        this.userService = userService;
    }

    /**
     * Custom authentication implementation
     * @param authentication An implementation object that must have non-empty email (name) and password (credentials)
     * @return A new {@link UsernamePasswordAuthenticationToken} if email and password are valid with users authorities
     */
    @Override
    public Authentication authenticate(Authentication authentication) {

        // Get the user from the authentication
        User user = userService.getUserByEmail(authentication.getName());

        // Return the authentication
        return new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.getAuthorities());
    }

    /**
     * @param authentication parameter to check the class of
     * @return true if authentication is an instance of UsernamePasswordAuthenticationToken class
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
