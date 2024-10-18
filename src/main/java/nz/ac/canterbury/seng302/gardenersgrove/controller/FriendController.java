package nz.ac.canterbury.seng302.gardenersgrove.controller;


import nz.ac.canterbury.seng302.gardenersgrove.entity.Friend;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.FriendNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for garden interactions.
 */
@Controller
public class FriendController {

    Logger logger = LoggerFactory.getLogger(FriendController.class);

    private final FriendService friendService;
    private final UserService userService;

    private final GardenService gardenService;

    @Autowired
    public FriendController(FriendService friendService, UserService userService, GardenService gardenService) {
        this.friendService = friendService;
        this.userService = userService;
        this.gardenService = gardenService;
    }

    /**
     * Get the friends page
     * @param search the keyword to search for
     * @param page the page number
     * @return the friends page
     */
    @GetMapping("/manage/friends")
    public Object getFriends(@RequestParam(name="search", required = false, defaultValue = "") String search,
                             @RequestParam(name="page", required = false, defaultValue = "1") String page) {
        logger.info("GET /manage/friends");
        var modelAndView = new ModelAndView();
        User currentUser = userService.getCurrentUser();

        int parsedPage;
        if (!search.isEmpty()) {
            try {
                parsedPage = Integer.parseInt(page);
                if (parsedPage < 1) {
                    parsedPage = 1;
                }
            } catch (NumberFormatException e) {
                parsedPage = 1;
            }
            Page<User> usersPage = userService.searchUsers(search, PageRequest.of(parsedPage - 1, 7));
            List<User> users = usersPage.getContent();
            if (users.isEmpty()) {
                modelAndView.addObject("searchError", "There is nobody with that name or email in Gardener's Grove.");
            } else {
                modelAndView.addObject("users", users);
            }
            modelAndView.addObject("search", search);
            modelAndView.addObject("searchOpen", true);
            modelAndView.addObject("currentPage", parsedPage);
            modelAndView.addObject("totalPages", usersPage.getTotalPages());
        }

        // Info to be displayed on loading of manage friends page
        List<Friend> requests = friendService.getIncomingRequests(currentUser.getId());
        modelAndView.addObject("requests", requests);

        List<User> friends = friendService.getCurrentUsersFriends(currentUser);
        modelAndView.addObject("friends", friends);

        List<Friend> sentRequests = friendService.getOutGoingRequests(currentUser.getId());
        modelAndView.addObject("sentRequests", sentRequests);

        modelAndView.addObject("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        modelAndView.addObject("navBarUser", userService.getCurrentUser());
        modelAndView.setViewName("manageFriends/manageFriends");
        return modelAndView;
    }

    /**
     * Send a friend request
     * @param addFriend id of the friend to add
     * @param redirectAttributes redirectAttributes
     * @return redirect to get friends page
     */
    @PostMapping("/manage/friends/add")
    public Object sendFriendRequest(@RequestParam(name="add-friend", required = false, defaultValue = "") Long addFriend,
                                    RedirectAttributes redirectAttributes) {
        logger.info("/manage/friends/add");

        // Get user that current user is sending a friend request to
        User requestRecipient = userService.getById(addFriend.toString());

        if (requestRecipient == null) {
            redirectAttributes.addFlashAttribute("error", "Sorry that user no longer exists");
            return "redirect:/manage/friends";
        }

        // Create friend relation-ship between current user, and other user.
        try {
            friendService.addFriend(new Friend(userService.getCurrentUser(), requestRecipient));
        } catch (FriendNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manage/friends";
        }

        return "redirect:/manage/friends";
    }

    /**
     * Accept or decline a friend request
     * @param acceptId id of friend request your accepting
     * @param declineId id of friend request your declining
     * @param redirectAttributes redirectAttributes
     * @return redirect to get friends page
     */
    @PostMapping("/manage/friends/request")
    public Object respondFriendRequest(@RequestParam(name="accept", required = false, defaultValue = "") String acceptId,
                                @RequestParam(name="decline", required = false, defaultValue = "") String declineId,
                                RedirectAttributes redirectAttributes) {
        logger.info("/manage/friends/request");
        if (!friendService.checkFriendRequest(acceptId, declineId)) {
            redirectAttributes.addFlashAttribute("error", "Invalid Friend Id");
            return "redirect:/manage/friends";
        }

        if (!acceptId.isEmpty()) {
            try {
                friendService.changeFriendStatus(Long.parseLong(acceptId), "accepted");
            } catch (FriendNotFoundException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/manage/friends";
            }
        }

        if (!declineId.isEmpty()) {
            try {
                friendService.changeFriendStatus(Long.parseLong(declineId), "declined");
            } catch (FriendNotFoundException e) {
               redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/manage/friends";
            }
        }

        return "redirect:/manage/friends";
    }


    @DeleteMapping("/manage/friends/request/cancel")
    public Object cancelFriendRequest(@RequestParam(name= "friendId", required = false, defaultValue = "") String friendshipIdString,
                                RedirectAttributes redirectAttributes) {
        logger.info("/manage/friends/request/cancel");

        if (friendshipIdString.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid Friend Id");
            return "redirect:/manage/friends";
        }

        Long friendshipId = Long.parseLong(friendshipIdString);

        Optional<Friend> friend = friendService.getByFriendshipId(friendshipId);

        if (friend.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid Friend Id");
            return "redirect:/manage/friends";
        }

        Friend pendingFriendship = friend.get();

        if (pendingFriendship.getSender() != userService.getCurrentUser()) {
            redirectAttributes.addFlashAttribute("error", "You cannot cancel a friend request you didn't send");
            return "redirect:/manage/friends";
        }
        if (!Objects.equals(pendingFriendship.getStatus(), "pending")) {
            redirectAttributes.addFlashAttribute("error", "This friend request has already been accepted or deleted. Please refresh the page and try again");
            return "redirect:/manage/friends";
        }

        try {   // Removes the pending friend relationship
            friendService.deleteFriend(friendshipId);
        } catch (FriendNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manage/friends";
        }

        return "redirect:/manage/friends";
    }

    @PostMapping("/friend")
    public Object removeFriend(@RequestParam(name="friendId", required = false, defaultValue = "") Long friendId,
                               RedirectAttributes redirectAttributes) {
        logger.info("/friend");

        if (friendId == null) {
            redirectAttributes.addFlashAttribute("error", "Invalid Friend Id");
            return "redirect:/manage/friends";
        }
        try {
            Friend friend = friendService.getFriendRequestsByUser1User2(userService.getCurrentUser().getId(), friendId).get(0);

            if (userService.getCurrentUser() != friend.getSender() && userService.getCurrentUser() != friend.getRecipient()) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete a friendship you are not a part of");
                return "redirect:/manage/friends";
            }
            if (!Objects.equals(friend.getStatus(), "accepted")) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete a friendship that has not yet been accepted");
                return "redirect:/manage/friends";
            }

            friendService.changeFriendStatus(friend.getFriendId(), "removed");
        } catch (FriendNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manage/friends";
        }

        return "redirect:/manage/friends";
    }
}
