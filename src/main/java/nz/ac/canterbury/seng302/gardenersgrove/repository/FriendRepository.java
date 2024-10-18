package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friend;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Friend repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface FriendRepository extends CrudRepository<Friend, Long> {
    Optional<Friend> findById(long id);

    List<Friend> findAll();

    @Query(value = "SELECT * FROM FRIEND WHERE RECIPIENT =?1 AND STATUS='pending'", nativeQuery = true)
    Optional<List<Friend>> findAllPendingByUser2Id(long recipient);

    @Query(value = "SELECT * FROM FRIEND WHERE SENDER =?1 AND (STATUS='pending' OR STATUS='declined')", nativeQuery = true)
    Optional<List<Friend>> findAllPendingAndDeclinedBySenderId(long sender);
    
    @Query(value = "SELECT * FROM FRIEND WHERE (RECIPIENT =?1 AND SENDER =?2) OR (RECIPIENT =?2 AND SENDER =?1)", nativeQuery = true)
    Optional<List<Friend>> findAllPendingByBothUsers(long sender, long recipient);

    @Query(value = "SELECT * FROM FRIEND WHERE ((RECIPIENT =?1 AND SENDER =?2) OR (RECIPIENT =?2 AND SENDER =?1)) AND STATUS='accepted'", nativeQuery = true)
    Optional<List<Friend>> findAllAcceptedByBothUserId(long sender, long recipient);


}
