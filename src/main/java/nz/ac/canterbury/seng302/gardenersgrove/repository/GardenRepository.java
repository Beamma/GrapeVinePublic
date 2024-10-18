package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Garden repository accessor using Spring's @link{CrudRepository}.
 * These (basic) methods are provided for us without the need to write our own implementations
 */
@Repository
public interface GardenRepository extends CrudRepository<Garden, Long> {
    Optional<Garden> findById(Long id);

    List<Garden> findAll();

    List<Garden> findByUser_Id(Long userId);

    //Based on information from https://www.baeldung.com/spring-data-jpa-query
    List<Garden> findByIsPublicGarden(boolean isPublicGarden);

    @Query(value = "SELECT DISTINCT GARDEN.GARDEN_ID, GARDEN.DESCRIPTION, GARDEN.IS_PUBLIC_GARDEN,  GARDEN.ADDRESS_LINE1, GARDEN.CITY, GARDEN.COUNTRY, GARDEN.LATITUDE, GARDEN.LONGITUDE, GARDEN.NAME, GARDEN.POSTCODE, GARDEN.SUBURB, GARDEN.WEATHER_DISMISSAL_DATE, GARDEN.WEATHER_MESSAGE_DISMISSED, GARDEN.SIZE, GARDEN.USER_ID " +
            "FROM GARDEN " +
            "LEFT JOIN PLANT ON GARDEN.GARDEN_ID = PLANT.GARDEN_ID " +
            "WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE AND GARDEN.NAME LIKE %?1% OR PLANT.NAME LIKE %?1% " +
            "ORDER BY GARDEN.GARDEN_ID DESC", nativeQuery = true)
    List<Garden> findAllGardensByName(String keyword, String userId);

    @Query(value = "SELECT * FROM GARDEN WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE ORDER BY GARDEN_ID DESC", nativeQuery = true)
    Page<Garden> findRecentPublicGardens(Pageable pageable);

    @Query(value = "SELECT DISTINCT GARDEN.GARDEN_ID, GARDEN.DESCRIPTION, GARDEN.IS_PUBLIC_GARDEN, GARDEN.ADDRESS_LINE1, GARDEN.CITY, GARDEN.COUNTRY, GARDEN.LATITUDE, GARDEN.LONGITUDE, GARDEN.POSTCODE, GARDEN.SUBURB, GARDEN.WEATHER_DISMISSAL_DATE, GARDEN.WEATHER_MESSAGE_DISMISSED, GARDEN.NAME, GARDEN.SIZE, GARDEN.USER_ID " +
            "FROM GARDEN " +
            "LEFT JOIN PLANT ON GARDEN.GARDEN_ID = PLANT.GARDEN_ID " +
            "LEFT JOIN GARDEN_TAG ON GARDEN_TAG.GARDEN_ID = GARDEN.GARDEN_ID " +
            "LEFT JOIN TAG ON GARDEN_TAG.TAG_ID = TAG.TAG_ID " +
            "WHERE (GARDEN.IS_PUBLIC_GARDEN = TRUE) AND (GARDEN.NAME LIKE %:search% OR PLANT.NAME LIKE %:search%) AND TAG.NAME IN :tags " +
            "ORDER BY GARDEN.GARDEN_ID DESC", nativeQuery = true)
    Page<Garden> findAllGardensByNameAndTagsPageable(@Param("search") String keyword, @Param("tags") List<String> tags, Pageable pageable);

    @Query(value = "SELECT DISTINCT GARDEN.GARDEN_ID, GARDEN.DESCRIPTION, GARDEN.IS_PUBLIC_GARDEN, GARDEN.ADDRESS_LINE1, GARDEN.CITY, GARDEN.COUNTRY, GARDEN.LATITUDE, GARDEN.LONGITUDE, GARDEN.POSTCODE, GARDEN.SUBURB, GARDEN.WEATHER_DISMISSAL_DATE, GARDEN.WEATHER_MESSAGE_DISMISSED, GARDEN.NAME, GARDEN.SIZE, GARDEN.USER_ID " +
            "FROM GARDEN " +
            "LEFT JOIN PLANT ON GARDEN.GARDEN_ID = PLANT.GARDEN_ID " +
            "WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE AND GARDEN.NAME LIKE %?1% OR PLANT.NAME LIKE %?1% " +
            "ORDER BY GARDEN.GARDEN_ID DESC", nativeQuery = true)
    Page<Garden> findAllPublicGardensByNamePageable(String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT GARDEN.GARDEN_ID, GARDEN.DESCRIPTION, GARDEN.IS_PUBLIC_GARDEN, GARDEN.ADDRESS_LINE1, GARDEN.CITY, GARDEN.COUNTRY, GARDEN.LATITUDE, GARDEN.LONGITUDE, GARDEN.POSTCODE, GARDEN.SUBURB, GARDEN.WEATHER_DISMISSAL_DATE, GARDEN.WEATHER_MESSAGE_DISMISSED, GARDEN.NAME, GARDEN.SIZE, GARDEN.USER_ID " +
            "FROM GARDEN " +
            "LEFT JOIN PLANT ON GARDEN.GARDEN_ID = PLANT.GARDEN_ID " +
            "LEFT JOIN GARDEN_TAG ON GARDEN_TAG.GARDEN_ID = GARDEN.GARDEN_ID " +
            "LEFT JOIN TAG ON GARDEN_TAG.TAG_ID = TAG.TAG_ID " +
            "WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE AND TAG.NAME IN :tags " +
            "ORDER BY GARDEN.GARDEN_ID DESC", nativeQuery = true)
    Page<Garden> findAllPublicGardensByTagsPageable(@Param("tags") List<String> tags, Pageable pageable);

    @Query(value = "SELECT DISTINCT GARDEN.GARDEN_ID, GARDEN.DESCRIPTION, GARDEN.IS_PUBLIC_GARDEN, GARDEN.ADDRESS_LINE1, GARDEN.CITY, GARDEN.COUNTRY, GARDEN.LATITUDE, GARDEN.LONGITUDE, GARDEN.POSTCODE, GARDEN.SUBURB, GARDEN.WEATHER_DISMISSAL_DATE, GARDEN.WEATHER_MESSAGE_DISMISSED, GARDEN.NAME, GARDEN.SIZE, GARDEN.USER_ID " +
            "FROM GARDEN " +
            "WHERE GARDEN.IS_PUBLIC_GARDEN = TRUE AND UPPER(GARDEN.NAME) LIKE %?2% AND GARDEN.USER_ID LIKE %?1% " +
            "ORDER BY GARDEN.GARDEN_ID DESC LIMIT ?3", nativeQuery = true)
    List<Garden> findAllPublicGardensByUserIdByNameWithLimit(Long userId, String upperCaseName, int limit);

}