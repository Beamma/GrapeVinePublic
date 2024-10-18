package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.CommentDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service functions for comments.
 * Provides access to comment repository.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private static final int COMMENT_PAGE_SIZE = 10;
    private static final int COMMENT_OFFSET_SIZE = 13;
    /**
     * Constructor.
     *
     * @param commentRepository provides database access.
     */
    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Add a comment to the database.
     *
     * @param commentDTO entity instance
     */
    public Comment addComment(CommentDTO commentDTO) {
        Comment comment = new Comment(commentDTO.getPost(), commentDTO.getUser(), commentDTO.getComment());
        return commentRepository.save(comment);
    }


    /**
     * Gets comment by id.
     *
     * @param id entity instance
     * @return a comment.
     */
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    /**
     * Gets all comments from the database.
     *
     * @return a list of all comments.
     */
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    /**
     * Gets the Initial 10 comments by post id, offset 3 in the repository method.
     *
     * @param id post id
     * @return a list of all comments.
     */
    public List<Comment> getInitialCommentsByPostId(Long id) {
        return commentRepository.findInitialPagedCommentsByPostId(id);
    }

    /**
     * Gets the Next 10 comments by post id
     * @param id post id
     * @param page the page number to get comments from, used as a multiplier for the offset
     * @return
     */
    public List<Comment> getCommentsByPostId(Long id, int page) {
        //we use 1 since we are zero index based
        int offset = ((page - 1) * COMMENT_PAGE_SIZE) + COMMENT_OFFSET_SIZE;
        return commentRepository.findPagedCommentsByPostId(id, offset);
    }

    /**
     * Gets the Initial 10 comments by post id, offset 3 in the repository method
     * and skipping over any ids that appear in excludedIds (i.e. comments the user has posted without reloading)
     * @param id post id
     * @param excludedIds ids to be excluded from the query
     * @return Page List of comments
     */
    public List<Comment> getInitialCommentsByPostIdExcludingIds(Long id, List<Long> excludedIds) {
        if (excludedIds.isEmpty()) {
            return getInitialCommentsByPostId(id);
        }
        return commentRepository.findInitialPagedCommentsByPostIdExcludingIds(id, excludedIds);
    }

    /**
     * Gets the next 10 comments by post id excluding the comments in excluded id list.
     * @param id post id
     * @param excludedIds ids of comments to be excluded from the query
     * @param page the page number to get comments from, used as a multiplier for the offset
     * @return page list of comments
     */
    public List<Comment> getCommentsByPostIdExcludingIds(Long id, List<Long> excludedIds, int page) {
        if (excludedIds.isEmpty()) {
            return getCommentsByPostId(id, page);
        }
        int offset = ((page - 1) * COMMENT_PAGE_SIZE) + COMMENT_OFFSET_SIZE;
        return commentRepository.findPagedCommentsByPostIdExcludingIds(id, excludedIds, offset);
    }

}
