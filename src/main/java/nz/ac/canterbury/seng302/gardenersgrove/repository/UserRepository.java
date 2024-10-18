package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository connected to UserService
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Optional to deal with null cases of user existence
     * Extends CrudRepository for built-in methods
     */
    Optional<User> findById(long id);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM USERS WHERE (EMAIL = ?1) AND (USER_ID != ?2)", nativeQuery = true)
    Page<User> findAllByEmail(String email, Pageable pageable, String userId);

    @Query(value = "SELECT * FROM USERS WHERE ((NO_LAST_NAME = TRUE AND FIRST_NAME LIKE ?1) OR (NO_LAST_NAME = FALSE AND CONCAT(FIRST_NAME, ' ', LAST_NAME) LIKE ?1)) AND USER_ID != ?2", nativeQuery = true)
    Page<User> findByFullName(String keyword, Pageable pageable, String userId);

    Optional<User> findByToken(String token);

    boolean existsByEmail(String email);

    List<User> findAll();

    @Query(value = "SELECT * FROM USERS WHERE USERS.USER_ID != ?1", nativeQuery = true)
    Optional<List<User>> findAllButOwn(long id);

    @Query(value = "SELECT USERS.* FROM FRIEND LEFT JOIN USERS ON  FRIEND.SENDER = USERS.USER_ID OR FRIEND.RECIPIENT = USERS.USER_ID WHERE USERS.USER_ID !=?1 AND (FRIEND.SENDER =?1 OR FRIEND.RECIPIENT =?1) AND FRIEND.STATUS = 'accepted'", nativeQuery = true)
    Optional<List<User>> getAllCurrentUsersFriends(long id);
}
