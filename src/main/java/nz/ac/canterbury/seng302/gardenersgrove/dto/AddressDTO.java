package nz.ac.canterbury.seng302.gardenersgrove.dto;

import java.util.Objects;

/**
 * Data transfer object for addresses/locations of gardens
 */
public class AddressDTO {
    /**
     * First address line of the address (the street address). e.g. 20 Kirkwood Avenue
     */
    private String addressLine1;

    /**
     * The suburb of the location, e.g. Ilam
     */
    private String suburb;

    /**
     * The postcode of the address, e.g. 8041
     */
    private String postcode;

    /**
     * The city of the address, e.g. Christchurch
     * This field is required
     */
    private String city;

    /**
     * The country of the address, e.g. New Zealand
     * This field is required
     */
    private String country;

    /**
     * The longitude of the address.
     * This is a hidden field in the location form section, and is not null if and only if the location has been
     * autofilled by the API
     */
    private Double longitude;

    /**
     * The latitude of the address.
     * This is a hidden field in the location form section, and is not null if and only if the location has been
     * autofilled by the API
     */
    private Double latitude;

    /**
     * No argument constructor, to create an empty DTO, the values of which can be set with the setters.
     * Used in testing and by Thymeleaf in the form
     */
    public AddressDTO() {}

    /**
     * Constructor with all args. Used in testing and in Garden::getLocation to create a DTO from the pieces of
     * information saved in the Garden object
     * @param addressLine1 street address for the AddressDTO object
     * @param suburb suburb for the AddressDTO object
     * @param postcode postcode for the AddressDTO object
     * @param city city for the AddressDTO object
     * @param country country for the AddressDTO object
     * @param longitude longitude for the AddressDTO object
     * @param latitude latitude for the AddressDTO object
     */
    public AddressDTO(String addressLine1, String suburb, String postcode, String city, String country, Double longitude, Double latitude) {
        this.addressLine1 = addressLine1;
        this.suburb = suburb;
        this.postcode = postcode;
        this.city = city;
        this.country = country;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns a formatted version of the full location.
     * A comma-separated list of parts of the address that increase in size, e.g. 20 Kirkwood Avenue, Ilam, Christchurch
     * Always includes city and country
     * @return the full location in a comma separated list of address components (if present)
     */
    public String formatLocation() {
        StringBuilder formattedLocation = new StringBuilder();
        if (addressLine1 != null && !addressLine1.isBlank()) {
            formattedLocation.append(addressLine1).append(", ");
        }
        if (suburb != null && !suburb.isBlank()) {
            formattedLocation.append(suburb).append(", ");
        }
        assert city != null && country != null; // Compulsory fields so should be impossible
        formattedLocation.append(city).append(", ").append(country);
        return formattedLocation.toString();
    }

    /**
     * Returns a short formatted version of the full location.
     * Returns the most specific part of the address
     * @return the most specific part of the address
     */
    public String formatShortLocation() {
        if (addressLine1 != null && !addressLine1.isBlank()) {
            return addressLine1;
        }
        if (suburb != null && !suburb.isBlank()) {
           return suburb;
        }
        return city;
    }

    /**
     * Checks for equality with other objects.
     * If other is another AddressDTO with identical fields, the objects are equal, and they are not otherwise
     * @param other the object being checked for equality
     * @return true if the objects are equal, and false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        AddressDTO otherAddressDTO = (AddressDTO) other;
        return Objects.equals(addressLine1, otherAddressDTO.addressLine1) &&
                Objects.equals(suburb, otherAddressDTO.suburb) &&
                Objects.equals(postcode, otherAddressDTO.postcode) &&
                Objects.equals(city, otherAddressDTO.city) &&
                Objects.equals(country, otherAddressDTO.country) &&
                Objects.equals(longitude, otherAddressDTO.longitude) &&
                Objects.equals(latitude, otherAddressDTO.latitude);
    }
}
