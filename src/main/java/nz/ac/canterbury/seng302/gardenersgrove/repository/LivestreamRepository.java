package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The repository for the live stream entity.
 */
@Repository
public interface LivestreamRepository extends CrudRepository<Livestream, Long> {

    /**
     * Gets all live streams.
     *
     * @return List of found live streams
     */
    List<Livestream> findAll();

    /**
     * Finds a live stream by title (for testing).
     *
     * @param title     The title
     * @return          List of found live streams.
     */
    List<Livestream> findLivestreamByTitle(String title);

    Optional<Livestream> findByOwner(User owner);

    /**
     * Finds livestream and paginates them using Pageable
     * @param pageable Page size and current page
     * @return A Page of livestreams of length of page size
     */
    @Query(value = "SELECT * FROM LIVESTREAM ORDER BY LIVESTREAM_ID DESC", nativeQuery = true)
    Page<Livestream> findCurrentLivestreams(Pageable pageable);
}