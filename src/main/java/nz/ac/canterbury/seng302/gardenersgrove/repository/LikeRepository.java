package nz.ac.canterbury.seng302.gardenersgrove.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Likes;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends CrudRepository<Likes, Long> {

    /**
     * Get post entity by a userId and by a postId
     * @param userId ID of the user who's liked the post
     * @param postId ID of the post that has been liked
     * @return The like entity that matches the userId and the postId
     */
    @Query(value = "SELECT * FROM LIKES WHERE USER_ID =?1 AND POST_ID =?2", nativeQuery = true)
    Optional<Likes> getByPostAndUser(Long userId, Long postId);

    /**
     * Get post entity by a postId
     * @param id ID of the post that has been liked
     * @return The like entity that matches the postId
     */
    @Query(value = "SELECT * FROM LIKES WHERE POST_ID =?1", nativeQuery = true)
    Optional<List<Likes>> getByPostId(Long id);

    /**
     * Remove (unlike) a post
     * @param userId the user unliking the post
     * @param postId the post being unliked
     */
    @Transactional
    @Modifying
    @Query( value = "DELETE FROM LIKES WHERE USER_ID =?1 AND POST_ID =?2", nativeQuery = true)
    void removeByUserAndPost(Long userId, Long postId);

    /**
     * Remove likes from a post
     * @param postId the post being unliked
     */
    @Transactional
    @Modifying
    @Query( value = "DELETE FROM LIKES WHERE POST_ID =?1", nativeQuery = true)
    void removeByPostId(Long postId);
}
