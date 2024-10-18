package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GardenFilterIntegrationTests {

    Garden garden1;
    Garden garden2;
    Garden garden3;
    Garden garden4;
    Garden garden5;
    Garden garden6;
    Garden garden7;
    Garden garden8;
    Garden garden9;
    Garden garden10;
    Garden garden11;

    Tag tag1;
    Tag tag2;
    Tag tag3;
    User user;
    User user1;
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GardenController gardenController;
    @Autowired
    private TagRepository tagRepository;

    MockMvc mockMvc;


    private AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);
    @BeforeAll
    void setUp() {
        user = new User("filterTester@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1 = userRepository.save(user);

        tag1 = new Tag("Tag1");
        tag1 = tagRepository.save(tag1);

        tag2 = new Tag("Tag2");
        tag2 = tagRepository.save(tag2);

        tag3 = new Tag("Tag3");
        tag3 =  tagRepository.save(tag3);

        garden1 = new Garden("Test1", addressDTO, user1);
        garden1.setPublicGarden(true);
        garden1.setTags(Set.of(tag1));
        gardenRepository.save(garden1);

        garden2 = new Garden("Test2", addressDTO, user1);
        garden2.setPublicGarden(true);
        garden2.setTags(Set.of(tag1, tag2));
        gardenRepository.save(garden2);

        garden3 = new Garden("Test3", addressDTO, user1);
        garden3.setPublicGarden(true);
        garden3.setTags(Set.of(tag1, tag2));
        gardenRepository.save(garden3);

        garden4 = new Garden("Test4", addressDTO, user1);
        garden4.setPublicGarden(true);
        garden4.setTags(Set.of(tag2));
        gardenRepository.save(garden4);

        garden5 = new Garden("Test5", addressDTO, user1);
        garden5.setPublicGarden(true);
        garden5.setTags(Set.of(tag3));
        gardenRepository.save(garden5);

        garden6 = new Garden("Test6", addressDTO, user1);
        garden6.setPublicGarden(true);
        gardenRepository.save(garden6);

        garden7 = new Garden("Test7", addressDTO, user1);
        garden7.setPublicGarden(true);
        gardenRepository.save(garden7);

        garden8 = new Garden("Test8", addressDTO, user1);
        garden8.setPublicGarden(true);
        gardenRepository.save(garden8);

        garden9 = new Garden("Test9", addressDTO, user1);
        garden9.setPublicGarden(true);
        gardenRepository.save(garden9);

        garden10 = new Garden("Test10", addressDTO, user1);
        garden10.setPublicGarden(true);
        gardenRepository.save(garden10);

        garden11 = new Garden("Test11", addressDTO, user1);
        garden11.setPublicGarden(true);
        gardenRepository.save(garden11);

        mockMvc = MockMvcBuilders
                .standaloneSetup(gardenController)
                .build();
    }

    @Test
    @WithMockUser(username = "filterTester@gmail.com")
    void no_filter() throws Exception {
        MvcResult result = mockMvc.perform(get("/garden/browse")
                        .param("page", "1"))

                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(11, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(2, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden11.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(10, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @Test
    @WithMockUser(username = "filterTester@gmail.com")
    void no_filter_page_too_high() throws Exception {
        MvcResult result = mockMvc.perform(get("/garden/browse")
                        .param("page", "5"))

                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(11, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(2, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden1.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(10, browseGardenDTO.getPageSize());
        Assertions.assertEquals(2, browseGardenDTO.getParsedPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/garden/browse?search=Test1&tags=&page=1", "/garden/browse?search=Test1&tags=&page=5"})
    @WithMockUser(username = "filterTester@gmail.com")
    void search_only_filter(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(3, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(1, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden11.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/garden/browse?search=WrongTest1&tags=&page=5", "/garden/browse?search=&tags=TagNotExist&page=1", "/garden/browse?search=Test1&tags=Tag2&page=1"})
    @WithMockUser(username = "filterTester@gmail.com")
    void returns_no_results(String  url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(0, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(0, browseGardenDTO.getTotalPages());
        Assertions.assertEquals("No gardens match your search", browseGardenDTO.getSearchError());
        Assertions.assertNull(browseGardenDTO.getGardens());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(0, browseGardenDTO.getParsedPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/garden/browse?search=&tags=Tag1&page=1", "/garden/browse?search=&tags=Tag1&page=5", "/garden/browse?search=Test&tags=Tag1&page=1"})
    @WithMockUser(username = "filterTester@gmail.com")
    void filter_1_tag(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(3, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(1, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden3.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(3, browseGardenDTO.getGardens().size());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/garden/browse?search=&tags=Tag1,Tag2&page=1", "/garden/browse?search=Test&tags=Tag1,Tag2&page=1"})
    @WithMockUser(username = "filterTester@gmail.com")
    void two_tag_only_filter(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url)
                        .param("page", "1"))

                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(4, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(1, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden4.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(4, browseGardenDTO.getGardens().size());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @Test
    @WithMockUser(username = "filterTester@gmail.com")
    void one_tag_only_filter_one_result() throws Exception {
        MvcResult result = mockMvc.perform(get("/garden/browse?search=&tags=Tag3")
                        .param("page", "1"))

                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(1, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(1, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden5.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(1, browseGardenDTO.getGardens().size());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @Test
    @WithMockUser(username = "filterTester@gmail.com")
    void all_tag_only_filter() throws Exception {
        MvcResult result = mockMvc.perform(get("/garden/browse?search=&tags=Tag1,Tag2,Tag3")
                        .param("page", "1"))

                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(5, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(1, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden5.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(5, browseGardenDTO.getGardens().size());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }


    @Test
    @WithMockUser(username = "filterTester@gmail.com")
    void search_with_1_tag_1_results() throws Exception {
        MvcResult result = mockMvc.perform(get("/garden/browse?search=Test1&tags=Tag1")
                        .param("page", "1"))

                .andExpectAll(
                        status().is(200),
                        view().name("browseGardens")
                ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) result.getModelAndView().getModel().get("browseGardenDTO");

        Assertions.assertEquals(1, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(1, browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(garden1.getGardenId(), browseGardenDTO.getGardens().getFirst().getGardenId());
        Assertions.assertEquals(1, browseGardenDTO.getGardens().size());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"tag", "TAG", "TaG"})
    @WithMockUser(username = "filterTester@gmail.com")
    void autofillIsCaseInsensitive(String tagBeginning) throws Exception {
        String expectedBody = String.format(
                "[{\"tagId\":%d,\"name\":\"Tag1\"},{\"tagId\":%d,\"name\":\"Tag2\"},{\"tagId\":%d,\"name\":\"Tag3\"}]",
                tag1.getTagId(), tag2.getTagId(), tag3.getTagId());

        String body = mockMvc.perform(get("/tags?input=" + tagBeginning))
            .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(expectedBody, body);
    }

    @Test
    @WithMockUser(username = "filterTester@gmail.com")
    void autofillForSpecificTag_OnlyReturnsThatTag() throws Exception {
        String expectedBody = String.format(
                "[{\"tagId\":%d,\"name\":\"Tag1\"}]",
                tag1.getTagId());
        String tagInput = "tag1";

        String body = mockMvc.perform(get("/tags?input=" + tagInput ))
            .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(expectedBody, body);
    }
}
