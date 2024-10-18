package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "TAG")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    Set<Garden> gardens;

    /**
     * JPA required no-args constructor
     */
    protected Tag() {}

    /**
     * Creates a new Tag object
     * @param tagName the tag string to bed added
     */
    public Tag(String tagName) {
        this.name = tagName;
    }

    /**
     * Gets the tag id as displayed in the database
     * @return tagId as displayed in database
     */
    public Long getTagId() {
        return this.tagId;
    }

    /**
     * Gets the tag value
     * @return the value of the tag
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set tag value
     * @param tagName the value set for the tag
     */
    public void setName(String tagName) {
        this.name = tagName;
    }
}
