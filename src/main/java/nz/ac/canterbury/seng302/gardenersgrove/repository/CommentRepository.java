package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    Optional<Comment> findById(Long id);

    List<Comment> findAll();

    @Query(value = "SELECT * FROM COMMENT WHERE POST_ID = %?1% ORDER BY LIKE_COUNT DESC, DATE_COMMENTED DESC LIMIT 10 OFFSET %?2%", nativeQuery = true)
    List<Comment> findPagedCommentsByPostId(Long postId, int offset);

    @Query(value = "SELECT * FROM COMMENT WHERE POST_ID = %?1% ORDER BY LIKE_COUNT DESC, DATE_COMMENTED DESC LIMIT 10 OFFSET 3", nativeQuery = true)
    List<Comment> findInitialPagedCommentsByPostId(Long postId);

    @Query(value = "SELECT * FROM COMMENT WHERE POST_ID = %?1% ", nativeQuery = true)
    List<Comment> findCommentsByPostId(Long postId);

    @Query(value = "SELECT * FROM COMMENT WHERE POST_ID = %?1% AND COMMENT_ID NOT IN %?2% ORDER BY LIKE_COUNT DESC, DATE_COMMENTED DESC LIMIT 10 OFFSET %?3%", nativeQuery = true)
    List<Comment> findPagedCommentsByPostIdExcludingIds(Long postId, List<Long> excludedIds, int offset);

    @Query(value = "SELECT * FROM COMMENT WHERE POST_ID = %?1% AND COMMENT_ID NOT IN %?2% ORDER BY LIKE_COUNT DESC, DATE_COMMENTED DESC LIMIT 10 OFFSET 3", nativeQuery = true)
    List<Comment> findInitialPagedCommentsByPostIdExcludingIds(Long postId, List<Long> excludedIds);

}