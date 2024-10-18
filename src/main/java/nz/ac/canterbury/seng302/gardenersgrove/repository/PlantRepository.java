package nz.ac.canterbury.seng302.gardenersgrove.repository;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Plant repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface PlantRepository extends CrudRepository<Plant, Long> {
    Optional<Plant> findById(Long id);
    List<Plant> findAll();

    @Query(value = "SELECT * FROM PLANT WHERE PLANT_ID =?1 and GARDEN_ID =?2", nativeQuery = true)
    Optional<Plant> findByIdAndGardenId(Long plantId, Long gardenId);
}
