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
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;

import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityFilterService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ContextConfiguration(classes = RunCucumberTest.class)
public class DeactivateAccountFeature {

    ResultActions result;

    MockMvc mockMvc;

    User user;
    User user1;

    Garden garden1;

    @Autowired
    UserRepository userRepository;
    @Autowired
    GardenController gardenController;
    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ProfanityFilterService profanityFilterService;

    @PersistenceContext
    private EntityManager entityManager;

    private final AddressDTO addressDTO = new AddressDTO("45 Ilam Road", "Ilam", "8042", "Christchurch", "New Zealand", 43.5168, 172.5721);



    @Before("@U23")
    public void setUp() {
        user = new User("test@gmail.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!");
        user1 = userRepository.save(user);

        garden1 = new Garden("Test1", addressDTO, user1);
        garden1 = gardenRepository.save(garden1);

        mockMvc = MockMvcBuilders
                .standaloneSetup(gardenController)
                .build();
    }

    @After("@U23")
    @Transactional
    public void tearDown() {
        entityManager.createNativeQuery("DELETE FROM GARDEN_TAG").executeUpdate();
        tagRepository.deleteAll();
        gardenRepository.deleteAll();
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Given("I am authenticated")
    public void i_am_authenticated() {
        var authentication = new UsernamePasswordAuthenticationToken("test@gmail.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Given("I have added {int} inappropriate tags")
    public void i_have_added_inappropriate_tags(Integer int1) {
        user1.setInappropriateWarningCount(int1);
        user1 = userRepository.save(user1);
    }

    @When("I add another inappropriate tag {string}")
    public void i_add_another_inappropriate_tag(String tag) throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken("tagTester@gmail.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Send mockMVC Request
        result = mockMvc.perform(MockMvcRequestBuilders.put("/garden/"+ garden1.getGardenId() + "/tag").param("tag", tag));
    }

    @When("I add another inappropriate tag")
    public void i_add_another_inappropriate_tag() throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.put("/garden/" + garden1.getGardenId() + "/tag")
                .with(csrf())
                .param("tag", "shit"));
    }

    @Then("I see a {string} warning message telling me {string}.")
    public void i_see_a_warning_message_telling_me(String attribute, String throwaway) throws Exception {

        result.andExpect(flash().attribute(attribute, "true"));
    }

    @Then("I receive an email about {string}, with the template {string}")
    public void i_receive_an_email_about_with_the_message(String subject, String template) {
        String recipient = "tagTester@gmail.com";
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
            Mockito.verify(emailService).sendEmail(eq(recipient), eq(subject),eq(template), Mockito.any())
        );
    }

    @Then("I’m unlogged from the system")
    public void i_m_unlogged_from_the_system() throws Exception {
        result.andExpect(redirectedUrl("/auth/login"));
    }





}
