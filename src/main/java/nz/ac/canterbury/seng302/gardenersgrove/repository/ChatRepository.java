package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends CrudRepository<Chat, Long> {

    /**
     * Gets the (up to) ten most recent chats that occurred before a given time, with the most recent first
     * @param streamId the id of the stream in which the chats were sent
     * @param timePosted we want the ten most recent messages before this time
     * @return  a list of chats matching the above criteria
     */
    @Query(value = "SELECT * FROM (SELECT * " +
            "FROM CHAT " +
            "WHERE STREAM_ID =?1 AND TIME_POSTED < ?2 " +
            "ORDER BY TIME_POSTED DESC LIMIT 10) " +
            "AS recent_chats ORDER BY TIME_POSTED DESC;", nativeQuery = true)
    List<Chat> findTop10ByStreamIdAndTimePostedBeforeOrderByTimePostedAsc(Long streamId, LocalDateTime timePosted);

}
