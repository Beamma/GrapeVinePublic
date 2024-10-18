package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.cucumber.RunCucumberTest;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class GardenTagModerationFeature {

    private Garden garden1;
    private User user;

    private MockHttpServletRequestBuilder requestBuilder;
    private ResultActions result;

    @Autowired
    private GardenController gardenController;
    private MockMvc mockMvc;
    @Autowired
    private ProfanityFilterService profanityFilterService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GardenRepository gardenRepository;
    @Autowired
    private TagRepository tagRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Before("@U22")
    public void SetUp() {
        User newUser = new User("test@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user = userRepository.save(newUser);

        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        garden1 = new Garden("My Garden", validLocation, user);

        garden1 = gardenRepository.save(garden1);

        mockMvc = MockMvcBuilders.standaloneSetup(gardenController)
                .build();
    }

    /**
     * Used to empty the database after all the tests are run
     */
    @After("@U22")
    @Transactional
    public void tearDown() {
        entityManager.createNativeQuery("DELETE FROM GARDEN_TAG").executeUpdate();
        tagRepository.deleteAll();
        gardenRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Given("I am adding a valid new garden tag named {string}")
    public void i_create_a_new_garden_tag_named(String tagName) {
        requestBuilder = MockMvcRequestBuilders.put("/garden/" + garden1.getGardenId() + "/tag")
                .param("tag", tagName);
    }

    @When("I confirm the tag")
    public void i_submit_the_new_tag_name_for_my_garden() throws Exception {
        result = mockMvc.perform(requestBuilder);
    }

    @Then("the tag is checked for offensive or inappropriate words")
    public void the_tag_is_checked_for_inappropriate_words() throws Exception {
        Mockito.verify(profanityFilterService).isTextProfane(Mockito.anyString());
    }

    @Then("an error message tells me that {string} in the {string} field")
    public void i_am_shown_an_error_in_its_appropriate_field(String errorMessage, String errorField) throws Exception {
        result.andExpectAll(flash().attribute(errorField, errorMessage));
    }

    @Then("the tag textbox is cleared")
    public void the_tag_textbox_is_cleared() throws Exception {
        result.andExpectAll(status().is(302));
    }

    @Then("the new tag {string} becomes a new user-defined tag on the system showing up in future auto-complete suggestions")
    public void the_new_tag_becomes_a_new_user_defined_tag_on_the_system_showing_up_in_future_auto_complete_suggestions(String newTag) {
        Assertions.assertTrue(tagRepository.findByName(newTag).isPresent());
    }

    @Then("the new tag {string} is added to my garden")
    public void the_new_tag_is_added_to_my_garden(String newTag) {
        Garden garden = gardenRepository.findById(garden1.getGardenId()).get();
        Assertions.assertTrue(garden.getTags().stream().anyMatch(t -> t.getName().equals(newTag)));
    }

    @Then("the tag is not added to the list of user-defined tags")
    public void the_tag_is_not_added_to_the_tag_list() {
        Assertions.assertEquals(0, tagRepository.findAll().size());
    }

    @Then("my inappropriate tag count increases by one")
    public void my_inappropriate_tag_count_increases_by_one() {
        Assertions.assertEquals(
                1, userRepository.findByEmail(user.getEmail()).get().getInappropriateWarningCount()
        );
    }

    @Then("the users count of inappropriate tags is increased by 1")
    public void the_users_inappropriate_tag_count_is_increased() {
        int tagCount = userRepository.findById(user.getId()).get().getInappropriateWarningCount();
        Assertions.assertEquals(1, tagCount);
    }

    @And("I receive an email confirming me that my account is blocked for {int} days.")
    public void iReceiveAnEmailConfirmingMeThatMyAccountIsBlockedForDays(int arg0) {
        // to be implemented in T4
    }

    @Then("I receive an email warning me that I will be blocked if I add a sixth tag")
    public void i_receive_an_email_warning_me_that_i_will_be_blocked_if_i_add_a_sixth_tag() {
        // to be implemented in T4
    }

}
