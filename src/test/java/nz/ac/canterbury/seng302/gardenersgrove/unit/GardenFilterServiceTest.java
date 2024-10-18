package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenFilterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GardenFilterServiceTest {

    private GardenFilterService gardenFilterService;
    private GardenRepository mockGardenRepository;

    private Garden garden1;
    private Garden garden2;
    private Garden garden3;
    private AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);
    private User user;

    @BeforeEach
    void setUp() {
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        gardenFilterService = new GardenFilterService(mockGardenRepository);

        user = Mockito.mock(User.class);
        garden1 = new Garden("Test1", addressDTO, user);
        garden2 = new Garden("Test2", addressDTO, user);
        garden3 = new Garden("Test3", addressDTO, user);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "a,,b,c", "a,b,c"})
     void test_parse_tags (String tagString) {
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO(tagString, "", "1");
        gardenFilterService.parseTags(browseGardenDTO);

        Assertions.assertEquals(Arrays.asList(tagString.split(",")).toString(), browseGardenDTO.getTags().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "-1", "0", "1.5", ""})
    void test_parse_pages_invalid(String page) {
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO("a,b,c", "", page);
        gardenFilterService.parsePages(browseGardenDTO);

        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2", "99999"})
    void test_parse_pages_valid(String page) {
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO("a,b,c", "", page);
        gardenFilterService.parsePages(browseGardenDTO);

        Assertions.assertEquals(Integer.parseInt(page), browseGardenDTO.getParsedPage());
    }

    @Test
    void test_getGardens_no_filter() {
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO("", "", "1");
        gardenFilterService.parseTags(browseGardenDTO);
        gardenFilterService.parsePages(browseGardenDTO);

        List<Garden> gardens = new ArrayList<>();
        gardens.add(garden1);
        gardens.add(garden2);
        gardens.add(garden3);

        Page<Garden> page = new PageImpl<>(gardens, PageRequest.of(1, 9), 11);
        Mockito.when(mockGardenRepository.findRecentPublicGardens(Mockito.any(Pageable.class))).thenReturn(page);
        gardenFilterService.getGardens(browseGardenDTO);

        Assertions.assertEquals(gardens.toString(), browseGardenDTO.getGardens().toString());
    }

    @Test
    void test_getGardens_search() {
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO("", "Test1", "1");
        gardenFilterService.parseTags(browseGardenDTO);
        gardenFilterService.parsePages(browseGardenDTO);

        List<Garden> gardens = new ArrayList<>();
        gardens.add(garden1);

        Page<Garden> page = new PageImpl<>(gardens, PageRequest.of(0, 9), 1);
        Mockito.when(mockGardenRepository.findAllPublicGardensByNamePageable("Test1", PageRequest.of(0, 9))).thenReturn(page);
        gardenFilterService.getGardens(browseGardenDTO);

        Assertions.assertEquals(gardens.toString(), browseGardenDTO.getGardens().toString());
    }

    @Test
    void test_getGardens_tags() {
        String tagString = "Tag1";
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO(tagString, "", "1");
        gardenFilterService.parseTags(browseGardenDTO);
        gardenFilterService.parsePages(browseGardenDTO);

        List<Garden> gardens = new ArrayList<>();
        gardens.add(garden1);

        List<String> tags = Arrays.asList(tagString.split(","));

        Page<Garden> page = new PageImpl<>(gardens, PageRequest.of(0, 9), 1);
        Mockito.when(mockGardenRepository.findAllPublicGardensByTagsPageable(tags, PageRequest.of(0, 9))).thenReturn(page);
        gardenFilterService.getGardens(browseGardenDTO);

        Assertions.assertEquals(gardens.toString(), browseGardenDTO.getGardens().toString());
    }

    @Test
    void test_getGardens_tags_and_search() {
        String tagString = "Tag1";
        String searchString = "Test1";
        BrowseGardenDTO browseGardenDTO = new BrowseGardenDTO(tagString, searchString, "1");
        gardenFilterService.parseTags(browseGardenDTO);
        gardenFilterService.parsePages(browseGardenDTO);

        List<Garden> gardens = new ArrayList<>();
        gardens.add(garden1);

        List<String> tags = Arrays.asList(tagString.split(","));

        Page<Garden> page = new PageImpl<>(gardens, PageRequest.of(0, 9), 1);
        Mockito.when(mockGardenRepository.findAllGardensByNameAndTagsPageable(searchString, tags, PageRequest.of(0, 9))).thenReturn(page);
        gardenFilterService.getGardens(browseGardenDTO);

        Assertions.assertEquals(gardens.toString(), browseGardenDTO.getGardens().toString());
    }
}
