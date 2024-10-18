package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.ChatMessageDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Chat;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ChatRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    private final UserService userService;

    @Autowired
    public ChatService(ChatRepository chatRepository, UserService userService) {
        this.chatRepository = chatRepository;
        this.userService = userService;
    }

    /**
     * saves a chat
     * @param chatMessageDTO the chat
     * @return the saved chat
     */
    public Chat addChat(ChatMessageDTO chatMessageDTO) {
        User owner = userService.getCurrentUser();
        return chatRepository.save(new Chat(chatMessageDTO, owner));
    }

    /**
     * Gets a list of the (up to) ten most recent chats from before the earliest chat message currently present
     * @param streamId the id of the stream for which we are fetching the chats
     * @param olderThan we want the ten most recent messages before this time
     * @return a list of up to 10 Chat objects that are the next ten most recent chats to show to the user
     */
    public List<Chat> getOlderChatsThanByStream(Long streamId, LocalDateTime olderThan) {
        return chatRepository.findTop10ByStreamIdAndTimePostedBeforeOrderByTimePostedAsc(streamId, olderThan);
    }
}
