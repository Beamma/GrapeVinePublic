package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import nz.ac.canterbury.seng302.gardenersgrove.security.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class Background {

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Given("A user with email {string} and password {string} exists")
    public void create_user(String email, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = customAuthenticationProvider.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}