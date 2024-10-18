package nz.ac.canterbury.seng302.gardenersgrove.utility;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@ConfigurationProperties("storage.plant-images")
public class PlantImageStorageProperties {
    private String location =  "src/main/resources/plant-images";
    private String cacheLocation = location + "/cache/";
    public String getLocation() {
        return location;
    }
    public String getCacheLocation() { return cacheLocation; }
    public void setLocation(String location) {
        this.location = location;
        this.cacheLocation = location + "/cache";
        createLocation();
    }

    public void createLocation() {
        try {
            Files.createDirectories(Paths.get(location));
            Files.createDirectories(Paths.get(cacheLocation));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }
}