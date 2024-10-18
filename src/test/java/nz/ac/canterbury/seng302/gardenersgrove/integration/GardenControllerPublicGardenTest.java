package nz.ac.canterbury.seng302.gardenersgrove.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class GardenControllerPublicGardenTest {

    @Autowired
    GardenRepository gardenRepository;
    @Autowired
    GardenService gardenService;
    @Autowired
    UserService userService;
    @Autowired
    FriendService friendService;
    @Autowired
    GardenFilterService gardenFilterService;
    @Autowired
    UserRepository userRepository;
    @MockBean
    WeatherService mockWeatherService;
    @MockBean
    ProfanityFilterService mockProfanityFilterService;
    GardenController gardenController;
    MockMvc mockMvc;
    AddressDTO validLocation;
    User user;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        // set up user
        user = userRepository.save(new User("user@example.com", "2001-01-01", "John", "Doe", false, "Password1!", "Password1!"));
        var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // mock profanity filter service
        Mockito.when(mockProfanityFilterService.isTextProfane(Mockito.any())).thenReturn(false);

        validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);

        Garden garden1 = new Garden("Garden 1", validLocation, user);
        garden1.setPublicGarden(true);
        gardenRepository.save(garden1);
        Garden garden2 = new Garden("Garden 2", validLocation, user);
        garden2.setPublicGarden(true);
        gardenRepository.save(garden2);
        Garden garden3 = new Garden("garden 3", validLocation, user);
        garden3.setPublicGarden(true);
        gardenRepository.save(garden3);
        Garden garden4 = new Garden("Garden 4", validLocation, user);
        garden4.setPublicGarden(true);
        gardenRepository.save(garden4);

        gardenController = new GardenController(gardenService, userService, friendService,
                mockWeatherService, mockProfanityFilterService, gardenFilterService);


        mockMvc = MockMvcBuilders.standaloneSetup(gardenController).build();
    }

    @AfterEach
    @Transactional
    public void tearDown() {
        gardenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void UserHasFourPublicGardens_SearchByNameReturnsCorrectGarden() throws Exception {
        String search = "Garden 2";

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isArray(),
                        MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)),
                        MockMvcResultMatchers.jsonPath("$[0].name").value("Garden 2")
                );


    }

    @Test
    void UserHasFourPublicGardens_SearchByNameReturnsMultipleCorrectGarden() throws Exception {
        String search = "Garden";

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isArray(),
                        MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)),
                        MockMvcResultMatchers.jsonPath("$[0].name").value("Garden 4"),
                        MockMvcResultMatchers.jsonPath("$[1].name").value("garden 3"),
                        MockMvcResultMatchers.jsonPath("$[2].name").value("Garden 2"),
                        MockMvcResultMatchers.jsonPath("$[3].name").value("Garden 1")
                );
    }

    @Test
    void UserHasNoPublicGardens_SearchByNameReturnsErrorMessage() throws Exception {
        gardenRepository.deleteAll();

        String search = "Garden";

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isArray(),
                        MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0))
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"@", "$", "*", "gar@"})
    void UserHasFourPublicGardens_SearchByInvalidNameReturnsErrorMessage(String search) throws Exception {

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isMap(),
                        MockMvcResultMatchers.jsonPath("$.errorMessage").value("Invalid search. Queries may only contain alphanumeric characters, -, ‘, dots, commas and spaces")
                );
    }

    @Test
    void UserHasPublicGardens_SearchByCapsName_ReturnsMultipleCorrectGardens() throws Exception {
        String search = "GARDEN";

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isArray(),
                        MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(4)),
                        MockMvcResultMatchers.jsonPath("$[0].name").value("Garden 4"),
                        MockMvcResultMatchers.jsonPath("$[1].name").value("garden 3"),
                        MockMvcResultMatchers.jsonPath("$[2].name").value("Garden 2"),
                        MockMvcResultMatchers.jsonPath("$[3].name").value("Garden 1")
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"q", "QU", "34", ",", ".", "-", "‘"})
    void UserHasNoMatchingPublicGardens_SearchByValidNameReturnsNoGardens(String search) throws Exception {

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isArray(),
                        MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0))
                );
    }


    @Test
    void UserHasNoMatchingPublicGardens_SearchBySpaceReturnsNoGardens() throws Exception {

        String search = " ";

        // Ensure there are no gardens with spaces in their names
        gardenRepository.deleteAll();
        Garden garden4 = new Garden("Garden4", validLocation, user);
        garden4.setPublicGarden(true);
        gardenRepository.save(garden4);
        Garden garden3 = new Garden("garden", validLocation, user);
        garden3.setPublicGarden(true);
        gardenRepository.save(garden3);

        mockMvc.perform(get("/garden/public/search").with(csrf())
                        .param("name", search))
                .andExpectAll(
                        status().isOk(),
                        MockMvcResultMatchers.jsonPath("$").isArray(),
                        MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0))
                );
    }
}
