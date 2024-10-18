package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ChatMessageDTO;

import java.time.LocalDateTime;

@Entity
@Table(name = "CHAT")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    /**
     * The id of the stream/chatroom the chat message is in
     */
    private Long streamId;

    /**
     * The author of the chat message
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    /**
     * When the chat was sent
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_posted", nullable = false, updatable = false)
    private LocalDateTime timePosted;

    /**
     * What the chat says
     */
    @Column
    private String message;

    public Chat() {}

    public Chat(ChatMessageDTO chatMessageDTO, User owner) {
        this.streamId = chatMessageDTO.getStreamId();
        this.owner = owner;
        this.timePosted = chatMessageDTO.getTimePosted();
        this.message = chatMessageDTO.getMessage();
    }

    public Long getChatId() {
        return chatId;
    }

    public Long getStreamId() {
        return streamId;
    }

    public User getOwner() {
        return owner;
    }

    public LocalDateTime getTimePosted() {
        return timePosted;
    }

    public String getMessage() {
        return message;
    }
}
