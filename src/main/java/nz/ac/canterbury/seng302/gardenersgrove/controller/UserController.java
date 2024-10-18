package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnPasswordChangeEvent;
import nz.ac.canterbury.seng302.gardenersgrove.exception.ValidationException;
import nz.ac.canterbury.seng302.gardenersgrove.exception.BadRequestException;
import nz.ac.canterbury.seng302.gardenersgrove.service.AutocompleteService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.UserImageStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

/**
 * The controller for the user. Contains all use pages.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    UserAuthenticationService userAuthenticationService;

    @Autowired
    private AutocompleteService autocompleteService;

    private final GardenService gardenService;

    private static final String NAV_BAR_USER = "navBarUser";

    /**
     * The constructor for the user controller
     * @param userAuthenticationService for authenticating users
     * @param userService for getting current user
     */
    @Autowired
    public UserController(UserAuthenticationService userAuthenticationService, UserService userService, GardenService gardenService) {
        this.userAuthenticationService = userAuthenticationService;
        this.gardenService = gardenService;
        this.userService = userService;
        userService.setStorageProperties(new UserImageStorageProperties());
    }

    /**
     * Setter for the event publisher.
     * FOR TESTING ONLY
     * @param publisher the publisher being set
     */
    public void setEventPublisher(ApplicationEventPublisher publisher) {
        this.eventPublisher = publisher;
    }

    /**
     * User can access their own profile page
     * @param model user data gets added to the model
     * @return users profile page
     */
    @GetMapping("/profile/{userId}")
    public String userProfile(@PathVariable("userId") String userId, Model model) {

        // Log profile page
        logger.info("GET /profile/id");

        //Check userId is valid
        if (!userService.validateUserId(userId)) {
            model.addAttribute("status", 404);
            model.addAttribute("error", "Page not found, not a valid user Id");
            return "error";
        }


        // Logic to retrieve user profile information
        User user = userService.getCurrentUser();
        userService.clearUserImageCache(user.getId().toString());

        // check if user exists
        if (user == null) {
            model.addAttribute("status", 403);
            model.addAttribute("error", "You are not friends with this user");
            return "error";
        }

        // Add user data to the model
        model.addAttribute("user", user);
        try {
            model.addAttribute("profileImage", user.getProfileImageBase64());
        } catch (IOException e) {
            logger.error("Error loading file" + e.getMessage());
            throw new RuntimeException(e);
        }
        if (userService.getCurrentUser().getId().toString().equals(userId)) {
            model.addAttribute("owner", true);
        } else {
            model.addAttribute("owner", false);
        }

        try {
            User profileUser =  userService.getUserProfile(userId);
            // Add user data to the model
            model.addAttribute("user", profileUser);

            // Get gardens for the navbar
            model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
            model.addAttribute("userId", userService.getCurrentUser().getId());
            model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());

            // Return the profile page
            return "user/profile";
        } catch (BadRequestException e) { // BadRequestException thrown from getUserProfile(), for user not found / not friends
            model.addAttribute("status", 403);
            model.addAttribute("error", "You are not friends with this user");
            model.addAttribute("user", userService.getById(userId));
            return "addFriendSuggest";
        }

    }

    /**
     * User can edit their profile picture
     * @param user the user to edit picture of
     * @param bindingResult to store errors
     * @param redirectAttributes  to pass attributes to next page
     * @param model user data, photo image
     * @return /user/profile page
     */
    @PostMapping("/profile/{userId}")
    public String userProfile(HttpServletRequest request,
                              @ModelAttribute("user") User user,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model, @PathVariable String userId) {

        // Log post request
        logger.info("POST /user/profile/" + userId);


        // Get the user to update from the user service, the Model Attribute user has null variables
        User currentUser = userService.getCurrentUser();

        // Set all values of user to be the values of the current user to show on profile page
        user.setEmail(currentUser.getEmail());
        user.setDob(currentUser.getDob());
        user.setFirstName(currentUser.getFirstName());
        user.setLastName(currentUser.getLastName());

        // Try to authenticate the user edit
        try {
            userAuthenticationService.authenticateUserEditProfileImage(user);
        } catch (ValidationException e) {
            model.addAttribute("gardens", gardenService.getGardens());
            redirectAttributes.addFlashAttribute("imageError", e.getMessage());
            model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
            model.addAttribute("user", currentUser);
            model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());

            return "redirect:/user/profile/" + currentUser.getId();
        }

        // Get gardens for the navbar
        redirectAttributes.addFlashAttribute("gardens", gardenService.getGardens());
        model.addAttribute("user", currentUser);
        // Reset the request security
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        if (user.getProfileImage().getOriginalFilename() != "") {
            redirectAttributes.addFlashAttribute("profileSuccess", "Image Success");
        }
        // Redirect to profile page
        return "redirect:/user/profile/" + currentUser.getId();
    }


    /**
     * Gardens Grove home page
     * @param model user data gets added to the model
     * @return /user/home page
     */
    @GetMapping("/home")
    public String userHome(Model model) {

        // Log home page
        logger.info("GET /user/home");

        // Logic to retrieve user profile information
        User user = userService.getCurrentUser();

        // Add user data to the model
        model.addAttribute("user", user);

        // Get gardens for the navbar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());



        // Return the home page
        return "user/home";
    }

    /**
     * Users can edit their own profile page
     * @param model user data
     * @return edit page
     */
    @GetMapping("/edit")
    public String userEdit(Model model) {

        // Log home page
        logger.info("GET /user/edit");

        // Logic to retrieve user profile information
        User user = userService.getCurrentUser();
        userService.clearUserImageCache(user.getId().toString());

        // Add user data to the model
        model.addAttribute("user", user);
        try {
            model.addAttribute("profileImage", user.getProfileImageBase64());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get gardens for the navbar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());


        // Return the profile page
        return "user/edit";
    }

    /**
     * User can submit their edited profile if authenticated user
     * @param user must be valid
     * @param bindingResult to store errors
     * @return page depending on if user is authorised to make changes
     */
    @PostMapping("/edit")
    public String userEdit(HttpServletRequest request,
                           @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // Log post request
        logger.info("POST user/edit");

        // Try to authenticate the user edit
        bindingResult  = userAuthenticationService.authenticateUserEdit(user, bindingResult);

        String profileImageBase64 = "";

        if (bindingResult.hasErrors()) {
            boolean hasImageError = false;
            if (bindingResult.hasFieldErrors("profileImage")) {
                hasImageError = true;
            }
            model.addAttribute("gardens", gardenService.getGardens());
            // pass through image on errored post redirect
            User currentUser = userService.getCurrentUser();

            if (!hasImageError && user.getProfileImage().getSize() != 0) {
                userService.cacheUserImage(user.getProfileImage(), currentUser.getId().toString());
                try {
                    profileImageBase64 = userService.getCachedUserImageBase64(currentUser.getId().toString());
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            } else {
                if (userService.cachedUserImageExists(currentUser.getId().toString())) {
                    try {
                        profileImageBase64 = userService.getCachedUserImageBase64(currentUser.getId().toString());
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                } else if (currentUser.getProfileImagePath() != null) {
                    try {
                        profileImageBase64 = currentUser.getProfileImageBase64();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
            model.addAttribute("profileImage", profileImageBase64);
            model.addAttribute("userId", userService.getCurrentUser().getId());
            model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
            model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());
            return "user/edit";
        }
        userAuthenticationService.editUser(user);

        // Get attributes for the navbar
        redirectAttributes.addFlashAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        redirectAttributes.addFlashAttribute("userId", userService.getCurrentUser().getId());


        // Reset the request security
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // Redirect to profile page
        return "redirect:/user/profile/"  + userService.getCurrentUser().getId();
    }

    /**
     * Get the edit password page
     *
     * @param model user data
     * @return edit password page
     */
    @GetMapping("/editPassword")
    public String userEditPassword(Model model) {

        // Log password page
        logger.info("GET /user/editPassword");

        // Logic to retrieve user profile information
        User user = userService.getCurrentUser();

        // Add user data to the model
        model.addAttribute("user", user);

        // Get gardens for the navbar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());

        // Return the password page
        return "user/editPassword";
    }

    @Autowired
    ApplicationEventPublisher eventPublisher;

    /**
     * User can submit their edited password if authenticated user
     * @param request the request
     * @param oldPassword the old password
     * @param password the new password
     * @param passwordRepeat the new password repeated
     * @param user the user
     * @param bindingResult to store errors
     * @param model user data
     * @return page depending on if user is authorised to make changes
     */
    @PostMapping("/editPassword")
    public String userEditPassword( HttpServletRequest request, @RequestParam("oldPassword") String oldPassword,
                                    @RequestParam("password") String password,
                                    @RequestParam("passwordRepeat") String passwordRepeat,
                                    @ModelAttribute("user") User user,
                                    BindingResult bindingResult,
                                    Model model) {

        // Log post request
        logger.info("POST /user/editPassword");
        // Try to authenticate the user edit

        bindingResult = userAuthenticationService.authenticateUserEditPassword(user, oldPassword, bindingResult);
        if (bindingResult.hasErrors()) {
            // Get gardens for the navbar
            model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
            model.addAttribute("userId", userService.getCurrentUser().getId());
            model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());

            model.addAttribute("user", user);
            model.addAttribute("oldPassword", oldPassword);
            model.addAttribute("password", password);
            model.addAttribute("passwordRepeat", passwordRepeat);

            return "user/editPassword";
        }
        User editedUser = userAuthenticationService.editUserPassword(user);
        String appUrl = request.getContextPath();
        eventPublisher.publishEvent(new OnPasswordChangeEvent(editedUser, request.getLocale(), appUrl));

        // Redirect to profile page
        return "redirect:/user/profile/" + userService.getCurrentUser().getId();
    }

    /**
     * Endpoint for the location autofilled pages to query
     * @param input a string representing the section of the url in the API request for the input data, e.g. 'Christchur'
     * @param type (Optionally) the type of location we are hoping to autofill: e.g. city, country etc
     * @return the response from the location autocomplete API
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<Object> getAutocomplete(@RequestParam String input,
                                                  @RequestParam(required = false) String type) {
        Object response = autocompleteService.getAutocomplete(input, type);
        return ResponseEntity.ok(response);
    }

}
