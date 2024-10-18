package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Tag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions.BrowsePublicGardensFeature.PAGE_SIZE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Both of these @'s are required for acceptance tests.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class BrowseFilterGardensFeature {

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
    private MockHttpServletRequestBuilder requestBuilder;
    ResultActions result;
    @PersistenceContext
    private EntityManager entityManager;

    private final AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);

    /**
     * The @Before, is used to add the required users tags and gardens to the test database
     * The @U24 is used to specify this method only runs down for scenarios tagged with the @U24
     */
    @Before("@U24")
    public void setUp() {
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

    /**
     * The @After, is used to empty the database after all the tests are ran. The @Transactional is required to make this work.
     * The @U24 is used to specify this method only runs down for scenarios tagged with the @U24
     */
    @After("@U24")
    @Transactional
    public void tearDown() {
        entityManager.createNativeQuery("DELETE FROM GARDEN_TAG").executeUpdate();
        tagRepository.deleteAll();
        gardenRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
    }

    // Background
    @Given("I am authenticated as {string}")
    public void i_am_authenticated_as(String email) {
        user = new User(email, "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1 = userRepository.save(user);
        var authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Given("I am on the browse garden form and I have the search set as {string} and the Tags as {string}")
    public void i_have_the_search_set_as_and_the_tags_as(String search, String tags) throws Exception {
        requestBuilder = MockMvcRequestBuilders.get("/garden/browse?search=" + search + "&tags=" + tags).param("page", "1");
    }

    @When("I submit the filter form")
    public void i_submit_the_filter_form() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("{int} results are displayed")
    public void results_are_displayed(Integer expectedNumResults) throws Exception {
        MvcResult response = result.andExpectAll(
                status().is(200),
                view().name("browseGardens")
        ).andReturn();

        BrowseGardenDTO browseGardenDTO = (BrowseGardenDTO) Objects.requireNonNull(response.getModelAndView()).getModel().get("browseGardenDTO");

        Assertions.assertEquals(expectedNumResults, browseGardenDTO.getSearchSize());
        Assertions.assertEquals(Math.ceil((double) expectedNumResults / PAGE_SIZE), browseGardenDTO.getTotalPages());
        Assertions.assertNull(browseGardenDTO.getSearchError());
        Assertions.assertEquals(Math.min(expectedNumResults, PAGE_SIZE), browseGardenDTO.getGardens().size());
        Assertions.assertEquals(9, browseGardenDTO.getPageSize());
        Assertions.assertEquals(1, browseGardenDTO.getParsedPage());
    }

    @Given("I want to browse for a tag {string}")
    public void i_want_to_browse_for_a_tag(String tagBeginning) {
        requestBuilder = MockMvcRequestBuilders.get("/tags?input=" + tagBeginning);
    }
    @When("I start typing the tag")
    public void i_start_typing_the_tag() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }
    @Then("tags matching my input are shown")
    public void tags_matching_my_input_are_shown() throws Exception {
        String expectedBody = String.format(
                "[{\"tagId\":%d,\"name\":\"Tag1\"},{\"tagId\":%d,\"name\":\"Tag2\"},{\"tagId\":%d,\"name\":\"Tag3\"}]",
                tag1.getTagId(), tag2.getTagId(), tag3.getTagId());
        String body = result.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assertions.assertEquals(expectedBody, body);
    }


}
