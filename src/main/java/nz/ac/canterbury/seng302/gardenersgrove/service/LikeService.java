package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentLikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for the business logic of liking posts
 */
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommentLikeRepository commentLikeRepository;

    private final CommentRepository commentRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, CommentLikeRepository commentLikeRepository, CommentRepository commentRepository) {
        this.likeRepository = likeRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Add a like to the database. (Like a post)
     * @param post the post that was liked
     * @param user the user who liked the post
     */
    public void addLike(Post post, User user) {
        Likes like = new Likes(post, user);
        likeRepository.save(like);
    }



    /**
     * Remove a like from the database (unlike)
     * @param post the post that was liked.
     * @param user the user who liked the post
     */
    public void removeLike(Post post, User user) {
        likeRepository.removeByUserAndPost(user.getId(), post.getId());
    }

    /**
     * Check if a like exists for a given post and user
     * @param post the post that was liked
     * @param user the user who liked the post
     * @return true if the user has already liked this post
     */
    public boolean likeExists(Post post, User user) {
        return likeRepository.getByPostAndUser(user.getId(), post.getId()).isPresent();
    }

    /**
     * Get the amount of likes for a certain post
     * @param postId the id of the post
     * @return the number of likes the given post has
     */
    public int getLikeCountByPostId(Long postId) {
        List<Likes> likes = likeRepository.getByPostId(postId).orElse(null);

        if (likes == null) {
            return 0;
        }

        return likes.size();
    }



    /**
     * Add a like to the database. (Like a comment)
     * @param user the user who liked the comment
     * @param comment the comment that was liked
     */
    public void addCommentLike(User user, Comment comment) {
        comment.incrementLikeCount();
        commentRepository.save(comment);
        LikeComment like = new LikeComment(comment, user);
        commentLikeRepository.save(like);
    }

    /**
     * Remove a comment like from the database (unlike)
     * @param user the user who liked the comment
     * @param comment the comment that was liked
     */
    public void removeCommentLike(User user, Comment comment) {
        comment.decrementLikeCount();
        commentRepository.save(comment);
        commentLikeRepository.removeByUserAndComment(user.getId(), comment.getId());
    }

    /**
     * Check if a like exists for a given comment and user
     * @param user the user who liked the comment
     * @param comment the comment that was liked
     * @return true if the user has already liked this comment
     */

    public boolean commentLikeExists(User user, Comment comment) {
        return commentLikeRepository.getByUserAndComment(user.getId(), comment.getId()).isPresent();
    }

    /**
     * Get the amount of likes for a certain comment
     * @return the number of likes the given comment has
     */

    public int getCommentLikeCountByCommentId(Long commentId) {
        List<LikeComment> likes =
                commentLikeRepository.getByCommentId(commentId).orElse(null);

        if (likes == null) {
            return 0;
        }

        return likes.size();
    }


}
