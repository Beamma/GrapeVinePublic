package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.GardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.exception.GardenNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GardenLocationFeature {

    private MockMvc mockMvc;
    private GardenController gardenController;
    private GardenService gardenService;

    @MockBean
    private GardenRepository gardenRepository;
    private ArrayList<Garden> gardens;
    @MockBean
    private FriendService friendService;
    @MockBean
    private WeatherService weatherService;
    @MockBean
    private static ProfanityFilterService mockProfanityFilterService;
    private UserService userService;
    private MockHttpServletRequestBuilder requestBuilder;
    private AddressDTO addressDTO;
    private GardenDTO gardenDTO;

    @Before
    public void setup() throws JsonProcessingException {
        User currentUser = new User("my@email.com", "1999-12-31", "Gerd", "Müller", false, "Password1!", "Password1!");
        userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getCurrentUser()).thenReturn(currentUser);

        Garden garden1 = Mockito.spy(new Garden("Garden 2", new AddressDTO("Pleasant Street", "Suburb Hill", "0000", "City", "Country", null, null), null, currentUser));
        Mockito.when(garden1.getGardenId()).thenReturn(1L);
        gardens = new ArrayList<>(List.of(garden1));
        gardenService = Mockito.mock(GardenService.class);
        Mockito.doAnswer(i -> {
            Garden g = i.getArgument(0);
            Garden addedGarden = Mockito.spy(g);
            Mockito.when(addedGarden.getGardenId()).thenReturn(2L);
            gardens.add(addedGarden);
            return addedGarden;
        }).when(gardenService).addGarden(Mockito.any(Garden.class));
        try {
            Mockito.doAnswer(i -> {
                Long id = i.getArgument(0);
                String name = i.getArgument(1);
                AddressDTO loc = i.getArgument(2);
                Double size = i.getArgument(3);
                String desc = i.getArgument(4);
                Garden originalGarden = gardens.stream().filter(g -> g.getGardenId() == id).findFirst().get();
                gardens.get(0).setLocation(loc);
                gardens.get(0).setName(name);
                gardens.get(0).setDescription(desc);
                gardens.get(0).setSize(size);
                return originalGarden;
            }).when(gardenService).updateGarden(Mockito.any(), Mockito.any(), Mockito.any(AddressDTO.class), Mockito.any(), Mockito.any());
        } catch (GardenNotFoundException ignored) {}
        Mockito.doAnswer(i -> {
            Long id = i.getArgument(0);
            return gardens.stream().filter(g -> g.getGardenId() == id).findFirst();
        }).when(gardenService).getGardenByID(Mockito.anyLong());
        Mockito.when(gardenService.checkGardenOwnership(Mockito.anyLong())).thenReturn(true);

        mockProfanityFilterService = Mockito.mock(ProfanityFilterService.class);
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.anyString())).thenAnswer(i -> ((String) i.getArgument(0)).contains("shit"));

        gardenController = new GardenController(gardenService, userService, friendService, weatherService, mockProfanityFilterService, new GardenFilterService(gardenRepository));
        mockMvc = MockMvcBuilders.standaloneSetup(gardenController).build();
    }

    @Given("I am on the create new garden form")
    public void i_am_on_the_create_new_garden_form() {
        requestBuilder = MockMvcRequestBuilders.post("/garden/add");
    }

    @Given("I am on the edit garden page")
    public void i_am_on_the_edit_garden_page() {
        requestBuilder = MockMvcRequestBuilders.put("/garden/add/1");
    }

    @When("I add a location")
    public void i_add_a_location() {
        addressDTO = new AddressDTO();
    }
    @When("I add a valid name")
    public void i_add_a_valid_name() {
        gardenDTO = new GardenDTO("Name", null, null, addressDTO);
    }

    @Then("I can specify a street address and number {string}, suburb {string}, city {string}, postcode {string}, and country {string}")
    public void i_can_specify_a_street_address_and_number_suburb_city_postcode_and_country(String addressLine1, String suburb, String city, String postcode, String country) throws Exception {
        addressDTO.setAddressLine1(addressLine1);
        addressDTO.setSuburb(suburb);
        addressDTO.setCity(city);
        addressDTO.setPostcode(postcode);
        addressDTO.setCountry(country);

        mockMvc.perform(requestBuilder.flashAttr("gardenDTO", gardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/2"),
                        model().hasNoErrors()
                );

        Assertions.assertAll(
                () -> Assertions.assertEquals(addressLine1, gardens.get(1).getLocation().getAddressLine1()),
                () -> Assertions.assertEquals(suburb, gardens.get(1).getLocation().getSuburb()),
                () -> Assertions.assertEquals(city, gardens.get(1).getLocation().getCity()),
                () -> Assertions.assertEquals(postcode, gardens.get(1).getLocation().getPostcode()),
                () -> Assertions.assertEquals(country, gardens.get(1).getLocation().getCountry())
        );
    }

    @Then("I can specify an edited street address and number {string}, suburb {string}, city {string}, postcode {string}, and country {string}")
    public void i_can_specify_an_edited_street_address_and_number_suburb_city_postcode_and_country(String addressLine1, String suburb, String city, String postcode, String country) throws Exception {
        addressDTO.setAddressLine1(addressLine1);
        addressDTO.setSuburb(suburb);
        addressDTO.setCity(city);
        addressDTO.setPostcode(postcode);
        addressDTO.setCountry(country);

        mockMvc.perform(requestBuilder.flashAttr("gardenDTO", gardenDTO))
                .andExpectAll(
                        redirectedUrl("/garden/1"),
                        model().hasNoErrors()
                );

        Assertions.assertAll(
                () -> Assertions.assertEquals(addressLine1, gardens.get(0).getLocation().getAddressLine1()),
                () -> Assertions.assertEquals(suburb, gardens.get(0).getLocation().getSuburb()),
                () -> Assertions.assertEquals(city, gardens.get(0).getLocation().getCity()),
                () -> Assertions.assertEquals(postcode, gardens.get(0).getLocation().getPostcode()),
                () -> Assertions.assertEquals(country, gardens.get(0).getLocation().getCountry())
        );
    }

    @When("The location can't be found by the location API")
    public void the_location_can_t_be_found_by_the_location_api() {
        addressDTO.setLatitude(null);
        addressDTO.setLongitude(null);
    }
}
