package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class DatabaseTest {

    @Autowired
    private GardenRepository gardenRepository;

    @Autowired
    private PlantRepository plantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TagRepository tagRepository;

    @MockBean
    private WeatherService mockWeatherService;

    User user1;

    private static EmailService emailService;
    @MockBean
    JavaMailSender mailSender;


    String validImageFilePath;
    String tooBigImageFilePath;
    String invalidTypeImageFilePath;

    AddressDTO validLocation;

    @BeforeEach
    void setUp() {
        User testUser = new User(
                "liam@email.com",
                "2000-01-01",
                "Liam",
                "Ceelen",
                false,
                "Password1!",
                "Password1!");
        user1 = userRepository.save(testUser);
        emailService = Mockito.mock(EmailService.class);
        validImageFilePath = "src/test/resources/TestImages/jpg_image.jpg";
        tooBigImageFilePath = "src/test/resources/TestImages/too_big_image.jpg";
        invalidTypeImageFilePath = "src/test/resources/TestImages/gif_valid.gif";
        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
    }

    @AfterEach
    public void cleanUp() {
        tagRepository.deleteAll();
        plantRepository.deleteAll();
        gardenRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void addGardenWithSizeTest() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, 5.0, user1));
        Optional<Garden> foundGarden = gardenRepository.findById(addedGarden.getGardenId());

        Assertions.assertNotNull(foundGarden);
        Assertions.assertTrue(foundGarden.isPresent());
        Assertions.assertEquals(addedGarden.getName(), foundGarden.get().getName());
        Assertions.assertEquals(addedGarden.getLocation(), foundGarden.get().getLocation());
        Assertions.assertEquals(addedGarden.getSize(), foundGarden.get().getSize());
    }

    @Test
    public void addGardenWithoutSizeTest() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, user1));
        Optional<Garden> foundGarden = gardenRepository.findById(addedGarden.getGardenId());

        Assertions.assertNotNull(foundGarden);
        Assertions.assertTrue(foundGarden.isPresent());
        Assertions.assertEquals(addedGarden.getName(), foundGarden.get().getName());
        Assertions.assertEquals(addedGarden.getLocation(), foundGarden.get().getLocation());
        Assertions.assertNull(addedGarden.getSize());
    }

    @Test
    public void GardenAddedWithSize_UpdatedWithSize_UpdatePersists() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, 5.0, user1));
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);

        try {
            gardenService.updateGarden(addedGarden.getGardenId(), "New name", validLocation,
                    10.0, null);
        } catch (GardenNotFoundException e) {
            Assertions.fail("Unexpected error thrown");
        }

        Optional<Garden> foundGarden = gardenRepository.findById(addedGarden.getGardenId());

        Assertions.assertNotNull(foundGarden);
        Assertions.assertTrue(foundGarden.isPresent());
        Assertions.assertEquals("New name", foundGarden.get().getName());
        Assertions.assertEquals(validLocation, foundGarden.get().getLocation());
        Assertions.assertEquals(10.0, foundGarden.get().getSize());
    }

    @Test
    public void GardenAddedWithoutSize_UpdatedWithSize_UpdatePersists() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, user1));
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);

        try {
            gardenService.updateGarden(addedGarden.getGardenId(), "New name", validLocation,
                    10.0, null);
        } catch (GardenNotFoundException e) {
            Assertions.fail("Unexpected error thrown");
        }

        Optional<Garden> foundGarden = gardenRepository.findById(addedGarden.getGardenId());

        Assertions.assertNotNull(foundGarden);
        Assertions.assertTrue(foundGarden.isPresent());
        Assertions.assertEquals("New name", foundGarden.get().getName());
        Assertions.assertEquals(validLocation, foundGarden.get().getLocation());
        Assertions.assertEquals(10.0, foundGarden.get().getSize());
    }

    @Test
    public void GardenAddedWithSize_UpdatedWithoutSize_UpdatePersists() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, 5.0, user1));
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);

        try {
            gardenService.updateGarden(addedGarden.getGardenId(), "New name", validLocation,
                    null, null);
        } catch (GardenNotFoundException e) {
            Assertions.fail("Unexpected error thrown");
        }

        Optional<Garden> foundGarden = gardenRepository.findById(addedGarden.getGardenId());

        Assertions.assertNotNull(foundGarden);
        Assertions.assertTrue(foundGarden.isPresent());
        Assertions.assertEquals("New name", foundGarden.get().getName());
        Assertions.assertEquals(validLocation, foundGarden.get().getLocation());
        Assertions.assertNull(foundGarden.get().getSize());
    }

    @Test
    public void GardenAddedWithoutSize_UpdatedWithoutSize_UpdatePersists() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, user1));
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);

        try {
            gardenService.updateGarden(addedGarden.getGardenId(), "New name", validLocation,
                    null, null);
        } catch (GardenNotFoundException e) {
            Assertions.fail("Unexpected error thrown");
        }

        Optional<Garden> foundGarden = gardenRepository.findById(addedGarden.getGardenId());

        Assertions.assertNotNull(foundGarden);
        Assertions.assertTrue(foundGarden.isPresent());
        Assertions.assertEquals("New name", foundGarden.get().getName());
        Assertions.assertEquals(validLocation, foundGarden.get().getLocation());
        Assertions.assertNull(foundGarden.get().getSize());
    }
    @Test
    public void GardenAddedWithSize_UpdateNonExistentId_ErrorThrown() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        Assertions.assertThrows(GardenNotFoundException.class,
                () -> {gardenService.updateGarden(10023L, "New name", validLocation, null, null);}
        );
    }

    @Test
    public void PlantAdded_PlantExistsInDatabase() {
        Garden addedGarden = gardenRepository.save(new Garden("Test name", validLocation, user1));
        Plant addedPlant = plantRepository.save(new Plant(addedGarden, "name", 5, "desc", null, null));

        Optional<Plant> foundPlant = plantRepository.findById(addedPlant.getPlantId());

        Assertions.assertNotNull(foundPlant);
        Assertions.assertEquals(addedPlant.getPlantId(), foundPlant.get().getPlantId());
        Assertions.assertEquals(addedPlant.getDatePlanted(), foundPlant.get().getDatePlanted());
        Assertions.assertEquals(addedPlant.getName(), foundPlant.get().getName());
        Assertions.assertEquals(addedPlant.getDescription(), foundPlant.get().getDescription());
        Assertions.assertEquals(addedPlant.getCount(), foundPlant.get().getCount());
    }

}
