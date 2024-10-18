package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;

import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Service class for Gardens, defined by the @link{Service} annotation.
 * This class links automatically with @link{GardenRepository}, see the @link{Autowired} annotation below
 */
@Service
public class GardenService {
    private GardenRepository gardenRepository;
    private UserService userService;

    private TagRepository tagRepository;

    private static final int LINK_GARDEN_RESULT_LIMIT = 20;


    @Autowired
    public GardenService(GardenRepository gardenRepository, UserService userService, TagRepository tagRepository) {
        this.gardenRepository = gardenRepository;
        this.userService = userService;
        this.tagRepository = tagRepository;
    }

    /**
     * Gets all Gardens from persistence
     *
     * @return all Gardens currently saved in persistence
     */
    public List<Garden> getGardens() {
        return gardenRepository.findAll();
    }

    /**
     * Gets all Gardens for a specified user
     * @param id
     * @return all gardens for given user
     */
    public List<Garden> getGardensByUserId(Long id) {
        return gardenRepository.findByUser_Id(id);
    }

    /**
     * Gets a Garden from persistence by id
     *
     * @return Garden currently saved in persistence that matches the provided id
     */
    public Optional<Garden> getGardenByID(Long id) {
        return(gardenRepository.findById(id));
    }

    /**
     * Adds a Garden to persistence
     *
     * @param garden object to persist
     * @return the saved Garden object
     */
    public Garden addGarden(Garden garden) {
        return gardenRepository.save(garden);
    }

    /**
     * Edits Garden in persistence
     * @param id the id of the garden in persistence
     * @param gardenName the new name of the garden
     * @param addressDTO the new location of the garden
     * @param size the new size of the garden
     * @param description the new description of the garden
     * @return the new garden
     */
    public Garden updateGarden(Long id, String gardenName, AddressDTO addressDTO, Double size,
                               String description) throws GardenNotFoundException {
        if (gardenRepository.findById(id).isEmpty()) {
            throw new GardenNotFoundException(String.format("Garden with the id %d does not exist", id));
        }
        Garden garden = gardenRepository.findById(id).get();
        garden.setName(gardenName);
        garden.setLocation(addressDTO);
        garden.setSize(size);
        garden.setDescription(description);
        gardenRepository.save(garden);
        return garden;
    }

    /**
     * Updates garden publicity
     * @param id the id of the garden
     * @param isPublic boolean for public/not
     * @return the new garden
     */
    public Garden updateGardenPublicity(Long id, boolean isPublic) throws GardenNotFoundException {
        Optional<Garden> possibleGarden = gardenRepository.findById(id);
        if (possibleGarden.isEmpty()) {
            throw new GardenNotFoundException(String.format("Garden with the id %d does not exist", id));
        }
        Garden garden = possibleGarden.get();
        garden.setPublicGarden(isPublic);
        gardenRepository.save(garden);
        return garden;
    }

    /**
     * Dismiss the weather message
     * @param id the id of the garden in persistence
     * @param weatherDismissed boolean is message dismissed or not
     * @return the garden
     */
    public Garden dismissGardenWeatherMessage(Long id, boolean weatherDismissed) throws GardenNotFoundException {
        Optional<Garden> possibleGarden = gardenRepository.findById(id);
        if (possibleGarden.isEmpty()) {
            throw new GardenNotFoundException(String.format("Garden with the id %d does not exist", id));
        }
        Garden garden = possibleGarden.get();
        garden.setWeatherMessageDismissed(weatherDismissed);
        gardenRepository.save(garden);
        return garden;
    }

    /**
     * Gets all public gardens from persistence
     * @return list of all gardens that have isPublicGarden set to true
     */
    public List<Garden> getPublicGardens() {
        return gardenRepository.findByIsPublicGarden(true);
    }

    /**
     * Check garden ownership
     * @param gardenId id of garden you want to check ownership for
     */
    public boolean checkGardenOwnership(Long gardenId) {
        Optional<Garden> garden = getGardenByID(gardenId);
        User currentUser = userService.getCurrentUser();
        if (garden.isEmpty()) {
            return false;
        }
        return Objects.equals(garden.get().getUser().getId(), currentUser.getId());
    }

    /**
     * Get the most recent gardens
     * @param pageRequest pagination
     * @return page of gardens
     */
    public Page<Garden> getRecentGardens(PageRequest pageRequest) {
        return gardenRepository.findRecentPublicGardens(pageRequest);
    }

    /**
     * Service for adding a tag to a garden
     * @param tagString String of tag
     * @param gardenId id of garden to add tag to
     */
    public void addTag(String tagString, Long gardenId) {
        Tag tag;
        // Check to see if Tag already exists
        Optional<Tag> databaseTags = tagRepository.findByName(tagString);
        if (databaseTags.isEmpty()) {
            // Add Tag to tag table
            tag = tagRepository.save(new Tag(tagString));
        } else {
            tag = databaseTags.get();
        }

        // Add tag to garden
        Garden garden = gardenRepository.findById(gardenId).get();
        garden.addTag(tag);
        gardenRepository.save(garden);
    }

    /**
     * Gets the list of garden tags
     * @return list of tags
     */
    public List<Tag> getTags() {
        return tagRepository.findAll();
    }

    /**
     * Finds all tags that start with the string {@code startsWith}
     * @param startsWith the string that begins the tags we are trying to find
     * @return a list of all the tags that begin with the string {@code startsWith}
     */
    public List<Tag> getAutofilledTags(String startsWith) {
        return tagRepository.findDistinctTagsFromPublicGardensOrderByNameStartingWith(startsWith);
    }

    /**
     * Gets the list of public garden tags
     * @return list of public tags
     */
    public List<Tag> getPublicTags() {
        return tagRepository.findDistinctTagsFromPublicGardens();
    }

    /**
     * Check if garden description is the same as currently saved
     * @return true if exists and the same, then description validation can be skipped
     *         else return false
     */
    public boolean checkDescriptionNoChange(Long id, String newDescription) {
        Optional<Garden> garden = getGardenByID(id);
        if (garden.isPresent()) {
            String oldDescription = garden.get().getDescription();
            return Objects.equals(oldDescription, newDescription);
        } else {
            return false;
        }
    }

    /**
     * Get all public gardens by user id, up to an upper limit (of 20)
     * @param userId the id of the user
     * @return list of public gardens
     */
    public List<Garden> searchPublicGardensByUserIdAndName(Long userId, String name) {
        return gardenRepository.findAllPublicGardensByUserIdByNameWithLimit(userId, name.toUpperCase(Locale.ROOT), LINK_GARDEN_RESULT_LIMIT);
    }
}



