package nz.ac.canterbury.seng302.gardenersgrove.dto;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * A DTO representing a post object
 */
public class PostDTO {
    private String title;
    private String content;
    private MultipartFile image;

    private Long gardenId;
    private Garden linkedGarden;

    public PostDTO() {}

    /**
     * Constructor
     * @param title title (heading) of the post
     * @param content text body of the post
     */
    public PostDTO(String title, String content) {
        this.title= title;
        this.content = content;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public MultipartFile getImage() {
        return this.image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public Long getGardenId() {
        return gardenId;
    }

    public void setGardenId(Long gardenId) {
        this.gardenId = gardenId;
    }

    /**
     * Returns the base64 encoding for the image, to be used in retaining the image preview
     * @return the base64 encoding for the image
     * @throws IOException if string encoding breaks
     */
    public String getImagePreview() throws IOException {
        return Base64.getEncoder().encodeToString(image.getBytes());
    }

    /**
     * Returns whether the image exists.
     * Used for deciding whether to run the image repopulation script, where we need to check for the image being null
     * or empty
     * @return true if the image exists, false otherwise
     */
    public boolean imageExists() {
        return image != null && !image.isEmpty();
    }

    public void setGarden (Garden garden) {
        this.linkedGarden = garden;
    }

    public Garden getLinkedGarden() {
        return this.linkedGarden;
    }
}
