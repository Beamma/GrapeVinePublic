package nz.ac.canterbury.seng302.gardenersgrove.dto;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;

/**
 * Data transfer object for gardens
 */
public class GardenDTO {
    /**
     * The name of the garden (not null)
     */
    private String gardenName;

    /**
     * The (optional) description for a garden
     */
    private String description;

    /**
     * The (optional) sie of a garden.
     * Stored as a string, as this is how it is represented on the garden forms
     */
    private String size;

    /**
     * The location of the garden, of which all fields are optional except for the city and the country
     */
    private AddressDTO location;

    /**
     * Constructor for a garden DTO
     * @param name the name of the garden
     * @param description the (optional) description of the garden
     * @param size the (optional) size of the garden
     * @param location the location of the garden
     */
    public GardenDTO(String name, String description, String size, AddressDTO location) {
        this.gardenName = name;
        this.description = description;
        this.size = size;
        this.location = location;
    }

    public String getGardenName() {
        return gardenName;
    }

    /**
     * Returns the garden's description, or null if the description is empty
     * @return the garden's description, or null if the description is the empty string
     */
    public String getDescription() {
        return description == null || description.isEmpty() ? null : description;
    }

    public String getSize() {
        return size;
    }

    /**
     * Returns the garden's size parsed into a double if it exists, or else null
     * @return The garden's size converted to a float if it exists, null otherwise
     */
    public Double getSizeAsDouble() {
        return size == null || size.isEmpty() ? null : GardenUtils.parseSize(size);
    }

    public AddressDTO getLocation() {
        return location;
    }

    public void setGardenName(String gardenName) {
        this.gardenName = gardenName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setLocation(AddressDTO location) {
        this.location = location;
    }
}
