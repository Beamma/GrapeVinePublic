package nz.ac.canterbury.seng302.gardenersgrove.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * Data transfer object for creating live streams.
 */
public class LiveStreamDTO {

    private String title;

    private String description;

    private MultipartFile image;


    /**
     * The constructor for create live stream DTO.
     * @param title         The title of the live stream
     * @param description   The optional description of the live stream
     */
    public LiveStreamDTO(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // Getters and Setters

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public MultipartFile getImage() {
        return this.image;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public boolean imageExists() {
        return image != null && !image.isEmpty();
    }

    public String getImagePreview() throws IOException {
        return Base64.getEncoder().encodeToString(image.getBytes());
    }
}

