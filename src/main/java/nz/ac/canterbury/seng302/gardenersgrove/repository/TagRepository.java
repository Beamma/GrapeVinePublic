package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for tags: Interacts with the database when looking for tags
 */
@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {

    Optional<Tag> findByName(String tag);

    List<Tag> findAll();

    @Query(value = "SELECT DISTINCT TAG.TAG_ID, TAG.NAME " +
            "FROM TAG " +
            "LEFT JOIN GARDEN_TAG ON TAG.TAG_ID = GARDEN_TAG.TAG_ID " +
            "LEFT JOIN GARDEN ON GARDEN_TAG.GARDEN_ID = GARDEN.GARDEN_ID " +
            "WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE", nativeQuery = true)
    List<Tag> findDistinctTagsFromPublicGardens();

    /**
     * Finds all public tags that start with the string {@code startsWith}
     * @param startsWith the string that begins the tags we are trying to find
     * @return a list of all the tags that begin with the string {@code startsWith}
     */
    @Query(value = "SELECT DISTINCT TAG.TAG_ID, TAG.NAME " +
            "FROM TAG " +
            "LEFT JOIN GARDEN_TAG ON TAG.TAG_ID = GARDEN_TAG.TAG_ID " +
            "LEFT JOIN GARDEN ON GARDEN_TAG.GARDEN_ID = GARDEN.GARDEN_ID " +
            "WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE " +
            "AND UPPER(TAG.NAME) LIKE UPPER(CONCAT(?1, '%'))" +
            "ORDER BY TAG.NAME", nativeQuery = true)
    List<Tag> findDistinctTagsFromPublicGardensOrderByNameStartingWith(String startsWith);
}
