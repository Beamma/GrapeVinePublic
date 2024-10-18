package nz.ac.canterbury.seng302.gardenersgrove.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a chat message
 * */
public class ChatMessageDTO {
    private String message;
    private String error;
    private Long streamId;
    private String name;

    private Long userId;

    private LocalDateTime timePosted;


    public ChatMessageDTO() {
    }

    public ChatMessageDTO(String message, Long streamId, String name, LocalDateTime timePosted, Long userId) {
        this.message = message;
        this.streamId = streamId;
        this.name = name;
        this.timePosted = timePosted;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() { return error; }

    public void setError(String error) { this.error = error; }

    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(LocalDateTime timePosted) {
        this.timePosted = timePosted;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
