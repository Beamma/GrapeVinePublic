package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.utility.EnvironmentUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
public class GardenTagIntegrationTests {
    MockMvc mockMvc;
    GardenService gardenService;
    TagRepository mockTagRepository;
    User mockUser1;
    User mockUser2;
    GardenRepository mockGardenRepository;
    Garden mockGarden1;
    WeatherService mockWeatherService;
    UserService mockUserService;
    ProfanityFilterService mockProfanityFilterService;
    @BeforeEach
    void setUp() throws JsonProcessingException {
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        mockTagRepository = Mockito.mock(TagRepository.class);
        FriendService mockFriendService = Mockito.mock(FriendService.class);
        mockWeatherService = Mockito.mock(WeatherService.class);
        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString()) ).thenAnswer(i -> ((String) i.getArgument(0)).contains("badWord"));


        gardenService = new GardenService(mockGardenRepository, mockUserService, mockTagRepository);

        mockUser1 = Mockito.mock(User.class);
        Mockito.when(mockUser1.getId()).thenReturn(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(mockUser1);

        Mockito.when(mockUserService.handleInappropriateSubmission(mockUser1)).then(invocation -> {
            User user = invocation.getArgument(0);
            if (user.getInappropriateWarningCount() >= 5) {
                Mockito.when(user.isBlocked()).thenReturn(true);
            }
            return user;
        });

        mockUser2 = Mockito.mock(User.class);
        Mockito.when(mockUser1.getId()).thenReturn(2L);

        mockGarden1 = Mockito.mock(Garden.class);
        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("First Tag"));
        Mockito.when(mockGarden1.getTags()).thenReturn(tags);

        Garden mockGarden2 = Mockito.mock(Garden.class);
        Mockito.when(gardenService.getGardenByID(1L)).thenReturn(Optional.ofNullable(mockGarden1));
        Mockito.when(gardenService.getGardenByID(2L)).thenReturn(Optional.ofNullable(mockGarden2));
        Mockito.when(mockGarden1.getUser()).thenReturn(mockUser1);
        Mockito.when(mockGarden2.getUser()).thenReturn(mockUser2);

        GardenController gardenController = new GardenController(gardenService, mockUserService, mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));
        gardenController.setEventPublisher(Mockito.mock(ApplicationEventPublisher.class));
        EnvironmentUtils mockEnvironmentUtils = Mockito.mock(EnvironmentUtils.class);
        Mockito.when(mockEnvironmentUtils.getInstance()).thenReturn("");
        Mockito.when(mockEnvironmentUtils.getBaseUrl()).thenReturn("http://localhost:8080");
        gardenController.setEnvironmentUtils(mockEnvironmentUtils);

        this.mockMvc = MockMvcBuilders
            .standaloneSetup(gardenController)
            .build();
    }

    /**
     * Delete all data from the database after class is run
     */
    @ParameterizedTest
    @ValueSource(strings = {"!", "@", "\t", "#", "$", "%", "^", "&", "*", "(", ")", "+", "/", ""})
    void GivenInvalidTag_ReturnsError (String tag) throws Exception {
        mockMvc.perform(put("/garden/1/tag")
            .param("tag", tag))
            .andExpectAll(
                    status().is(302),
                    flash().attribute("tagError", "The tag name must only contain alphanumeric characters, spaces,  -, _, ', or ”")
            );

        Mockito.verify(mockTagRepository, Mockito.never()).findByName(Mockito.any());
    }

    @Test
    void GivenTagToLong_ReturnsError () throws Exception {
        String tag = "a".repeat(26);
        mockMvc.perform(put("/garden/1/tag")
                        .param("tag", tag))
                .andExpectAll(
                        status().is(302),
                        flash().attribute("tagError", "A tag cannot exceed 25 characters")
                );

        Mockito.verify(mockTagRepository, Mockito.never()).findByName(Mockito.any());
    }

    @Test
    void GivenDontOwnGarden_ReturnsError () throws Exception {
        mockMvc.perform(put("/garden/2/tag")
                        .param("tag", "Test"))
                .andExpectAll(
                        status().is(302),
                        flash().attribute("tagError", "You do not own this garden")
                );

        Mockito.verify(mockTagRepository, Mockito.never()).findByName(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"badWord"})
    void GivenContainsProfanity_ReturnsError (String tag) throws Exception {
        mockMvc.perform(put("/garden/1/tag")
                        .param("tag", tag))
                .andExpectAll(
                        status().is(302),
                        flash().attribute("tagError", "The submitted tag wasn't added, as it was flagged as inappropriate")
                );

        Mockito.verify(mockTagRepository, Mockito.never()).findByName(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAAAAAAAAAAAAAAAAAAAAAAAA", "Test1", "Test 2", " Test 5 6 ", "Test", "Test\""})
    void GivenValidExisiting_AddsTagToGarden (String tagString) throws Exception {
        Tag tag = new Tag(tagString);
        Mockito.when(mockTagRepository.findByName(tagString)).thenReturn(Optional.of(tag));
        mockMvc.perform(put("/garden/1/tag")
                        .param("tag", tagString))
                .andExpectAll(
                        status().is(302),
                        flash().attributeCount(0)
                );

        Mockito.verify(mockTagRepository, Mockito.atLeast(1)).findByName(Mockito.any());
        Mockito.verify(mockTagRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(mockGardenRepository, Mockito.atLeastOnce()).save(Mockito.any());
        Mockito.verify(mockGarden1, Mockito.times(1)).addTag(tag);
        Mockito.verify(mockGardenRepository, Mockito.times(1)).save(mockGarden1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAAAAAAAAAAAAAAAAAAAAAAAA", "Test1", "Test 2", " Test 5 6 ", "Test", "Test\""})
    void GivenValidNotExisiting_AddsTagToGarden (String tagString) throws Exception {
        Tag tag = new Tag(tagString);
        Mockito.when(mockTagRepository.findByName(tagString)).thenReturn(Optional.of(tag));
        mockMvc.perform(put("/garden/1/tag")
                        .param("tag", tagString))
                .andExpectAll(
                        status().is(302),
                        flash().attributeCount(0)
                );

        Mockito.verify(mockTagRepository, Mockito.atLeast(1)).findByName(Mockito.any());
        Mockito.verify(mockTagRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(mockGardenRepository, Mockito.atLeastOnce()).save(Mockito.any());
        Mockito.verify(mockGarden1, Mockito.times(1)).addTag(Mockito.any());
        Mockito.verify(mockGardenRepository, Mockito.times(1)).save(mockGarden1);
    }

    @Test
    void GivenHasFourInappropriateTags_AddFifth_ShowsWarning () throws Exception {

        Mockito.when(mockUserService.getCurrentUser().hasReachedInappropriateWarningLimit()).thenReturn(true);

        // User has 4 inappropriate tags
        Mockito.when(mockUser1.getInappropriateWarningCount()).thenReturn(4);

        // User attempts to add another inappropriate tag
        mockMvc.perform(put("/garden/1/tag")
                        .param("tag", "badWord"))
                .andExpect(
                        flash().attribute("fifthInappropriateSubmission", "true")
                );

        // Assert user is not blocked and they have done 5 inappropriate tags
        Mockito.verify(mockUserService, Mockito.atLeastOnce()).handleInappropriateSubmission(mockUser1);
        Assertions.assertTrue(mockUserService.getCurrentUser().hasReachedInappropriateWarningLimit());
        Assertions.assertFalse(mockUser1.isBlocked());
    }

    @Test
    void GivenUserHasFiveInappropriateTags_OnSixth_ReturnsRedirect_UserBlocked() throws Exception {
        // User has 5 inappropriate tags
        Mockito.when(mockUser1.getInappropriateWarningCount()).thenReturn(5);

        // User attempts to add another inappropriate tag
        // Assert redirect with blocked attribute
        mockMvc.perform(put("/garden/1/tag")
                        .param("tag", "badWord"))
                .andExpectAll(
                        flash().attribute("blocked", "true"),
                        view().name("redirect:/auth/login")
                );

        // Assert user is blocked
        Mockito.verify(mockUserService, Mockito.atLeastOnce()).handleInappropriateSubmission(mockUser1);
        Assertions.assertTrue(mockUser1.isBlocked());
    }
}
