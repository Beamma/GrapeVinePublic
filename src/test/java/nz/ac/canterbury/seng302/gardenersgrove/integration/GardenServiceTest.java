package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.stream.IntStream;

@DataJpaTest
@Import(Garden.class)
class GardenServiceTest {

    User user1;
    AddressDTO validLocation;
    @Autowired
    UserRepository userRepository;

    UserService userService;


    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        user1 = userRepository.save(new User("Liam@email.com", "2000-01-01", "Liam", "Ceelen", false, "Password1!", "Password1!"));
        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
    }

    @Autowired
    private GardenRepository gardenRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void gardenAddedSuccessfully() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        Garden garden = gardenService.addGarden(new Garden("Vegetable Garden", validLocation, 4.5, user1));
        Assertions.assertEquals("Vegetable Garden", garden.getName());
        Assertions.assertEquals(garden.getLocation(), validLocation);
        Assertions.assertEquals(4.5, garden.getSize());
    }

    @Test
    void locationNull_ThrowsException() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        Assertions.assertThrows(NullPointerException.class, () -> gardenService.addGarden(new Garden("Vegetable Garden", null, user1)));
    }

    @Test
    void nameNull_ThrowsException() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        Garden garden = new Garden(null, validLocation, user1);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> gardenService.addGarden(garden));
    }

    @Test
    void locationAndNameNullThrowsException() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        Assertions.assertThrows(NullPointerException.class, () -> gardenService.addGarden(new Garden(null, null, user1)));
    }

    @Test
    void descriptionTooLongThrowsException() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        String tooLongDescription = "a".repeat(2049);
        Garden garden = new Garden("Vegetable Garden", validLocation, 4.5, tooLongDescription, user1);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> gardenService.addGarden(garden));
    }

    @Test
    void getPublicGardensOnlyReturnsPublicGardens() {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        Garden publicGarden = new Garden("Vegetable Garden", validLocation, 4.5, user1);
        publicGarden.setPublicGarden(true);
        Garden nonPublicGarden = new Garden("Flower Garden", validLocation, 3.5, user1);

        gardenService.addGarden(publicGarden);
        gardenService.addGarden(nonPublicGarden);

        List<Garden> publicGardens = gardenService.getPublicGardens();
        Assertions.assertEquals(1, publicGardens.size());
        Assertions.assertEquals("Vegetable Garden", publicGardens.get(0).getName());
    }

    @Test
    void descriptionNoChangeReturnsTrue() throws GardenNotFoundException {
        // Set up garden
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        String initialDescription = "Valid description";
        Garden garden = gardenService.addGarden(new Garden("Vegetable Garden", validLocation, 4.5, initialDescription, user1));
        Long id = garden.getGardenId();
        // Edit garden for no change
        gardenService.updateGarden(id, "Vegetable Garden", validLocation, 4.5, initialDescription);

        boolean noChange = gardenService.checkDescriptionNoChange(garden.getGardenId(), initialDescription);
        Assertions.assertTrue(noChange);
        // Validation on description should be not be run
    }

    @Test
    void descriptionChangeReturnsFalse() throws GardenNotFoundException {
        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        String initialDescription = "Valid description";
        Garden garden = gardenService.addGarden(new Garden("Vegetable Garden", validLocation, 4.5, initialDescription, user1));
        Long id = garden.getGardenId();
        String newDescription = "Another Valid description";
        // Edit garden for change description
        gardenService.updateGarden(id, "Vegetable Garden", validLocation, 4.5, newDescription);

        boolean noChange = gardenService.checkDescriptionNoChange(garden.getGardenId(), initialDescription);
        Assertions.assertFalse(noChange);
        // Validation on description should be run again
    }


    @Test
    void twentyGardensPresent_returnsTwenty() {
        int expectedNumberOfGardens = 20;

        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        IntStream.range(0, 20).forEach(
                (i) -> {
                    Garden g = new Garden("Vegetable Garden " + i, validLocation, 4.5, "Description", user1);
                    g.setPublicGarden(true);
                    gardenService.addGarden(g);
                }
        );
        List<Garden> gardens = gardenService.searchPublicGardensByUserIdAndName(user1.getId(), "Vegetable Garden");
        Assertions.assertEquals(expectedNumberOfGardens, gardens.size());
    }

    @Test
    void twentyOneGardensPresent_returnsTwenty() {
        int expectedNumberOfGardens = 20;

        GardenService gardenService = new GardenService(gardenRepository, userService, tagRepository);
        IntStream.range(0, 21).forEach(
                (i) -> {
                    Garden g = new Garden("Vegetable Garden " + i, validLocation, 4.5, "Description", user1);
                    g.setPublicGarden(true);
                    gardenService.addGarden(g);
                }
        );
        List<Garden> gardens = gardenService.searchPublicGardensByUserIdAndName(user1.getId(), "Vegetable Garden");
        Assertions.assertEquals(expectedNumberOfGardens, gardens.size());
    }
}