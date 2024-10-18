package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.WeatherConditionsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnBlockedAccountEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnFifthInappropriateSubmissionWarningEvent;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.utility.EnvironmentUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.GardenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.web.bind.annotation.PathVariable;
import static java.lang.Long.parseLong;

/**
 * Controller for garden interactions.
 */
@Controller
public class GardenController {
    Logger logger = LoggerFactory.getLogger(GardenController.class);

    private final GardenService gardenService;
    private final GardenFilterService gardenFilterService;
    private final WeatherService weatherService;
    private final FriendService friendService;
    private final UserService userService;
    private final GardenValidator gardenValidator;
    ProfanityFilterService profanityFilterService;

    private static final String NAV_USER_TAG = "navBarUser";

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    EnvironmentUtils environmentUtils;

    @Autowired
    public GardenController(GardenService gardenService, UserService userService, FriendService friendService, WeatherService weatherService, ProfanityFilterService profanityFilterService, GardenFilterService gardenFilterService) {
        this.gardenService = gardenService;
        this.userService = userService;
        this.friendService = friendService;
        this.weatherService = weatherService;
        this.gardenFilterService = gardenFilterService;
        this.gardenValidator = new GardenValidator(profanityFilterService);
        this.profanityFilterService = profanityFilterService;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ModelAndView modelAndView = new ModelAndView("404");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
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
     * Setting for the environment utils
     * FOR TESTING ONLY
     *
     * @param environmentUtils the utility being set
     */
    public void setEnvironmentUtils(EnvironmentUtils environmentUtils) {
        this.environmentUtils = environmentUtils;
    }


    /**
     * Gets the thymeleaf template for the Garden list page
     *
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return "My Gardens" garden list page
     */
    @GetMapping("/garden/list/{userId}")
    public Object getGardenList(Model model, @PathVariable("userId") String userId) {
        logger.info("GET /garden/list");
        var modelAndView = new ModelAndView();
        // Check userId is valid
        if (!userService.validateUserId(userId)) {
            model.addAttribute("status", 403);
            model.addAttribute("error", "Cannot view another users page");
            return "error";
        }

        User user = userService.getById(userId);
        if (user == null) {
            model.addAttribute("status", 403);
            model.addAttribute("error", "Cannot view another users page");
            return "error";
        }


        User currentUser = userService.getCurrentUser();

        if (!friendService.checkIfFriends(currentUser.getId(), Long.parseLong(userId))) {
            model.addAttribute("status", 403);
            model.addAttribute("error", "Cannot view another users page");
            return "error";
        }


        List<Garden> gardens = gardenService.getGardensByUserId(Long.parseLong(userId));
        modelAndView.addObject("gardensDisplay", gardens);
        if (gardens.isEmpty()) {
            modelAndView.addObject("noGardens", true);
        }


        modelAndView.addObject("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        modelAndView.addObject("user", userService.getById(userId));
        modelAndView.addObject(NAV_USER_TAG, userService.getCurrentUser());

        modelAndView.setViewName("gardenListPage");

        return modelAndView;
    }

    /**
     * The garden details page for a given garden
     *
     * @param id    the id of the garden being looked at
     * @param model a map-like representation of details required for the page to render correctly
     * @return The garden details page for a given garden
     */
    @GetMapping("/garden/{gardenId}")
    public Object viewGardenById(@PathVariable("gardenId") String id, Model model) {
        logger.info(String.format("GET garden/%s", id));
        var modelAndView = new ModelAndView();

        if (!(id.matches("^[0-9]+"))) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }
        User currentUser = userService.getCurrentUser();

        Optional<Garden> garden = gardenService.getGardenByID(parseLong(id));

        if (garden.isEmpty()) {
            modelAndView.setStatus(HttpStatus.FORBIDDEN);
            modelAndView.setViewName("error");
            modelAndView.addObject("error", "Forbidden, you do not own this garden");
            modelAndView.addObject("status", 403);
            return modelAndView;
        } else if (!friendService.checkIfFriends(currentUser.getId(), garden.get().getUser().getId()) && !garden.get().isPublicGarden()) {
            modelAndView.setStatus(HttpStatus.FORBIDDEN);
            modelAndView.setViewName("error");
            modelAndView.addObject("error", "Forbidden, you do not own this garden");
            modelAndView.addObject("status", 403);
            return modelAndView;
        } else {
            if (Objects.equals(currentUser.getId(), garden.get().getUser().getId())) {
                modelAndView.addObject("owner", true);
            } else {
                modelAndView.addObject("owner", false);
            }
            modelAndView.setStatus(HttpStatus.OK);
            modelAndView.addObject("garden", garden.get());

            // Sets the location for the garden depending on whether the location was autofilled, which would cause coordinates to be created.
            String location;
            WeatherConditionsDTO weatherConditionsDTO = new WeatherConditionsDTO();
            weatherConditionsDTO.setHasErrors();
            if (garden.get().getLocation().getLatitude() != null && garden.get().getLocation().getLongitude() != null) {
                location = garden.get().getLocation().getLatitude() + "," + garden.get().getLocation().getLongitude();
                weatherConditionsDTO = weatherService.getWeatherConditions(location);
            }
            modelAndView.addObject("weatherConditions", weatherConditionsDTO);

            if (weatherConditionsDTO != null) {
                if (weatherConditionsDTO.getWeatherConditionsHistory().isEmpty() || weatherConditionsDTO.getWeatherConditions().isEmpty()) {
                    modelAndView.addObject("weatherError", "Could not get weather data");
                } else {
                    if (garden.get().getWeatherDismissalDate() == null) {
                        modelAndView.addObject("weatherMessageDismissed", false);
                        if (weatherConditionsDTO.needToWater()) {
                            modelAndView.addObject("weatherMessage", "There hasn’t been any rain recently, make sure to water your plants if they need it");
                        } else if (weatherConditionsDTO.isRaining()){
                            modelAndView.addObject("weatherMessage", "Outdoor plants don’t need any water today");
                        }
                    } else {
                        // Create the dismissal date as the current date
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                        String currentDateString = sdf.format(new Date());
                        String dismissalDateString = sdf.format(garden.get().getWeatherDismissalDate());

                        if (!currentDateString.equals(dismissalDateString)) {
                            modelAndView.addObject("weatherMessageDismissed", false);
                            try {
                                gardenService.dismissGardenWeatherMessage(parseLong(id), false);
                            } catch (Exception e) {
                                modelAndView.addObject("weatherError", "Could not get weather data");
                            }
                            if (weatherConditionsDTO.needToWater()) {
                                modelAndView.addObject("weatherMessage", "There hasn’t been any rain recently, make sure to water your plants if they need it");
                            } else if (weatherConditionsDTO.isRaining()){
                                modelAndView.addObject("weatherMessage", "Outdoor plants don’t need any water today");
                            }
                        } else {
                            modelAndView.addObject("weatherMessageDismissed", true);
                        }
                    }
                }
            }



            modelAndView.setViewName("gardenView");
        }
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute("tags", gardenService.getTags());
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());
        return modelAndView;
    }


    @GetMapping("/garden/{gardenId}/dismiss-weather")
    public Object dismissWeatherMessage(@PathVariable("gardenId") String id, Model model) {
        logger.info(String.format("GET garden/%s/dismissWeather", id));
        var modelAndView = new ModelAndView();

        if (!(id.matches("^[0-9]+"))) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }

        Optional<Garden> garden = gardenService.getGardenByID(parseLong(id));

        if (garden.isEmpty()) {
            modelAndView.setStatus(HttpStatus.FORBIDDEN);
            modelAndView.setViewName("error");
            modelAndView.addObject("error", "Forbidden, you do not own this garden");
            modelAndView.addObject("status", 403);
            return modelAndView;
        }

        garden.get().setWeatherMessageDismissed(true);
        try {
            gardenService.dismissGardenWeatherMessage(parseLong(id), true);
        } catch (GardenNotFoundException e) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;

        }
        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.setViewName("redirect:/garden/" + id);
        return "redirect:/garden/" + id;
    }

    /**
     * Changes the publicity of a garden with a given ID
     *
     * @param isPublic           whether the garden should be public or not
     * @param gardenId           the d of the garden for which the publicity is being set
     * @param model              a map-like representation of details required for the page to render correctly
     * @param redirectAttributes a map-like representation of details required for the redirected page to render correctly
     * @return the details page of the updated garden
     */
    @PutMapping("/garden/{gardenId}")
    public String editGardenPublicity(@RequestParam(name = "isPublic", required = false) String isPublic
            , @PathVariable("gardenId") long gardenId,
                                      Model model, RedirectAttributes redirectAttributes) {

        Optional<Garden> garden = gardenService.getGardenByID(gardenId);

        if (!gardenService.checkGardenOwnership(gardenId)) {
            model.addAttribute("error", "Forbidden, you do not own this garden");
            model.addAttribute("status", 403);
            return "error";
        }
        if (garden.isEmpty()) {
            return "404";
        }

        try {
            gardenService.updateGardenPublicity(gardenId, !GardenValidator.isFieldEmpty(isPublic));
        } catch (GardenNotFoundException e) {
            return "404";
        }

        redirectAttributes.addFlashAttribute("garden", garden.get());
        redirectAttributes.addFlashAttribute("editSuccessful", true);
        return "redirect:/garden/" + gardenId;
    }

    /**
     * Gets form to be displayed, includes the ability to display results of previous form when linked to from POST form
     *
     * @param gardenDTO Data transfer object containing the data for the garden the user is creating
     * @param model     (map-like) representation of gardens and userID for use in thymeleaf, with values being
     *                  set to relevant parameters provided
     * @return thymeleaf addGardenForm
     */
    @GetMapping("/garden/add")
    public String addGarden(@ModelAttribute(name = "gardenDTO") GardenDTO gardenDTO,
                            Model model) {
        logger.info("GET /garden/add");
        model.addAttribute("gardenDTO", gardenDTO);
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());
        return "addGardenForm";
    }

    /**
     * Posts a form response with added garden details
     *
     * @param gardenDTO Data transfer object containing the data for the garden the user is creating
     * @param model     (map-like) representation of gardens and userID for use in thymeleaf, with values being
     *                  set to relevant parameters provided
     * @return the page for the added garden if no errors are found, and the field with error messages otherwise
     */
    @PostMapping("/garden/add")
    public String submitForm(@ModelAttribute(name = "gardenDTO") GardenDTO gardenDTO,
                             Model model,
                             BindingResult bindingResult) {
        logger.info("POST /garden/add");

        List<ValidationExceptionDTO> exceptions = gardenValidator.validateGarden(gardenDTO, Optional.of(false));
        if (exceptions.isEmpty()) {
            Garden addedGarden = gardenService.addGarden(new Garden(gardenDTO, userService.getCurrentUser()));
            return "redirect:/garden/" + addedGarden.getGardenId();
        }

        exceptions.forEach(e -> bindingResult.rejectValue(e.getField(), e.getErrorCode(), e.getMessage()));

        model.addAttribute("gardenEdit", false);

        // If the only error is the profanity API, then let pop up modal be open
        String apiErrorMessage = "Description could not be checked for profanity at this time. Please try again later";
        if (exceptions.size() == 1 && exceptions.stream().anyMatch(e -> apiErrorMessage.equals(e.getMessage()))) {
            logger.info("Profanity API Error");
            model.addAttribute("apiErrorOpen", true);
        }

        // For navbar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());

        return "addGardenForm";
    }


    /**
     * Gets form to be displayed, includes the ability to display results of previous form when linked to from POST form
     *
     * @param id        the id of the garden for which the edit form is being reqeusted
     * @param gardenDTO Data transfer object containing the data for the garden the user is creating
     * @param model     (map-like) representation of gardens and userID for use in thymeleaf, with values being
     *                  set to relevant parameters provided
     * @return thymeleaf addGardenForm
     */
    @GetMapping("/garden/add/{id}")
    public String editGardenGet(@PathVariable("id") Long id,
                                @ModelAttribute(name = "gardenDTO") GardenDTO gardenDTO,
                                Model model) {
        logger.info(String.format("GET /garden/add/%d", id));

        Optional<Garden> garden = gardenService.getGardenByID(id);
        if (!gardenService.checkGardenOwnership(id)) {
            model.addAttribute("error", "Forbidden, you do not own this garden");
            model.addAttribute("status", 403);
            return "error";
        }
        String size = garden.get().getSize() != null ? String.valueOf(garden.get().getSize()) : null;
        model.addAttribute("gardenDTO", new GardenDTO(
                        garden.get().getName(),
                        garden.get().getDescription(),
                        size,
                        garden.get().getLocation())
        );

        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());
        return "addGardenForm";
    }

    /**
     * Creates a put request with the garden details for the edit
     *
     * @param id        the id of the garden for which the edit form is being requested
     * @param gardenDTO Data transfer object containing the data for the garden the user is creating
     * @param model     (map-like) representation of gardens and userID for use in thymeleaf, with values being
     *                  set to relevant parameters provided
     * @return the page for the added garden if no errors are found, and the field with error messages otherwise
     */
    @PutMapping("/garden/add/{id}")
    public String editGardenPut(@PathVariable("id") Long id,
                                @ModelAttribute(name = "gardenDTO") GardenDTO gardenDTO,
                                Model model,
                                BindingResult bindingResult) {

        if (!gardenService.checkGardenOwnership(id)) {
            model.addAttribute("error", "Forbidden, you do not own this garden");
            model.addAttribute("status", 403);
            return "error";
        }

        Optional<Garden> garden = gardenService.getGardenByID(id);
        if (garden.isEmpty()) {
            return "404";
        }
        // Garden Service to check if description has changed
        boolean skipDescriptionValidation = gardenService.checkDescriptionNoChange(id, gardenDTO.getDescription());
        List<ValidationExceptionDTO> exceptions = gardenValidator.validateGarden(gardenDTO, Optional.of(skipDescriptionValidation));

        if (exceptions.isEmpty()) {
            try {
                gardenService.updateGarden(id, gardenDTO.getGardenName(), gardenDTO.getLocation(), gardenDTO.getSizeAsDouble(),
                        gardenDTO.getDescription());
            } catch (GardenNotFoundException e) {
                return "404";
            }
            model.addAttribute("gardenEdit", true);
            return "redirect:/garden/" + id;
        }

        exceptions.forEach(e -> bindingResult.rejectValue(e.getField(), e.getErrorCode(), e.getMessage()));

        // If the only error is the profanity API, then let pop up modal be open
        String apiErrorMessage = "Description could not be checked for profanity at this time. Please try again later";
        if (exceptions.size() == 1 && exceptions.stream().anyMatch(e -> apiErrorMessage.equals(e.getMessage()))) {
            logger.info("Profanity API Error");
            model.addAttribute("apiErrorOpen", true);
        }
        // Pass back a copy of the description to pre-fill Continue Without Description option
        model.addAttribute("descriptionCopy", garden.get().getDescription());

        // For navbar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());

        return "addGardenForm";
    }

    /**
     * Returns the search results page for a given query
     *
     * @param search the search term given by the user
     * @param page   the number of the page the user wants to view
     * @param tags A string with tags seperated via a comma
     * @return the search results page for a given query
     */
    @GetMapping("/garden/browse")
    public Object gardenBrowse(@RequestParam(name = "search", required = false, defaultValue = "") String search,
                               @RequestParam(name = "page", required = false, defaultValue = "1") String page,
                               @RequestParam(name = "tags", required = false) String tags,
                               Model model) {
        logger.info("GET /garden/browse");

        // Create a BrowseGardenDTO, that carries Tags, Search, Page and resulting Gardens
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO(tags, search, page);
        // Parse the tags from a string to a List<String>
        gardenFilterService.parseTags(browseGardenDTO);
        // Page parsing
        gardenFilterService.parsePages(browseGardenDTO);
        // Get Gardens
        gardenFilterService.getGardens(browseGardenDTO);
        // Possibly then return a DTO?
        model.addAttribute(browseGardenDTO);

        // Get gardens and user for nav bar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());

        return "browseGardens";
    }

    /**
     * Endpoint for adding tags to gardens
     * @param id of garden
     * @param tagName the string of the tag
     * @param redirectAttributes required
     * @return redirect back to Get gardenView route
     */
    @PutMapping("/garden/{id}/tag")
    public Object editGardenPut(HttpServletRequest request, @PathVariable("id") Long id,
                                @RequestParam(name = "tag") String tagName, RedirectAttributes redirectAttributes) {
        logger.info("PUT /garden/" + id.toString() + "/tag");

        User currentUser = userService.getCurrentUser();
        if (!GardenValidator.isValidTag(tagName)) {
            // Handle Error Invalid Tag
            redirectAttributes.addFlashAttribute("tagError", "The tag name must only contain alphanumeric characters, spaces,  -, _, ', or ”");
            redirectAttributes.addFlashAttribute("invalidTag", tagName);
            return "redirect:/garden/" + id;
        } else if (!GardenValidator.tagIsValidLength(tagName)) {
            // Handle Tag To Long
            redirectAttributes.addFlashAttribute("tagError", "A tag cannot exceed 25 characters");
            redirectAttributes.addFlashAttribute("invalidTag", tagName);
            return "redirect:/garden/" + id;
        } else if (!gardenService.checkGardenOwnership(id)) {
            redirectAttributes.addFlashAttribute("tagError", "You do not own this garden");
            redirectAttributes.addFlashAttribute("invalidTag", tagName);
            return "redirect:/garden/" + id;
        } else {
            try {
                if (gardenValidator.isProfane(tagName)) {
                    // Handle Profane Tag
                    userService.handleInappropriateSubmission(userService.getCurrentUser());
                    if (userService.getCurrentUser().isBlocked()) {
                        eventPublisher.publishEvent(new OnBlockedAccountEvent(currentUser, request.getLocale(), request.getContextPath()));
                        redirectAttributes.addFlashAttribute("blocked", "true");
                        return "redirect:/auth/login";
                    }
                    // Use flash attribute to show the warning modal on the gardenView page
                    if (userService.getCurrentUser().hasReachedInappropriateWarningLimit()) {
                        redirectAttributes.addFlashAttribute("fifthInappropriateSubmission", "true");
                        eventPublisher.publishEvent(new OnFifthInappropriateSubmissionWarningEvent(currentUser, request.getLocale(), request.getContextPath()));
                    }
                    // Add errors for profane tag
                    redirectAttributes.addFlashAttribute("invalidTag", tagName);
                    redirectAttributes.addFlashAttribute("tagError", "The submitted tag wasn't added, as it was flagged as inappropriate");
                } else {
                    // Add tag to database (Blue Sky Scenario)
                    gardenService.addTag(tagName, id);
                }
                return "redirect:/garden/" + id;
            } catch (JsonProcessingException | HttpClientErrorException e) {
                logger.error("Could not check tag for profanity");
                redirectAttributes.addFlashAttribute("tagError", "The submitted tag wasn't added, as it could not be checked for profanity at this time");
                return "redirect:/garden/" + id;
            }
        }
    }

    /**
     * Endpoint for checking tag when adding it to the search.
     *
     * @param tag The tag the user is trying to add.
     * @return A response entity with true if the tag exists and false if not.
     */
    @GetMapping("/tags/check")
    public ResponseEntity<Boolean> checkTag(@RequestParam String tag) {

        // Log request
        logger.info("GET /tags/check Checking tag");

        // Check for same tag
        boolean exists = gardenService.getPublicTags().stream()
                .anyMatch(existingTag -> existingTag.getName().equals(tag));

        // Return result
        return ResponseEntity.ok(exists);
    }

    /**
     * Endpoint to autofill tag suggestions when browsing gardens by tag
     * @param input the input that user has entered into the textbox
     * @return a list of tags that begin with the input string, or an empty list if none were found
     */
    @GetMapping("/tags")
    public ResponseEntity<Object> autofillTags(@RequestParam String input) {
        if (GardenValidator.isValidTag(input)) {
            return ResponseEntity.ok(gardenService.getAutofilledTags(input));
        }
        return ResponseEntity.ok(List.of());
    }

    /**
     * Endpoint to search the user's public gardens by tag
     * @param name the name of the garden to search for
     * @return a list of the public gardens owned by the user, or an empty list if none were found
     */
    @GetMapping("/garden/public/search")
    public ResponseEntity<Object> searchPublicGardensOfCurrentUser(@RequestParam String name) {
        if (name.matches("^[\\p{L}0-9 ‘.,-]+$")) {
            List<Garden> gardens = gardenService.searchPublicGardensByUserIdAndName(userService.getCurrentUser().getId(), name);
            if (gardens.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(gardens);
        }
        return ResponseEntity.ok(Map.of("errorMessage", "Invalid search. Queries may only contain alphanumeric characters, -, ‘, dots, commas and spaces"));
    }
}
