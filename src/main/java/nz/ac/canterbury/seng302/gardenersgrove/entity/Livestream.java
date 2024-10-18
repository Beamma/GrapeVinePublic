package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

/**
 * Entity class for a livestream.
 */
@Entity
@Table(name= "LIVESTREAM")
public class Livestream {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "livestream_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    private int watchCount = 0;

    @Column(nullable = true)
    private String imagePath = null;

    /**
     * JPA required no-args constructor
     */
    public Livestream() {}

    /**
     * The constructor for the live stream entity.
     *
     * @param user          The owner of the live stream
     * @param title         The title of the live stream
     * @param description   The description of the live stream
     * @param imagePath     The image path of the live stream thumbnail
     */
    public Livestream(User user, String title, String description, String imagePath) {
        this.owner = user;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(int watchCount) {
        this.watchCount = watchCount;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}