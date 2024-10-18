package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The controller for the home directory (/)
 */
@Controller
public class HomeController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserService userService;

    /**
     * Controller for the home page
     * @return Page depending on authentication
     */
    @GetMapping("/")
    public String showHomePage() {

        // Log home page
        logger.info("GET /");

        // Get the users authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("authentication at /:", authentication);
        // Check if they are authenticated
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // User is authenticated, redirect to the profile page

            return "redirect:user/profile/" + userService.getCurrentUser().getId();
        }

        // User is not authenticated, show the landing page
        return "landing";
    }
}
