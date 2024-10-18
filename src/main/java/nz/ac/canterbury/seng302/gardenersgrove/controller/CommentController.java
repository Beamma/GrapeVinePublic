package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.CommentDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnFifthInappropriateSubmissionWarningEvent;
import nz.ac.canterbury.seng302.gardenersgrove.event.OnBlockedAccountEvent;
import nz.ac.canterbury.seng302.gardenersgrove.service.CommentService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PostService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.CommentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;


/**
 * For sending json requests back and forth from the post page, without refreshing it
 * using the JavaScript fetch API
 */
@RestController
public class CommentController {

    Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final CommentValidator commentValidator;
    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public CommentController(UserService userService, PostService postService, CommentService commentService,
                             ProfanityFilterService profanityFilterService, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
        this.commentValidator = new CommentValidator(profanityFilterService);
        this.eventPublisher = eventPublisher;
    }

    /**
     * For adding a comment to a post
     *
     * @param id the id of the post
     * @return A response entity with either id of
     * the new comment or an error message in a string
     */
    @PutMapping("/post/{id}/comment")
    public ResponseEntity<Object> addCommentToPost(HttpServletRequest request, @PathVariable Long id,
                                                   @RequestBody Map<String, String> requestBody) {
        logger.info("PUT /post/{}/comment", id);

        String commentText = requestBody.get("comment");
        User currentUser = userService.getCurrentUser();
        CommentDTO commentDTO = new CommentDTO(commentText, currentUser, postService.getPostById(id));
        commentValidator.validateComment(commentDTO);

        boolean isFifthInappropriateSubmission = false;

        if (commentDTO.isProfane()) {
            userService.handleInappropriateSubmission(commentDTO.getUser());
            if (userService.getCurrentUser().isBlocked()) {
                eventPublisher.publishEvent(new OnBlockedAccountEvent(commentDTO.getUser(),
                        request.getLocale(), request.getContextPath()));

                String redirectUrl = "../auth/logout-banned";
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("redirected", true);
                responseBody.put("url", redirectUrl);

                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body(responseBody);
            }
            if (userService.getCurrentUser().hasReachedInappropriateWarningLimit()) {
                isFifthInappropriateSubmission = true;
                eventPublisher.publishEvent(new OnFifthInappropriateSubmissionWarningEvent(currentUser, request.getLocale(), request.getContextPath()));
            }
        }

        if (commentDTO.getError() != null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .header("Fifth-Inappropriate-Submission", String.valueOf(isFifthInappropriateSubmission))
                    .body(commentDTO.getError());
        }

        Comment savedComment = commentService.addComment(commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment.getId().toString());
    }

    /**
     * Use this endpoint to retrieve the html for a single comment.
     * Frontend calling this allows a comment fragment to have all its
     * fields populated by thymeleaf automatically.
     *
     * @param postId    id of the post
     * @param commentId id of comment
     * @param model     the model to populate thymeleaf fields
     * @return A html fragment containing a single comment
     */
    @GetMapping("/post/{postId}/comment/{commentId}")
    public ModelAndView getCommentCount(@PathVariable Long postId,
                                        @PathVariable Long commentId,
                                        Model model) {
        logger.info("GET /post/" + postId + "/comment/" + commentId);
        Comment comment = commentService.getCommentById(commentId);
        Post post = postService.getPostById(postId);

        if (comment == null || post == null) {
            //This is to ensure frontend doesn't receive anything it could confuse as html
            return null;
        }
        model.addAttribute("comment", comment);
        model.addAttribute("post", post);

        return new ModelAndView("fragments/comment", model.asMap());
    }

    /**
     * Use this endpoint to retrieve the html for the fifth inappropriate submission warning
     *
     * The way the modal exists atm, it has the th:if={fifthModalSubmission} in it, so having it hidden in the front-end,
     * when the page is initially loaded, it doesn't render. Then, when the comment is submitted, the page is not
     * refreshed, so the modal is not present to be shown. Having it on the page already (hidden) then making it
     * show up after a comment submission would require changing how the modal works in other places
     * (i.e. making it purely JS vs using the th:if and thymeleaf variables).
     *
     * @return A html fragment containing the modal
     */
    @GetMapping("/warning-modal")
    public ModelAndView getWarningModal(Model model) {
        logger.info("GET /warning-modal");

        model.addAttribute("fifthInappropriateSubmission", "true");

        return new ModelAndView("fragments/inappropriate-warning-modal", model.asMap());
    }

    /**
     * For the 'load more comments' button, gets comment from DB in ordered by like count, recency and page
     *
     * @param postId the ID of the post
     * @param page   the page number for which comments are retrieved from
     * @return List of comments retrieved
     */
    @GetMapping("/post/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long postId,
                                                           @RequestParam int page,
                                                           @RequestHeader(required = false) String excludedIdsHeader) {
        logger.info("GET /post/" + postId + "/comments");

        List<Long> excludedIds = new ArrayList<>();
        if (excludedIdsHeader != null && !excludedIdsHeader.isEmpty()) {
            //Help from Copilot to stream ids to list of Longs
            try {
                Arrays.stream(excludedIdsHeader.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .forEach(excludedIds::add);
                //Functionality works without excluded ids, just nice to have.
            } catch (Exception ignored) {
                logger.error("Failed to parse excluded comment ids");
            }
        }

        List<Comment> comments;
        if (page == 0) {
            //First page of comments must be treated differently
            comments = commentService.getInitialCommentsByPostIdExcludingIds(postId, excludedIds);
        } else {
            comments = commentService.getCommentsByPostIdExcludingIds(postId, excludedIds, page);
        }

        Map<String, Object> response = new HashMap<>();
        if (comments.isEmpty()) {
            response.put("message", "No comments found");
        } else {
            response.put("comments", comments);
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



}
