package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseFeedDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Comment;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentLikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LikeRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * Service functions for posts
 * provides access to post repository.
 */
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    /**
     * Constructor
     *
     * @param postRepository provides database access to posts
     * @param commentRepository provides database access to comments
     * @param likeRepository provides database access to post likes
     * @param commentLikeRepository provides database access to comment likes
     */
    @Autowired
    public PostService(PostRepository postRepository, CommentRepository commentRepository, LikeRepository likeRepository, CommentLikeRepository commentLikeRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    /**
     * Add a post to the database
     *
     * @param postDTO entity instance
     * @return the saved post
     */
    public Post addPost(PostDTO postDTO) {
        String fileName = null;

        // Save image if exists
        if (postDTO.getImage() != null && !postDTO.getImage().isEmpty()) {
            fileName = imageService.saveImage(postDTO.getImage());
        }

        // Create post
        Post post = new Post(postDTO, fileName, userService.getCurrentUser());

        // Save and return post
        return postRepository.save(post);
    }

    /**
     * Gets all posts from the database
     * @return a list of all posts
     */
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * Checks that the DTO pages are integers and larger than 0, if not sets them the page in the DTO as 1
     *
     * @param browseFeedDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void parsePages(BrowseFeedDTO browseFeedDTO) {
        int parsedPage;
        try {
            parsedPage = Integer.parseInt(browseFeedDTO.getPage());
            if (parsedPage < 1) {
                parsedPage = 1;
            }
        } catch (NumberFormatException e) {
            parsedPage = 1;
        }

        browseFeedDTO.setParsedPage(parsedPage);
    }
    /**
     * Retrieve a post by post id
     * @param id id of the post
     * @return optional post object
     */
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    /**
     * Pass in a DTO with all the required information, and it will query the database, returning posts
     *
     * @param browseFeedDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void getPosts(BrowseFeedDTO browseFeedDTO) {
        //Gets the pages using the query seen below
        Page<Post> postsPage = queryRepository(browseFeedDTO);
        //For total posts number (the 'of' for pagination)
        int numberOfPosts = (int) postsPage.getTotalElements();
        // If the user has input a page size that is larger than possible:
        if (browseFeedDTO.getParsedPage() > postsPage.getTotalPages()) {
            browseFeedDTO.setParsedPage(postsPage.getTotalPages());
            if (numberOfPosts != 0) {
                postsPage = queryRepository(browseFeedDTO);
            }
        }
        //Setting the feedDTO
        List<Post> feedPosts = postsPage.getContent();
        browseFeedDTO.setTotalPages(postsPage.getTotalPages());
        browseFeedDTO.setNumberOfPosts(numberOfPosts);
        browseFeedDTO.setPosts(feedPosts);
    }

    /**
     * Handles Making the query
     * @param browseFeedDTO is a DTO to carry and transfer all data between service layer, controller and view
     * @return returns a Page of Posts
     */
    private Page<Post> queryRepository(BrowseFeedDTO browseFeedDTO) {
        Page<Post> postsPage = postRepository.findRecentPosts(PageRequest.of(browseFeedDTO.getParsedPage() - 1, browseFeedDTO.getPageSize()));
        return postsPage != null ? postsPage : Page.empty();
    }

    /**
     * Delete post by ID.
     *
     * @param id the post id
     */
    public void deletePost(Long id) {

        // Get all post comments
        for (Comment comment : commentRepository.findCommentsByPostId(id)) {
            // Delete comment likes
            commentLikeRepository.deleteCommentLikesByCommentId(comment.getId());

            // Delete comment
            commentRepository.deleteById(comment.getId());
        }

        // Delete post likes
        likeRepository.removeByPostId(id);

        // Delete post
        postRepository.deleteById(id);
    }
}
