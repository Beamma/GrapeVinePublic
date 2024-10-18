package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;


/**
 * Entity class reflecting an entry of a friendship, between user1 and user2 with a status
 */
@Entity
@Table(name = "FRIEND")
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="sender")
    private User sender;

    @ManyToOne
    @JoinColumn(name="recipient")
    private User recipient;

    @Column()
    private String status = "pending";

    /**
     * JPA required no-args constructor
     */
    protected Friend() {}

    /**
     * Creates a new Friend object
     * @param sender User who sent request
     * @param recipient User who request was sent to
     */
    public Friend(User sender, User recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }

    public Long getFriendId() {
        return id;
    }

    /**
     * Gets the user who's sending the request
     * @return User who sent request
     */
    public User getSender() {
        return sender;
    }

    /**
     * Gets the user who's receiving the request
     * @return User who receives request
     */
    public User getRecipient() {
        return recipient;
    }

    /**
     * Gets status of friend request
     * @return status of the friend request
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status of friend request
     * @param status of the friend request
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", user1='" + sender.toString() + '\'' +
                ", user2='" + recipient.toString() + '\'' +
                ", status=" + status +
                '}';
    }
}
