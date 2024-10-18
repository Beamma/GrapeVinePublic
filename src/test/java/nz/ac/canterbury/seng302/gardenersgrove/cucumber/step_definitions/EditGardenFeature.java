package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.TagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class EditGardenFeature {
    private static MockMvc gardenMockMvc;
    private MockHttpServletRequestBuilder requestBuilder;
    private static ProfanityFilterService mockProfanityFilterService;
    private static GardenRepository mockGardenRepository;
    private static UserService mockUserService;
    private static GardenService gardenService;
    private static InternalResourceViewResolver viewResolver;
    private ResultActions request;
    private static User jane;
    private static Garden mockGarden1;
    private static AddressDTO validLocation;

    private static SecurityContextHolder mockSecurityContextHolder;
    private GardenDTO gardenDTO;
    private static TagRepository tagRepository;

    private static WeatherService mockWeatherService;


    @BeforeAll
    public static void before_or_after_all() throws JsonProcessingException {
        mockGardenRepository = Mockito.mock(GardenRepository.class);
        mockUserService = Mockito.mock(UserService.class);
        mockWeatherService = Mockito.mock(WeatherService.class);
        tagRepository = Mockito.mock(TagRepository.class);

        gardenService = new GardenService(mockGardenRepository, mockUserService, tagRepository);

        jane = new User();
        jane.setEmail("jane@email.com");
        jane.setPassword("Password1!");
        jane.setEnabled(true);
        jane.grantAuthority("ROLE_USER");
        jane.setId(1L);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(jane);
        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);
        mockGarden1 = Mockito.spy(new Garden("My Garden", validLocation, 2.0, jane));
        Mockito.when(mockGarden1.getGardenId()).thenReturn(1L);
        Mockito.doAnswer(i -> {
            Garden g = i.getArgument(0);
            mockGarden1.setLocation(g.getLocation());
            mockGarden1.setName(g.getName());
            mockGarden1.setDescription(g.getDescription());
            mockGarden1.setSize(g.getSize());
            return mockGarden1;
        }).when(mockGardenRepository).save(Mockito.any(Garden.class));


        Mockito.when(mockGardenRepository.findById(1L)).thenReturn(Optional.of(mockGarden1));

        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("shit"));

        // Fix from https://stackoverflow.com/a/21755562 (Circular path error)
        viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/jsp/view/");
        viewResolver.setSuffix(".jsp");


        EmailService mockEmailService = Mockito.mock(EmailService.class);
        FriendService mockFriendService = Mockito.mock(FriendService.class);
        Mockito.when(mockFriendService.checkIfFriends(1l, 1l)).thenReturn(true);
        GardenController gardenController = new GardenController(gardenService, mockUserService, mockFriendService, mockWeatherService, mockProfanityFilterService, new GardenFilterService(mockGardenRepository));
        gardenMockMvc = MockMvcBuilders.standaloneSetup(gardenController)
                .build();
    }

    @Given("I am at the garden details page for a garden I own")
    public void i_am_on_the_garden_details_page_for_a_garden_i_own() throws Exception {
        request = gardenMockMvc.perform(MockMvcRequestBuilders.get("/garden/1"));
        request.andExpect(status().isOk()).andExpect(view().name("gardenView"));

    }


    @When("I click the edit garden button")
    public void i_hit_the_edit_button() throws Exception {
        String phrase = "<span>Edit </span>";
        boolean containsPhrase = Files.lines(Paths.get("src/main/resources/templates/gardenView.html"))
                .anyMatch(l -> l.contains(phrase));
        Assertions.assertTrue(containsPhrase);

    }

    @Then("I see the edit garden form with all the details prepopulated.")
    public void i_see_a_form_to_edit_my_garden() throws Exception {
        gardenMockMvc.perform(MockMvcRequestBuilders.get("/garden/add/1"))
                .andExpectAll(
                        status().isOk(),
                        view().name("addGardenForm")
                );
        Assertions.assertAll(
                () -> Assertions.assertEquals(1L, mockGarden1.getGardenId()),
                () -> Assertions.assertEquals("My Garden", mockGarden1.getName()),
                () -> Assertions.assertEquals(validLocation.getAddressLine1(), mockGarden1.getLocation().getAddressLine1()),
                () -> Assertions.assertEquals(validLocation.getSuburb(), mockGarden1.getLocation().getSuburb()),
                () -> Assertions.assertEquals(validLocation.getPostcode(), mockGarden1.getLocation().getPostcode()),
                () -> Assertions.assertEquals(validLocation.getCity(), mockGarden1.getLocation().getCity()),
                () -> Assertions.assertEquals(validLocation.getCountry(), mockGarden1.getLocation().getCountry()),
                () -> Assertions.assertEquals(2.0, mockGarden1.getSize())
        );
    }

    @Given("I am on the edit garden form")
    public void i_am_on_the_edit_garden_form() {
        requestBuilder = MockMvcRequestBuilders.put("/garden/add/1").with(csrf());
    }


    @When("I click “Submit” on the edit garden form")
    public void i_submit_the_form() throws Exception {
        request = gardenMockMvc.perform(requestBuilder);
    }

    @Then("the garden details are updated with values: name {string}, city {string}, country {string} and size {string}")
    public void the_garden_details_are_updated(String gardenName, String city, String country, String gardenSize) throws Exception {
        Mockito.verify(mockGardenRepository, atLeastOnce()).save(mockGarden1);
        Assertions.assertEquals(mockGarden1.getName(), gardenName);
        Assertions.assertEquals(mockGarden1.getLocation().getCity(), city);
        Assertions.assertEquals(mockGarden1.getLocation().getCountry(), country);
        if (gardenSize.isEmpty()) {
            Assertions.assertNull(mockGarden1.getSize());
        } else {
            Assertions.assertEquals(mockGarden1.getSize(), GardenUtils.parseSize(gardenSize));
        }
    }

    @Then("I am taken back to the Garden page")
    public void I_am_taken_back_to_the_garden_page() throws Exception {
        request = gardenMockMvc.perform(MockMvcRequestBuilders.get("/garden/1"));
        request.andExpect(status().isOk()).andExpect(view().name("gardenView"));


    }

    @Then("an error message tells me {string} in the {string} field")
    public void i_am_shown_an_error_in_the_appropriate_field(String errorMessage, String errorField) throws Exception {
        request.andExpectAll(
                        model().attributeExists(errorField),
                        model().attribute(errorField, errorMessage)
                );
    }

    @When("I enter {string}, {string}, {string}, {string} and {string}")
    public void i_enter_and(String name, String description, String size, String city, String country) {
        AddressDTO addressDTO = new AddressDTO("31 Home Avenue", "Ilam", "8041", city, country, -143.54, 35.356);
        gardenDTO = new GardenDTO(name, description, size, addressDTO);
        requestBuilder.flashAttr("gardenDTO", gardenDTO);
    }

    @Then("the garden details are updated with {string}, {string}, {string}, {string} and {string}")
    public void the_garden_details_are_updated_with_and(String name, String description, String size, String city, String country) {
        Mockito.verify(mockGardenRepository, atLeastOnce()).save(mockGarden1);
        Assertions.assertEquals(name, mockGarden1.getName());
        Assertions.assertEquals(city, mockGarden1.getLocation().getCity());
        Assertions.assertEquals(country, mockGarden1.getLocation().getCountry());
        if (size.isBlank()) {
            Assertions.assertNull(mockGarden1.getSize());
        } else {
            Assertions.assertEquals(GardenUtils.parseSize(size), mockGarden1.getSize());
        }
        if (description.isBlank()) {
            Assertions.assertNull(mockGarden1.getDescription());
        } else {
            Assertions.assertEquals(description, mockGarden1.getDescription());
        }
    }

    @Then("There is an error in the {string} field saying {string}")
    public void there_is_an_error_in_the_field_saying(String errorField, String errorMessage) throws Exception {
        MvcResult result = request
                .andExpectAll(
                        model().attributeHasFieldErrorCode("gardenDTO", errorField, "401")
                )
                .andReturn();

        BindingResult bindingResult = (BindingResult) result.getModelAndView()
                .getModel()
                .get("org.springframework.validation.BindingResult.gardenDTO"); // This line from chatGPT
        boolean containsExpectedError = bindingResult.getFieldErrors(errorField)
                .stream()
                .anyMatch(e -> e.getDefaultMessage().equals(errorMessage));

        Assertions.assertTrue(containsExpectedError);
    }

}
