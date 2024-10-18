package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseLiveStreamsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LivestreamService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.StreamValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for browsing, creating, and viewing livestreams.
 */
@Controller
@RequestMapping("/livestream")
public class LiveStreamController {

    Logger logger = LoggerFactory.getLogger(LiveStreamController.class);

    @Autowired
    LivestreamService livestreamService;

    StreamValidator streamValidator;

    @Autowired
    GardenService gardenService;

    @Autowired
    UserService userService;

    private static final String NAV_USER_TAG = "navBarUser";

    private static final String LIVE_STREAM_DTO_TAG = "livestreamDTO";

    public LiveStreamController () {
        streamValidator = new StreamValidator();
    }

    /**
     * Gets the browse page which displays all the livestreams, uses pagination
     */
    @GetMapping("/browse")
    public String browseLivestreams(@RequestParam(name = "page", required = false, defaultValue = "1") String page, Model model) {

        BrowseLiveStreamsDTO browseLiveStreamsDTO = new BrowseLiveStreamsDTO(page);

        livestreamService.parsePages(browseLiveStreamsDTO);

        livestreamService.getLiveStreamsPaginated(browseLiveStreamsDTO);

        // Add livestreams
        model.addAttribute(browseLiveStreamsDTO);

        // Get gardens and user for nav bar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());

        return "livestream/browse";
    }

    /**
     * Gets the create page which the user can use to create a livestream.
     */
    @GetMapping("/create")
    public String createLivestreamForm(Model model,
                                       @ModelAttribute(name = LIVE_STREAM_DTO_TAG) LiveStreamDTO livestreamDTO) {

        // Get gardens and user for nav bar
        model.addAttribute(LIVE_STREAM_DTO_TAG, livestreamDTO);
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());
        return "livestream/create";
    }

    /**
     * Creates a livestream.
     *
     * @param livestreamDTO The DTO.
     */
    @PostMapping("/create")
    public String createLivestream(Model model, @ModelAttribute(name = LIVE_STREAM_DTO_TAG) LiveStreamDTO livestreamDTO,
                                   BindingResult bindingResult) {

        String url;

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(livestreamDTO);

        //Get livestream owner
        User livestreamOwner = userService.getCurrentUser();
        //Check if owner already has an existing livestream
        Livestream currentLivestream = livestreamService.getLivestreamByUser(livestreamOwner);

        if (exceptions.isEmpty() && currentLivestream == null) {
            // Add live stream to database
            Livestream livestream = livestreamService.createLiveStream(livestreamDTO, livestreamOwner);
            model.addAttribute(LIVE_STREAM_DTO_TAG, livestreamDTO);
            url = "redirect:browse/" + livestream.getId();
        }
        else {
            exceptions.forEach(e -> bindingResult.rejectValue(e.getField(), e.getErrorCode(), e.getMessage()));
            if (currentLivestream != null) {
                bindingResult.reject("existingLivestream", currentLivestream.getId().toString());
            }
            url = "livestream/create";
        }
        // Get gardens and user for nav bar
        model.addAttribute(LIVE_STREAM_DTO_TAG, livestreamDTO);
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());

        return url;
    }

    /**
     * Views a particular livestream.
     *
     * @param livestreamId The id of the livestream.
     */
    @GetMapping("/browse/{livestreamId}")
    public String livestream(@PathVariable Long livestreamId, Model model) {
        // Gets the livestream
        Livestream livestream = livestreamService.getLivestream(livestreamId);
        if (livestream == null) {
            model.addAttribute("status", "404");
            model.addAttribute("error", "This livestream no longer exists");
            return "error";
        }
        // Check if the current user is the owner of the livestream
        boolean isOwner = livestream.getOwner().getId().equals(userService.getCurrentUser().getId());

        //TODO: probably move this to front end if got time.
        User user = userService.getCurrentUser();
        String firstName = user.getFirstName();
        String lastName = user.getLastName() == null ? "" : ' ' + user.getLastName();
        model.addAttribute("chatName", firstName + lastName);

        // Add necessary attributes to the view
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("livestream", livestream);
        model.addAttribute("isHost", isOwner);
        model.addAttribute("hostId", livestream.getOwner().getId());

        //use unique livestreamId as the roomId users subscribe to
        model.addAttribute("roomId", livestreamId);

        // Get gardens and user for nav bar
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_USER_TAG, userService.getCurrentUser());

        return "livestream/view";
    }

    /**
     * Gets the terms of service for livestreaming.
     */
    @GetMapping("/terms")
    public String termsOfService() {
        return "livestream/terms";
    }
}