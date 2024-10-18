package nz.ac.canterbury.seng302.gardenersgrove.controller;


import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseFeedDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnBlockedAccountEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnFifthInappropriateSubmissionWarningEvent;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PostService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PostValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller containing post and feed related endpoints
 */
@Controller
public class PostController {

    Logger logger = LoggerFactory.getLogger(PostController.class);
    private final UserService userService;
    private final GardenService gardenService;
    private final PostService postService;
    private final PostValidator postValidator;
    private final ApplicationEventPublisher eventPublisher;
    private static final String NAV_BAR_USER = "navBarUser";
    private static final String GARDENS = "gardens";



    /**
     * Constructor
     * @param userService user service for retrieving user information
     * @param gardenService garden service for populating user gardens in navbar
     * @param postService post service for accessing repository
     * @param profanityFilterService service for checking text with 3rd party api
     * @param eventPublisher handles events for posts such as inappropriate content submissions
     */
    @Autowired
    public PostController(UserService userService, GardenService gardenService, PostService postService,
                          ProfanityFilterService profanityFilterService, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.gardenService = gardenService;
        this.postService = postService;
        this.postValidator = new PostValidator(profanityFilterService, postService);
        this.eventPublisher = eventPublisher;
    }

    /**
     * GET Endpoint for browsing the feed
     * @return the feed page with posts injected
     */
    @GetMapping("/feed/browse")
    public Object getFeed(@RequestParam(name = "page", required = false, defaultValue = "1") String page, Model model) {
        logger.info("GET /feed/browse");

        BrowseFeedDTO browseFeedDTO = new BrowseFeedDTO(page);

        // Check that user hasn't manually input an invalid page
        postService.parsePages(browseFeedDTO);

        postService.getPosts(browseFeedDTO);

        //To create a string of comment-input field id's which is used for 'enter' submission on the comment form
        String allowedIdsString = browseFeedDTO.getPosts().stream()
                .map(post -> "comment-input" + post.getId())
                .collect(Collectors.joining(","));

        // Add attribute dto model
        model.addAttribute("allowedIds", allowedIdsString);
        model.addAttribute(browseFeedDTO);
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());
        model.addAttribute(GARDENS, gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        return "posts/feedPage";
    }

    /**
     * GET Endpoint directing to the "addPostForm"
     * @param postDTO a post DTO for populating the fields
     * @param model Model
     * @return the add Post Form
     */
    @GetMapping("/post/add")
    public String getPostForm(@ModelAttribute(name = "postDTO") PostDTO postDTO, Model model) {

        logger.info("GET /post/add");

        // Add attributes
        model.addAttribute("postDTO", postDTO);
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());
        model.addAttribute(GARDENS, gardenService.getGardensByUserId(userService.getCurrentUser().getId()));

        return "posts/addPostForm";
    }

    /**
     * POST Endpoint for creating a new post
     * @param postDTO post DTO to be converted to entity
     * @param model Model
     * @param bindingResult object for holding result of validation
     * @return Feed page on validation success or return to create post page on validation fail
     * or return to login page on 6th inappropriate submission
     */
    @PostMapping("/post/add")
    public String submitPost(@ModelAttribute(name = "postDTO") PostDTO postDTO ,
                             Model model,
                             BindingResult bindingResult,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        logger.info("POST /post/add");

        List<ValidationExceptionDTO> exceptions = postValidator.validatePost(postDTO);

        // Add 1 to the inappropriate counter.
        if (exceptions.stream().anyMatch(ValidationExceptionDTO::getIsProfane)) {
            // Handle Profane Post
            User currentUser = userService.handleInappropriateSubmission(userService.getCurrentUser());
            if (currentUser.isBlocked()) {
                eventPublisher.publishEvent(new OnBlockedAccountEvent(userService.getCurrentUser(), request.getLocale(), request.getContextPath()));
                redirectAttributes.addFlashAttribute("blocked", "true");
                return "redirect:/auth/login";
            }
            // Use attributes to show the warning modal on the gardenView page
            if (userService.getCurrentUser().hasReachedInappropriateWarningLimit()) {
                model.addAttribute("fifthInappropriateSubmission", "true");
                eventPublisher.publishEvent(new OnFifthInappropriateSubmissionWarningEvent(currentUser, request.getLocale(), request.getContextPath()));
            }
            // Add errors for profane tag
            model.addAttribute("invalidPost", postDTO);
            model.addAttribute("postError", "The submitted post wasn't added, as it was flagged as inappropriate");
        }

        if (exceptions.isEmpty()) {
            // Set garden name here as not passed through javascript

            Garden linkedGarden = null;

            if (postDTO.getGardenId() != null) {
                linkedGarden = gardenService.getGardenByID(postDTO.getGardenId()).orElse(null);
            }

            postDTO.setGarden(linkedGarden);
            // Add attributes for feed page
            Post addedPost = postService.addPost(postDTO);
            model.addAttribute("postEdit", false);
            model.addAttribute("addedPost", addedPost);
            return "redirect:/feed/browse";
        }

        exceptions.forEach(e -> bindingResult.rejectValue(e.getField(), e.getErrorCode(), e.getMessage()));

        model.addAttribute("postEdit", false);

        // For navbar
        model.addAttribute(GARDENS, gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());
        if (postDTO.getGardenId() != null && gardenService.getGardenByID(postDTO.getGardenId()).isPresent()) {
            model.addAttribute("linkedGarden", gardenService.getGardenByID(postDTO.getGardenId()).get());
        }
        return "posts/addPostForm";
    }

    /**
     * DELETE Endpoint for getting rid of a post
     * @param id the id of the post to be deleted, found in request path
     * @return reload the feed/browse page.
     */
    @DeleteMapping("/post/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {

        logger.info("DELETE /post/");

        // Validate
        if (!postValidator.checkPostExists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else if (!postValidator.checkUserOwnsPost(id, userService.getCurrentUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Delete post
        postService.deletePost(id);

        // Return OK
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}