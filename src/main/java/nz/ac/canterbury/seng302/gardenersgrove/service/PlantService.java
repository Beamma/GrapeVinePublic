package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.exception.PlantNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.utility.PlantImageStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Service class for Plants, defined by the @link{Service} annotation.
 */
@Service
public class PlantService {
    private final PlantRepository plantRepository;

    public static ArrayList<String> errors = new ArrayList<String>(Arrays.asList(
            "dateError", "descriptionError", "imageError", "countError", "nameError"
    ));

    /**
     * Constructor for PlantService
     *
     * @param plantRepository the repository to use
     */
    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    /**
     * Gets all Plants from persistence
     *
     * @return all Plants currently saved in persistence
     */
    public List<Plant> getPlants() {
        return plantRepository.findAll();
    }

    /**
     * Adds a Plant to persistence
     *
     * @param plant object to persist
     * @return the saved Plant object
     */
    public Plant addPlant(Plant plant) {
        return plantRepository.save(plant);
    }

    /**
     * Gets a Plant from persistence by id
     *
     * @return Plant currently saved in persistence that matches the provided id
     */
    public Optional<Plant> getPlantByID(Long id) {
        return plantRepository.findById(id);
    }

    public Optional<Plant> getPlantByIdAndGardenId(Long id, Long gardenId) {
        return plantRepository.findByIdAndGardenId(id, gardenId);
    }

    public Plant updatePlant(Long plantId, String plantName, String description, Integer count, Date datePlanted, String plantImagePath) throws PlantNotFoundException {
        Optional<Plant> possiblePlant = plantRepository.findById(plantId);

        if (possiblePlant.isEmpty()) {
            throw new PlantNotFoundException(String.format("Plant with the id %d does not exist", plantId));
        }
        Plant plant = possiblePlant.get();
        plant.setName(plantName);
        plant.setDescription(description);
        plant.setCount(count);
        plant.setDate(datePlanted);
        plant.setPlantImage(plantImagePath);

        plantRepository.save(plant);
        return plant;
    }

    private Path rootLocation;
    private Path cacheLocation;

    public void setStorageProperties(PlantImageStorageProperties properties) {
        if (properties.getLocation().trim().isEmpty()) {
            throw new RuntimeException("error");
        }
        this.rootLocation = Paths.get(properties.getLocation());
        this.cacheLocation = Paths.get(properties.getCacheLocation());
        properties.createLocation();

    }

    // All image storing methods are based on the tutorial at https://spring.io/guides/gs/uploading-files/
    public void store(MultipartFile file, String fileName, String extension) {
        try {
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(fileName + "." + extension)).toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("error in filepath");

            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource((file.toUri()));
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    public void deleteFile(String filename) {
        FileSystemUtils.deleteRecursively(rootLocation.resolve(filename).toFile());
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Checks the model and view object for any errors
     *
     * @param modelAndView object containing errors
     * @return true if any errors exist, otherwise false.
     */
    public boolean hasErrors(ModelAndView modelAndView) {
        for (String error : errors) {
            if (modelAndView.getModel().containsKey(error)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cache an image for the plant if the form needs repopulated
     * Identify the cached image by the userId
     *
     * @param plantImage A multipart file of the plantImage
     */
    public void cachePlantImage(MultipartFile plantImage, String userId) {
        clearPlantImageCache(userId);
        String extension = Objects.requireNonNull(plantImage.getOriginalFilename()).split("\\.")[1];
        String fileName = "plant" + userId;
        try {
            Path destinationFile = this.cacheLocation.resolve(Paths.get(fileName + "." + extension)).toAbsolutePath();
            if (!destinationFile.getParent().equals(this.cacheLocation.toAbsolutePath())) {
                throw new RuntimeException("error in filepath");
            }
            try (InputStream inputStream = plantImage.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.getLogger(PlantService.class.getName()).log(Level.INFO, e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            Logger.getLogger(PlantService.class.getName()).log(Level.INFO, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Clear any cached plant image of the current user
     *
     * @param userId the id of the user
     */
    public void clearPlantImageCache(String userId) {
        Path cachedPlantImage = getCachedPlantImagePath(userId);
        if (cachedPlantImage != null) {
            try {
                Files.delete(cachedPlantImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get the cached plant image of the current user
     *
     * @param userId the id of the user
     * @return the path to the cached plant image or null if none exist
     */
    private Path getCachedPlantImagePath(String userId) {
        String fileName = "plant" + userId;
        // Copilot suggested using this method to find the image without knowing the extension
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(cacheLocation, fileName + "*")) {
            for (Path entry : stream) {
                return entry;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Function for displaying the cached plant image of the current user
     *
     * @param userId the id of the user
     * @return the base64 string of the cached plant image or null if none exist
     */
    public String getCachedPlantImageBase64(String userId) throws IOException {
        Path imagePath = getCachedPlantImagePath(userId);
        if (imagePath == null) {
            return null;
        }
        String extension = imagePath.toString().split("\\.")[1];
        // SVG images cannot be encoded
        if ("svg".equals(extension)) {
            return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(
                    Files.readAllBytes(Path.of(cacheLocation.toString() + "/" + imagePath.getFileName())));
        } else {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(
                    Files.readAllBytes(Path.of(cacheLocation.toString() + "/" + imagePath.getFileName())));
        }
    }

    /**
     * Store the cached plant image of the current user to the plant image directory
     * and clear the cache.
     * For use when the user has successfully submitted the form.
     *
     * @param userId the id of the user
     * @param fileName the name of the file to store
     *
     * @return the name of the stored file
     */
    public String storeCachedPlantImage(String userId, String fileName) {
        Path cachedPlantImage = getCachedPlantImagePath(userId);
        if (cachedPlantImage != null) {
            String extension = cachedPlantImage.toString().split("\\.")[1];
            try {
                Path destinationFile = this.rootLocation.resolve(Paths.get(fileName + "." + extension)).toAbsolutePath();
                if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                    throw new RuntimeException("error in filepath");
                }
                Files.copy(cachedPlantImage, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                clearPlantImageCache(userId);
                return fileName + "." + extension;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Check if a cached plant image exists for the current user
     *
     * @param userId the id of the user
     * @return true if a cached plant image exists, otherwise false
     */
    public boolean cachedPlantImageExists(String userId) {
        return getCachedPlantImagePath(userId) != null;
    }
}
