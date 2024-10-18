package nz.ac.canterbury.seng302.gardenersgrove.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.entity.LikeComment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends CrudRepository<LikeComment, Long> {
    /**
     * Gets a like by comment id and user id
     */
    @Query(value = "SELECT * FROM LIKE_COMMENT WHERE USER_ID =?1 AND COMMENT_ID =?2", nativeQuery = true)
    Optional<LikeComment> getByUserAndComment(Long userId, Long commentId);

    /**
     * Gets a list of likes by comment id
     */

    @Query(value = "SELECT * FROM LIKE_COMMENT WHERE COMMENT_ID =?1", nativeQuery = true)
    Optional<List<LikeComment>> getByCommentId(Long commentId);

    /**
     * Remove (unlike) a comment
     * @param userId the user unliking the comment
     * @param commentId the comment being unliked
     */
    @Transactional
    @Modifying
    @Query( value = "DELETE FROM LIKE_COMMENT WHERE USER_ID =?1 AND COMMENT_ID =?2", nativeQuery = true)
    void removeByUserAndComment(Long userId, Long commentId);

    /**
     * Remove all likes from a comment
     * This is used when a post is deleted (and by extension, its comments)
     * Makes sure that all the likes are also deleted to avoid orphaned data
     * @param commentId the comment being unliked
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM LIKE_COMMENT WHERE COMMENT_ID = ?1", nativeQuery = true)
    void deleteCommentLikesByCommentId(Long commentId);

}