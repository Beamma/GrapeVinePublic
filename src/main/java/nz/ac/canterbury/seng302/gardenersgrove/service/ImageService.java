package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service class responsible for handling image file storage.
 */
@Service
public class ImageService {

    /**
     * The directory where uploaded images are stored.
     */
    private static final String UPLOAD_DIR = "src/main/resources/public/";

    /**
     * The root location where images are stored.
     */
    private final Path rootLocation;

    /**
     * Constructor.
     * Initializes the root location path.
     */
    public ImageService() {
        this.rootLocation = Paths.get(UPLOAD_DIR);
        createDirectoryIfNotExists();
    }

    /**
     * Creates the directory if it does not already exist.
     */
    private void createDirectoryIfNotExists() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    /**
     * Saves an image file to the designated upload directory.
     *
     * @param imageFile the image file to be saved
     * @return the saved images file name
     * @throws RuntimeException if an error occurs
     */
    public String saveImage(MultipartFile imageFile) {
        try {
            // Generate a unique file name
            String originalFilename = imageFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            String fileName = UUID.randomUUID().toString() + "." + extension;

            // Determine the destination file path
            Path destinationFile = rootLocation.resolve(Paths.get(fileName)).toAbsolutePath();

            // Check if the destination file is within the upload directory
            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new RuntimeException("File path error: destination is outside of the intended directory");
            }

            // Save the file
            try (InputStream inputStream = imageFile.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store the image", e);
        }
    }

    /**
     * Loads an image path.
     *
     * @param filename of the image
     * @return the image path
     */
    public Path loadImage(String filename) {
        return rootLocation.resolve(filename).normalize().toAbsolutePath();
    }
}