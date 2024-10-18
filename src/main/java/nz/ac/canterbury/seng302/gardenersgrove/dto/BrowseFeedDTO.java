package nz.ac.canterbury.seng302.gardenersgrove.dto;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;

import java.util.List;

/**
 * Data transfer object for data of the browse feed
 */
public class BrowseFeedDTO {
    private String page;
    private int pageSize = 10;
    private int parsedPage;
    private int totalPages;
    private int numberOfPosts;
    private List<Post> posts;
    /**
     * Constructor
     * @param page page for the browse feed feature
     */
    public BrowseFeedDTO(String page) {
        this.page = page;
    }
    /**
     * Gets the current page
     * @return the current page
     */
    public String getPage(){
        return this.page;
    }
    /**
     * @param posts A list of post objects
     */
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    /**
     * @return A list of posts
     */
    public List<Post> getPosts() {
        return this.posts;
    }
    /**
     * Sets the parsedPage
     * @param parsedPage a parsedPage that can be trusted to be true
     */
    public void setParsedPage(int parsedPage){
        this.parsedPage = parsedPage;
    }
    /**
     * @return a parsedPage that can be trusted to be true
     */
    public int getParsedPage(){
        return this.parsedPage;
    }

    /**
     * @return How many posts each page contains
     */
    public int getPageSize(){
        return this.pageSize;
    }
    /**
     * @param pages Total amount of pages required to display all posts
     */
    public void setTotalPages(int pages){
        this.totalPages = pages;
    }

    /**
     * @return Total amount of pages required to display all posts
     */
    public int getTotalPages() {
        return this.totalPages;
    }
    /**
     * @param pageSize How many posts per page
     */
    public void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }
    /**
     * @param numberOfPosts Sets the total number of posts
     */
    public void setNumberOfPosts(int numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }
    /**
     * @return Total number of posts for pagination
     */
    public int getNumberOfPosts() {
        return this.numberOfPosts;
    }
}
