package nz.ac.canterbury.seng302.gardenersgrove.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.dto.PostDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Post entity
 * Contains columns: PostId, OwnerId, Title, and Content
 */
@Entity
@Table(name = "POST")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @OneToMany(mappedBy = "post")
    @JsonManagedReference // Manage serialization of the parent reference
    private List<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @Column
    private String title;

    @Column(nullable = false, length = 2048)
    private String content;

    @Column(nullable = true)
    private String imagePath = null;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_posted", nullable = false, updatable = false)
    private LocalDateTime datePosted;

    @ManyToOne
    @JoinColumn(name = "linked_garden", nullable = true)
    private Garden linkedGarden;

    protected Post() {
    }

    /**
     * When the entity is saved to the database, the current date and time is recorded and saved.
     */
    @PrePersist
    protected void onCreate() {
        if (datePosted == null) {
            datePosted = LocalDateTime.now();
        }
    }

    public Post(String title, String content, User owner) {
        this.title = title;
        this.content = content;
        this.owner = owner;
    }

    public Post(String title, String content, LocalDateTime datePosted, User owner) {
        this.title = title;
        this.content = content;
        this.datePosted = datePosted;
        this.owner = owner;
    }

    public Post(PostDTO postDTO, String imagePath, User owner) {
        this.title = postDTO.getTitle();
        this.content = postDTO.getContent();
        this.imagePath = imagePath;
        this.owner = owner;
        this.linkedGarden = postDTO.getLinkedGarden();

    }

    public Long getId() {
        return this.postId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public User getOwner() {
        return this.owner;
    }

    /**
     * Calculates the time elapsed since the post was created and returns a formatted string.
     * The format is as follows:
     * If the post was created less than a minute ago, returns the number of seconds, e.g., "13 Seconds"
     * If the post was created less than an hour ago, returns the number of minutes, e.g., "42 Minutes".
     * If the post was created more than an hour ago but less than a day ago, returns the number of hours rounded down, e.g., "3 Hours".
     * If the post was created more than a day ago, returns the number of days rounded down, e.g., "3 Days".
     * If the post was created more than a week ago, returns the number of weeks rounded down, e.g., "2 Weeks".
     *
     * @return A string representing the time elapsed since the post was created.
     */
    public String getTimeSincePosted() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(datePosted, now);

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
     *
     * @param date a date and time of when the post was made
     */
    public void setDate(LocalDateTime date) {
        datePosted = date;
    }

    /**
     * Method for testing purposes only
     *
     * @param id set an artificial ID of a Post entity
     */
    public void setId(Long id) {
        this.postId = id;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    /**
     * Gets the comments associated with the post
     *
     * @return the comments on this post
     */
    public List<Comment> getComments() {
        //dev merge, add limit of 3
        return this.comments
                .stream()
                .sorted(Comparator
                        .comparing(Comment::getLikeCount, Comparator.reverseOrder())
                        .thenComparing(Comment::getDateCommented, Comparator.reverseOrder())
                )
                .limit(3)
                .toList();
    }

    public long getCommentsSize() {
        return this.comments.size();
    }

    public Garden getLinkedGarden() {
        return this.linkedGarden;
    }

}
