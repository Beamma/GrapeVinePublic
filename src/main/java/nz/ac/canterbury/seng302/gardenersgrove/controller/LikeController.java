package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.CommentService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LikeService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PostService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * For sending json requests back and forth from the post page, without refreshing it
 * using the JavaScript fetch API
 */
@RestController
public class LikeController {

    Logger logger = LoggerFactory.getLogger(LikeController.class);
    private final UserService userService;

    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    @Autowired
    public LikeController(UserService userService, PostService postService, LikeService likeService, CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    // POST LIKES

    /**
     * For adding a like to a post
     *
     * @param id the id of the post
     * @return the like count in json format
     */
    @PutMapping("/post/{id}/like")
    public ResponseEntity<Object> addLikeToPost(@PathVariable Long id) {
        logger.info("PUT /post/{}/like", id);

        User user = userService.getCurrentUser();
        Post post = postService.getPostById(id);

        if (post == null) {
            String message = "Post not found";
            return ResponseEntity.badRequest().body(message);
        } else {
            likeService.addLike(post, user);
        }


        int likeCount = likeService.getLikeCountByPostId(id);

        return ResponseEntity.ok(likeCount);
    }

    /**
     * For getting the like count for a post
     *
     * @param id the id of the post
     * @return the like count in json format or an error message
     */
    @GetMapping("/post/{id}/like")
    public ResponseEntity<Map<String, Object>> getLikeCount(@PathVariable Long id) {
        logger.info("GET /post/" + id + "/like");

        User user = userService.getCurrentUser();
        Post post = postService.getPostById(id);

        if (post == null) {
            String message = "Post not found";
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }

        int likeCount = likeService.getLikeCountByPostId(id);
        boolean likeExists = likeService.likeExists(post, user);
        Map<String, Object> response = Map.of("likeCount", likeCount, "likeExists", likeExists);


        return ResponseEntity.ok(response);
    }

    /**
     * For removing a like from a post
     *
     * @param id the id of the post
     * @return the like count in json format or an error message
     */
    @DeleteMapping("/post/{id}/like")
    public ResponseEntity<Object> removeLikeFromPost(@PathVariable Long id) {
        logger.info("DELETE /post/{}/like", id);

        User user = userService.getCurrentUser();
        Post post = postService.getPostById(id);

        if (post == null) {
            String message = "Post not found";
            return ResponseEntity.badRequest().body(message);
        }
        if (likeService.likeExists(post, user)) { // Check if user has liked this post already
            likeService.removeLike(post, user);
        } else {
            String message = "User has not liked this post";
            return ResponseEntity.badRequest().body(message);
        }

        int likeCount = likeService.getLikeCountByPostId(id);

        return ResponseEntity.ok(likeCount);
    }

    // COMMENT LIKES

    /**
     * For adding a like to a comment
     * @param commentId the id of the comment or an error message
     * @return the like count in json format
     */
    @PutMapping("/comment/{commentId}/like")
    public ResponseEntity<Object> addLikeToComment(@PathVariable Long commentId) {
        logger.info("PUT /comment/{}/like", commentId);

        Comment comment = commentService.getCommentById(commentId);
        User user = userService.getCurrentUser();

        if (comment == null) {
            String message = "Comment not found";
            return ResponseEntity.badRequest().body(message);
        }

        if (!likeService.commentLikeExists(user, comment)) { // Check if user has liked this comment already
            likeService.addCommentLike(user, comment);
        }

        int likeCount = likeService.getCommentLikeCountByCommentId(commentId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * For getting the like count for a comment
     * @param commentId the id of the comment
     * @return the like count in json format
     */
    @GetMapping("/comment/{commentId}/like")
    public ResponseEntity<Map<String, Object>> getCommentLikeCount(@PathVariable Long commentId) {
        logger.info("GET /comment/{}/like", commentId);

        Comment comment = commentService.getCommentById(commentId);
        User user = userService.getCurrentUser();

        if (comment == null) {
            String message = "Comment not found";
            return ResponseEntity.badRequest().body(Map.of("error", message));
        }

        int likeCount = likeService.getCommentLikeCountByCommentId(commentId);
        boolean likeExists = likeService.commentLikeExists(user, comment);
        Map<String, Object> response = Map.of("likeCount", likeCount, "likeExists", likeExists);

        return ResponseEntity.ok(response);
    }

    /**
     * For removing a like from a comment
     *
     * @param commentId the id of the post
     * @return the like count in json format
     */
    @DeleteMapping("/comment/{commentId}/like")
    public ResponseEntity<Object> removeLikeFromComment(@PathVariable Long commentId) {
        logger.info("DELETE /comment/{}/like", commentId);

        Comment comment = commentService.getCommentById(commentId);
        User user = userService.getCurrentUser();

        if (comment == null) {
            String message = "Comment not found";
            return ResponseEntity.badRequest().body(message);
        }

        if (likeService.commentLikeExists(user, comment)) { // Check if user has liked this comment already
            likeService.removeCommentLike(user, comment);
        } else {
            String message = "User has not liked this comment";
            return ResponseEntity.badRequest().body(message);
        }

        int likeCount = likeService.getCommentLikeCountByCommentId(commentId);

        return ResponseEntity.ok(likeCount);
    }
}
