package nz.ac.canterbury.seng302.gardenersgrove.entity;

import jakarta.persistence.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Entity class reflecting an entry of a plant.
 */
@Entity
@Table(name = "PLANT")
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long plantId;

    @ManyToOne
    @JoinColumn(name="gardenId")
    private Garden garden;

    @Column(nullable = false)
    private String name;

    private Integer count;

    private Integer imageSize;

    @Column(columnDefinition = "VARCHAR(512)")
    private String description;

    private Date datePlanted;


    /**
     * JPA required no-args constructor
     */
    protected Plant() {
    }

    /**
     * Creates a new Plant object
     *
     * @param garden      garden the plant is in
     * @param name        name of plant
     * @param count       number of plants (optional)
     * @param description description of plant (optional)
     * @param datePlanted date the plant was planted (optional)
     * @param plantImage  image of the plant (optional)
     */
    public Plant(Garden garden, String name, Integer count, String description, Date datePlanted, String plantImage) {
        this.garden = garden;
        this.name = name;
        this.count = count;
        this.description = description;
        this.datePlanted = datePlanted;
        this.plantImagePath = plantImage;
        setPlantImage(plantImage);

    }

    /**
     * Gets the id of the plant
     * @return the id of the plant
     */
    public long getPlantId() {
        return plantId;
    }

    /**
     * Gets the name of the plant
     * @return the name of the plant
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the count of the plant
     * @return the count of the plant
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Gets the description of the plant
     * @return the description of the plant
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the date the plant was planted
     * @return the date the plant was planted
     */
    public Date getDatePlanted() {
        return datePlanted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setDate(Date date) {
        this.datePlanted = date;
    }

    public void setGarden(Garden garden) {
        this.garden = garden;
    }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + plantId +
                ", name='" + name + '\'' +
                ", count=" + count +
                ", description='" + description + '\'' +
                ", datePlanted=" + datePlanted +
                '}';
    }

    @Column(columnDefinition = "VARCHAR(512)")
    private String plantImagePath;

    public String getPlantImage() {
        return plantImagePath;
    }


    public void setPlantImage(String image) {
        this.plantImagePath = image;
        try {
            this.imageSize =
                    Files.readAllBytes(Path.of("src/main/resources/plant-images/" + plantImagePath)).length;
        } catch (IOException e) {
            this.imageSize = 0;
        }
    }


    public String getPlantImageBase64() throws IOException {
        Path imagePath = Paths.get("src/main/resources/plant-images/" + plantImagePath);
        Logger.getLogger(Plant.class.getName()).info("Attempting to access image at: " + imagePath.toAbsolutePath());
        if (imagePath.toString().split("\\.").length == 1) {
            return null;
        }
        String extension = imagePath.toString().split("\\.")[1];
        // SVG images cannot be encoded
        if ("svg".equals(extension) || "svg+xml".equals(extension)) {
            return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of("src/main/resources/plant-images/" + plantImagePath)));
        } else {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(Path.of("src/main/resources/plant-images/" + plantImagePath)));
        }
    }

    public Integer getPlantImageSize() {
        return imageSize;
    }





}
