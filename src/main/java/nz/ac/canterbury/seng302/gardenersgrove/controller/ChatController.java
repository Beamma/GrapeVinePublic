package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.sun.tools.jconsole.JConsoleContext;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ChatMessageDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Chat;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.ChatService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ChatMessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {
    Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final UserService userService;
    private final ChatService chatService;
    private final ChatMessageValidator chatMessageValidator;

    @Autowired
    public ChatController(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
        this.chatMessageValidator = new ChatMessageValidator();
    }

    /**
     * publishes a message to the chat room with id {roomId}
     * the message is broadcast to subscribed users of the respective chat room.
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessageDTO publishChatMessage(ChatMessageDTO message) {
        logger.info("Message sent: " + message.getMessage());
        System.out.println(message.getUserId());
        return message;

    }

    /**
     * POST Endpoint for validating a chat message
     * @param requestBody httpRequest body
     * @return returns responseEntity to be processed on client side. Returns status 400 (has errors) or 200 (valid comment)
     */
    @PostMapping("/chat/add")
    public ResponseEntity<Object> addMessage(@RequestBody Map<String, String> requestBody) {
    logger.info("POST /chat/add");

        String chatMessage = requestBody.get("message");
        String name = requestBody.get("name");
        Long streamId = Long.parseLong(requestBody.get("streamId"));
        LocalDateTime timePosted = LocalDateTime.parse(requestBody.get("timePosted"));
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO(chatMessage, streamId, name, timePosted, userService.getCurrentUser().getId());
        chatMessageValidator.validateChatMessage(chatMessageDTO);

        if (chatMessageDTO.getError() != null) {
            logger.info("POST /chat/add: ERROR STATE");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(chatMessageDTO.getError());
        }
        logger.info("POST /chat/add: SUCCESS STATE");
        chatService.addChat(chatMessageDTO);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * GET endpoint for lazy loading the ten most recent chats before the most recent chat already visible
     * @param streamId the id of the stream for which we are fetching chats
     * @param olderThanString a string representing the time that the oldest message currently visible was sent
     * @return a ResponseEntity containing a list of up to 10 chats
     */
    @GetMapping("/chat/{streamId}/get-old-comments")
    public ResponseEntity<Object> getOldComments(@PathVariable("streamId") long streamId, @RequestParam String olderThanString) {
        LocalDateTime olderThan = LocalDateTime.parse(olderThanString);
        List<Chat> chats = chatService.getOlderChatsThanByStream(streamId, olderThan);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("chats", chats));
    }

}