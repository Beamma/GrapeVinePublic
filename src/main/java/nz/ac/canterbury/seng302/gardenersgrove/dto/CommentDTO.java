package nz.ac.canterbury.seng302.gardenersgrove.dto;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Post;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

/**
 * An object for transferring DATA about a comment around.
 */
public class CommentDTO {
    private String comment;

    private Boolean containsProfanity = false;

    private String error;

    private User user;
    private Post post;

    public CommentDTO(String comment, User user, Post post) {
        this.comment = comment;
        this.user = user;
        this.post = post;
    }

    public void setContainsProfanity(Boolean containsProfanity) {
        this.containsProfanity = containsProfanity;
    }

    public Boolean isProfane() {
        return containsProfanity;
    }

    public String getComment() {
        return this.comment;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return this.error;
    }

    public User getUser() {
        return this.user;
    }

    public Post getPost() {
        return this.post;
    }
}
