package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(GardenService.class)
@ActiveProfiles("test")
public class GardenServiceTest {
    GardenService gardenService;

    GardenRepository gardenRepository;

    @MockBean
    UserService userService;

    TagRepository tagRepository;
    Garden garden1User1;



    @BeforeEach
    public void setUp() {
        tagRepository = Mockito.mock(TagRepository.class);
        gardenRepository = Mockito.mock(GardenRepository.class);
        userService = Mockito.mock(UserService.class);
        gardenService = new GardenService(gardenRepository, userService, tagRepository);

        User mockUser1 = Mockito.mock(User.class);
        User mockUser2 = Mockito.mock(User.class);
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);

        Mockito.when(mockUser1.getId()).thenReturn(1L);

        Mockito.when(userService.getCurrentUser()).thenReturn(mockUser1);

        List<Garden> gardenList = new ArrayList<>();

        garden1User1 = new Garden("Vegetable Garden1", validLocation, 4.5, mockUser1);
        Garden garden2User2 = new Garden("Vegetable Garden2", validLocation, mockUser2);

        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("First Tag"));

        garden1User1.setTags(tags);

        gardenList.add(garden1User1);
        gardenList.add(garden2User2);

        //Because anyLong() doesn't read the value correctly
        int INDEX_OFFSET = -1;
        Mockito.when(gardenRepository.findById(Mockito.any())).then(invocation -> {
            Long firstArgument = (invocation.getArgument(0)); //The value used in the findById function
            Garden gardenFromList = gardenList.get(firstArgument.intValue() + INDEX_OFFSET);
            return (Optional.of(gardenFromList));
        });
    }

    @Test
    public void GivenUser1_AndGardenOwnedByUser1_ReturnTrue () {
        Long num = 1L;
        boolean check = gardenService.checkGardenOwnership(num);
        Assertions.assertTrue(check);
    }

    @Test void GivenUser1_AndGardenOwnedByUser2_ReturnFalse () {
        Assertions.assertFalse(gardenService.checkGardenOwnership(2L));
    }

    @Test
    public void GivenTagExists_TagIsNotAddedToDataBase () {
        Tag tag = new Tag("Test");
        Mockito.when(tagRepository.findByName("Test")).thenReturn(Optional.of(tag));
        gardenService.addTag("Test", 1L);

        Mockito.verify(tagRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(gardenRepository, Mockito.atLeastOnce()).save(Mockito.any());
        Mockito.when(tagRepository.save(Mockito.any())).thenReturn(tag);
        List<Tag> tags = garden1User1.getTagsOrdered();

        long count = tags.stream()
                .filter(item -> item.equals(tag))
                .count();

        // Assert
        Assertions.assertEquals(1, count);
    }

    @Test
    public void GivenTagDoesntExist_TagIsAddedToDataBase () {
        Tag tag = new Tag("Test");
        Mockito.when(tagRepository.findByName("Test")).thenReturn(Optional.empty());
        Mockito.when(tagRepository.save(Mockito.any())).thenReturn(tag);
        gardenService.addTag("Test", 1L);

        Mockito.verify(tagRepository, Mockito.atLeastOnce()).save(Mockito.any());
        Mockito.verify(gardenRepository, Mockito.atLeastOnce()).save(Mockito.any());
        List<Tag> tags = garden1User1.getTagsOrdered();

        long count = tags.stream()
                .filter(item -> item.equals(tag))
                .count();

        // Assert
        Assertions.assertEquals(1, count);
    }

    @Test
    public void GivenTagIsInappropriate_TagIsNotAddedToDataBase () {
        Tag tag = new Tag("Shit");
        Mockito.when(tagRepository.findByName("Shit")).thenReturn(Optional.of(tag));
        gardenService.addTag("Shit", 1L);

        Mockito.verify(tagRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(gardenRepository, Mockito.atLeastOnce()).save(Mockito.any());
        Mockito.when(tagRepository.save(Mockito.any())).thenReturn(tag);
        List<Tag> tags = garden1User1.getTagsOrdered();

        long count = tags.stream()
                .filter(item -> item.equals(tag))
                .count();

        // Assert
        Assertions.assertEquals(1, count);
    }
}
