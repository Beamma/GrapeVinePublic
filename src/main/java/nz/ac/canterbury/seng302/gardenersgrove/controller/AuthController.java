package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ForgotPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ResetPasswordDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnForgotPasswordEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnPasswordChangeEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.gardenersgrove.exception.ValidationException;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserAuthenticationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The auth controller. Contains POST and GET for login and register
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserService service;

    Logger logger = LoggerFactory.getLogger(AuthController.class);

    UserAuthenticationService userAuthenticationService;

    /**
     * The constructor for auth controller
     *
     * @param userAuthenticationService for authenticating users
     */
    @Autowired
    public AuthController(UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    /**
     * Setter for the event publisher.
     * FOR TESTING ONLY
     *
     * @param publisher the publisher being set
     */
    public void setEventPublisher(ApplicationEventPublisher publisher) {
        this.eventPublisher = publisher;
    }

    /**
     * Setter for the user service.
     * FOR TESTING ONLY
     *
     * @param userService the userService being set
     */
    public void setUserService(UserService userService) {
        this.service = userService;
    }


    /**
     * Gets the thymeleaf page representing the /login page
     *
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf landingPageTemplate
     */
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {

        // Log request
        logger.info("GET /auth/login");

        // Check if there is a logout message
        String bannedMessage = (String) request.getSession().getAttribute("logoutMessage");
        if (bannedMessage != null) {
            model.addAttribute("message", bannedMessage);
            request.getSession().removeAttribute("logoutMessage");
        }

        // Add user to model
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }

        // Return login template
        return "auth/login";
    }

    /**
     * @param user          The user object from the form
     * @param bindingResult The binding result to add errors
     * @param request       The request to validate
     */
    @PostMapping("/login")
    public String login(@ModelAttribute("user") User user, @RequestParam("password") String password, BindingResult bindingResult, HttpServletRequest request, Model model) {

        // Log post request
        logger.info("POST /auth/login");

        bindingResult = userAuthenticationService.authenticateUserLogin(user, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("password", password);
            return "auth/login";
        }

        userAuthenticationService.logInUser(user);

        // Add the token to the session
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // Redirect to profile page
        return "redirect:/user/home";
    }

    /**
     * Gets the thymeleaf page representing the /register page
     *
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf register
     */
    @GetMapping("/register")
    public String register(Model model) {

        // Log request
        logger.info("GET /auth/register");

        // Add user to model for form
        model.addAttribute("user", new User());

        // Return register template
        return "auth/register";
    }



    @PostMapping("/register")
    public String register( HttpServletRequest request,
                            @RequestParam("password") String password,
                            @RequestParam("passwordRepeat") String passwordRepeat,
                            @ModelAttribute("user") User user,
                            BindingResult bindingResult,
                            Model model) {

        // Log post request
        logger.info("POST /auth/register");

        bindingResult = userAuthenticationService.authenticateUserRegister(user, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("password", password);
            model.addAttribute("passwordRepeat", passwordRepeat);
            return "auth/register";
        } else {
            User registered = userAuthenticationService.registerUser(user);
            String appUrl = request.getContextPath();
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), appUrl));
        }

        // Redirect to validate email page
        return "redirect:/auth/registration-confirm";
    }


    @GetMapping("/registration-confirm")
    public String confirmRegistration(WebRequest request, Model model) {
        logger.info("GET /auth/registrationConfirm");

        return "auth/registrationConfirm";
    }

    @PostMapping("/registration-confirm")
    public Object confirmRegistration(WebRequest request, Model model, @RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        logger.info("POST /auth/registrationConfirm");

        VerificationToken verificationToken = service.getVerificationToken(token);
        if (verificationToken == null) {
            String message = "Signup code invalid";
            ModelAndView modelAndView = new ModelAndView("auth/registrationConfirm");
            modelAndView.addObject("message", message);
            modelAndView.setStatus(HttpStatusCode.valueOf(400));
            return modelAndView;
        }

        if (verificationToken.hasExpired()) {
            String message = "Signup code invalid";
            ModelAndView modelAndView = new ModelAndView("auth/registrationConfirm");
            modelAndView.addObject("message", message);
            modelAndView.setStatus(HttpStatusCode.valueOf(400));
            return modelAndView;
        }
        User user = verificationToken.getUser();

        user.setEnabled(true);
        service.saveRegisteredUser(user);
        redirectAttributes.addFlashAttribute("accountActivationSuccessMessage", "Your account has been activated, please log in");
        logger.info("Authenticated correctly");
        return "redirect:/auth/login";
    }

    /**
     * Handles get requests to the forgot password page
     * @param model Representation of data to be used in thymeleaf display
     * @return The forgot password HTML template
     */
    @GetMapping("/forgotPassword")
    public String forgotPassword(Model model) {

        // Log request
        logger.info("GET /auth/forgotPassword");

        // Add DTO to model for form
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());

        // Return template
        return "auth/forgotPassword";
    }

    /**
     * Handles the post request for the forgot password page
     * @param forgotPasswordDTO The data transfer object for the page
     * @param bindingResult Object to add errors to the form
     * @return The page to be redirected to
     */
    @PostMapping("/forgotPassword")
    public String forgotPassword(HttpServletRequest request, Model model,
                                 @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO forgotPasswordDTO,
                                 BindingResult bindingResult) {

        // Log request
        logger.info("POST /auth/forgotPassword");

        // Try to authenticate the request
        try {
            User authenticatedUser = userAuthenticationService.authenticateForgotPassword(forgotPasswordDTO);

            // Send email if valid
            if (authenticatedUser != null && request.getLocale() != null && request.getContextPath() != null) {
                eventPublisher.publishEvent(new OnForgotPasswordEvent(authenticatedUser, request.getLocale(), request.getContextPath()));
            }

        // Show errors
        } catch (ValidationException e) {
            bindingResult.rejectValue(e.getField(), e.getErrorCode(), e.getMessage());
            return "auth/forgotPassword";
        }

        // Add confirmation message to the model
        model.addAttribute("confirmationMessage", "An email was sent to the address if it was recognised.");

        // Return the page with the confirmation message
        return "auth/forgotPassword";
    }

    /**
     * Handles get requests to the reset password page
     * @param model Representation of data to be used in thymeleaf display
     * @return The reset password HTML template
     */
    @GetMapping("/resetPassword")
    public String resetPassword(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {

        // Log request
        logger.info("GET /auth/resetPassword");

        // Check that token is valid
        try {
            userAuthenticationService.checkTokenValid(token);
        } catch (ValidationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reset password link has expired");
            return "redirect:auth/login";
        }

        // Add DTO to model for form
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setToken(token);
        model.addAttribute("resetPasswordDTO", resetPasswordDTO);

        // Return template
        return "auth/resetPassword";
    }

    /**
     * Handles the post request for the reset password page
     * @param resetPasswordDTO The data transfer object for the page
     * @param bindingResult Object to add errors to the form
     * @return The page to be redirected to
     */
    @PostMapping("/resetPassword")
    public String resetPassword(HttpServletRequest request, Model model,
                                 @ModelAttribute("resetPasswordDTO") ResetPasswordDTO resetPasswordDTO,
                                 BindingResult bindingResult) {

        // Log request
        logger.info("POST /auth/resetPassword");

        // Try to authenticate the request
        try {
            // Check valid
            User user = userAuthenticationService.authenticateResetPassword(resetPasswordDTO);

            // Publish an event for the email
            eventPublisher.publishEvent(new OnPasswordChangeEvent(user, request.getLocale(), request.getContextPath()));

        } catch (ValidationException e) {
            // Add errors to page
            bindingResult.rejectValue(e.getField(), e.getErrorCode(), e.getMessage());

            // Add given values back into page
            return "auth/resetPassword";
        }

        // Return redirect user to login page
        return "redirect:/auth/login";
    }

    @GetMapping("/logout-banned")
    public String logoutBanned(HttpServletRequest request, HttpServletResponse response, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // Set flash attribute
        redirectAttributes.addFlashAttribute("blocked", "true");
        return "redirect:/auth/login";
    }
}