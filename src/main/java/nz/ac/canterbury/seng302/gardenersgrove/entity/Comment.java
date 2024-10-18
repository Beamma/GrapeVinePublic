package nz.ac.canterbury.seng302.gardenersgrove.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.checkerframework.checker.units.qual.C;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Table for storing data on comments on posts
 */
@Entity
@Table(name= "Comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length=2048)
    private String text;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_commented", nullable = false, updatable = false)
    private LocalDateTime dateCommented;

    @Column(name = "like_count")
    private int likeCount;

    public Comment() {
        // Default constructor
    }

    @PrePersist
    protected void onCreate() {
        if (dateCommented == null) {
            dateCommented = LocalDateTime.now();
        }
    }

    public Comment(Post post, User user, String text) {
        this.post = post;
        this.user = user;
        this.text = text;
    }

    /**
     * Calculates the time elapsed since the comment was created and returns a formatted string.
     * The format is as follows:
     *     If the comment was created less than a minute ago, returns the number of seconds, e.g., "13 Seconds"
     *     If the comment was created less than an hour ago, returns the number of minutes, e.g., "42 Minutes".
     *     If the comment was created more than an hour ago but less than a day ago, returns the number of hours rounded down, e.g., "3 Hours".
     *     If the comment was created more than a day ago, returns the number of days rounded down, e.g., "3 Days".
     *     If the comment was created more than a week ago, returns the number of weeks rounded down, e.g., "2 Weeks".
     *
     * @return A string representing the time elapsed since the comment was created.
     */
    public String getTimeSincePosted() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateCommented, now);

        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        long weeks = days / 7;

        if (weeks > 0) {
            return weeks + " Week" + (weeks > 1 ? "s" : "");
        } else if (days > 0) {
            return days + " Day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " Hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " Minute" + (minutes > 1 ? "s" : "");
        } else {
            return "Less Than A Minute";
        }
    }

    /**
     * Should only be used for testing
     * @param date a date and time of when the comment was made
     */
    public void setDate(LocalDateTime date) {
        dateCommented = date;
    }

    /**
     * Method for testing purposes only
     * @param id set an artificial ID of a Comment entity
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Returns the text content of the post
     * @return text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the user who made the comment
     * @return user
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the ID of the comment
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the post commented on
     * @return post
     */
    public Post getPost() {
        return post;
    }

    /**
     * Returns the time the comment was left, used for sorting comments on a post
     * @return dateCommented
     */
    public LocalDateTime getDateCommented() {
        return dateCommented;
    }

    /**
     * Returns the number of likes the comment has
     * @return likeCount
     */

    public int getLikeCount() {
        return likeCount;
    }

    /**
     * Sets the number of likes the comment has
     * @param likeCount the number of likes the comment has
     */
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * Increments the number of likes the comment has
     */
    public void incrementLikeCount() {
        likeCount++;
    }

    /**
     * Decrements the number of likes the comment has
     */

    public void decrementLikeCount() {
        likeCount--;
    }



}
