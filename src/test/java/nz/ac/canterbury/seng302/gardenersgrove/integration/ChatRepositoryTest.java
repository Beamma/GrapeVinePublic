package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.dto.ChatMessageDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.ChatRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("test")
public class ChatRepositoryTest {
    Livestream livestream;
    User user;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LivestreamRepository livestreamRepository;

    @Autowired
    ChatRepository chatRepository;

    private LocalDateTime sampleTime;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("liam@email.com",
                "2000-01-01",
                "Liam",
                "Ceelen",
                false,
                "Password1!",
                "Password1!"));
        livestream = livestreamRepository.save(new Livestream(user, "Livestream", "", "fake/path"));
        sampleTime = LocalDate.of(2024, Month.SEPTEMBER, 30).atStartOfDay();
    }

    @AfterEach
    public void cleanUp() {
        livestreamRepository.deleteAll();
        chatRepository.deleteAll();
    }

    private void nChatsExist(int n) {
        LocalDateTime now = sampleTime;
        IntStream.range(1, n + 1).forEach(i -> {
                    ChatMessageDTO dto = new ChatMessageDTO("message" + i, livestream.getId(), user.getFirstName(), now.minusMinutes(i), 1L);
                    chatRepository.save(new Chat(dto, user));
                }
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void upToTenMessages_getsAllMessages(int n) {
        nChatsExist(n);
        List<Chat> chats = chatRepository.findTop10ByStreamIdAndTimePostedBeforeOrderByTimePostedAsc(livestream.getId(), sampleTime);
        Assertions.assertEquals(n, chats.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 20})
    void moreThanTenMessages_gets10Messages(int n) {
        int expectedNumChats = 10;

        nChatsExist(n);
        List<Chat> chats = chatRepository.findTop10ByStreamIdAndTimePostedBeforeOrderByTimePostedAsc(livestream.getId(), sampleTime);
        Assertions.assertEquals(expectedNumChats, chats.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 20})
    void moreThanTenMessages_getsMostRecent10Messages(int n) {
        int expectedNumChats = 10;

        nChatsExist(n);
        List<Chat> chats = chatRepository.findTop10ByStreamIdAndTimePostedBeforeOrderByTimePostedAsc(livestream.getId(), sampleTime);

        Assertions.assertArrayEquals(IntStream.range(1, expectedNumChats + 1).toArray(),
                chats.stream().mapToInt(c -> Integer.parseInt(c.getMessage().substring(7))).toArray());
    }

    @Test
    void recentMessagesExist_getsMessagesBeforeAGivenTime() {
        int expectedNumChats = 10;
        int numMinsAgo = 3;
        LocalDateTime oldestRenderedChat = sampleTime.minusMinutes(numMinsAgo).minusSeconds(30);

        nChatsExist(15);
        List<Chat> chats = chatRepository.findTop10ByStreamIdAndTimePostedBeforeOrderByTimePostedAsc(livestream.getId(), oldestRenderedChat);

        Assertions.assertArrayEquals(IntStream.range(numMinsAgo + 1, numMinsAgo + expectedNumChats + 1).toArray(),
                chats.stream().mapToInt(c -> Integer.parseInt(c.getMessage().substring(7))).toArray());
    }
}
