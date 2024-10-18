package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;

import java.util.*;

/**
 * Entity class reflecting an entry of name, location and size.
 * Note the @link{Entity} annotation required for declaring this as a persistence entity
 */
@Entity
@Table(name = "GARDEN")
public class Garden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gardenId;

    @ManyToOne
    @JoinColumn(name="userId")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column
    private Double size;

    @OneToMany(mappedBy = "garden", fetch = FetchType.EAGER)
    private List<Plant> plants;

    @Column(nullable = false)
    private boolean isPublicGarden;

    @Column(length = 2048)
    private String description;

    @Column(nullable = true)
    private boolean weatherMessageDismissed;

    @Column(nullable = true)
    private Date weatherDismissalDate;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "Garden_Tag",
        joinColumns = @JoinColumn(name = "garden_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @Column
    private String addressLine1;

    @Column
    private String suburb;

    @Column
    private String postcode;

    @Column
    private String city;

    @Column
    private String country;
    @Column
    private Double longitude;

    @Column
    private Double latitude;



    /**
     * JPA required no-args constructor
     */
    protected Garden() {}

    /**
     * Creates a new Garden object
     * @param name name of garden
     * @param location location of garden
     */

    public Garden(String name, AddressDTO location, User user) {
        this.name = name;
        this.user = user;
        this.addressLine1 = location.getAddressLine1();
        this.suburb = location.getSuburb();
        this.postcode = location.getPostcode();
        this.city = location.getCity();
        this.country = location.getCountry();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.isPublicGarden = false;
        this.weatherDismissalDate = null;
        this.weatherMessageDismissed = false;
    }

    public Garden(String name, AddressDTO location, Double size, User user) {
        this.name = name;
        this.addressLine1 = location.getAddressLine1();
        this.suburb = location.getSuburb();
        this.postcode = location.getPostcode();
        this.city = location.getCity();
        this.country = location.getCountry();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.size = size;
        this.user = user;
        this.isPublicGarden = false;
        this.weatherDismissalDate = null;
        this.weatherMessageDismissed = false;
    }

    public Garden(String name, AddressDTO location, Double size, String description, User user) {
        this.name = name;
        this.addressLine1 = location.getAddressLine1();
        this.suburb = location.getSuburb();
        this.postcode = location.getPostcode();
        this.city = location.getCity();
        this.country = location.getCountry();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.size = size;
        this.description = description;
        this.isPublicGarden = false;
        this.user = user;
        this.weatherDismissalDate = null;
        this.weatherMessageDismissed = false;
    }

    public Garden(GardenDTO gardenDTO, User user) {
        this.name = gardenDTO.getGardenName();
        this.addressLine1 = gardenDTO.getLocation().getAddressLine1();
        this.suburb = gardenDTO.getLocation().getSuburb();
        this.postcode = gardenDTO.getLocation().getPostcode();
        this.city = gardenDTO.getLocation().getCity();
        this.country = gardenDTO.getLocation().getCountry();
        this.longitude = gardenDTO.getLocation().getLongitude();
        this.latitude = gardenDTO.getLocation().getLatitude();
        this.size = gardenDTO.getSizeAsDouble();
        this.description = gardenDTO.getDescription();
        this.isPublicGarden = false;
        this.user = user;
        this.weatherDismissalDate = null;
        this.weatherMessageDismissed = false;
    }

    public Long getGardenId() {
        return gardenId;
    }

    public String getName() {
        return name;
    }

    public AddressDTO getLocation() {
        return new AddressDTO(addressLine1, suburb, postcode, city, country, longitude, latitude);
    }

    public Double getSize() {
        return size;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(AddressDTO location) {
        this.addressLine1 = location.getAddressLine1();
        this.suburb = location.getSuburb();
        this.postcode = location.getPostcode();
        this.city = location.getCity();
        this.country = location.getCountry();
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isPublicGarden() {
        return isPublicGarden;
    }

    public void setPublicGarden(boolean isPublicGarden) {
        this.isPublicGarden = isPublicGarden;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    /**
     * Get tags in list form to display in alphabetical order on garden view page
     * check for null before using sorting methods
     * @return list of ordered Tag objects
     */
    public List<Tag> getTagsOrdered() {
        if (this.tags == null) {
            return Collections.emptyList();
        } else {
            return this.tags.stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .toList();
        }
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void setWeatherMessageDismissed(boolean dismissed) {
        this.weatherMessageDismissed = dismissed;
        if (dismissed) {
            this.weatherDismissalDate = new Date();
        }
    }

    public boolean getWeatherMessageDismissed() {
        return this.weatherMessageDismissed;
    }

    public Date getWeatherDismissalDate() {
        return this.weatherDismissalDate;
    }

    public void setWeatherDismissalDate(Date date) {
        this.weatherDismissalDate = date;
    }

    @Override
    public String toString() {
        return "Garden{" +
                "id=" + gardenId +
                ", name='" + name + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", suburb='" + suburb + '\'' +
                ", postcode='" + postcode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", size=" + size + "m^2" +
                ", plants=[" + plants + "]" +
                '}';
    }
}
