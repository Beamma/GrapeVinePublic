package nz.ac.canterbury.seng302.gardenersgrove.integration;


import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantController;
import nz.ac.canterbury.seng302.gardenersgrove.dto.AddressDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.exception.PlantNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.text.ParseException;

@SpringBootTest
@ActiveProfiles("test")
@Import(Plant.class)
public class PlantControllerTest {
    private PlantService mockPlantService;
    private PlantController plantController;
    private RedirectAttributes mockRedirectAttributes;
    private Model mockModel;
    private MockMvc mockMvc;

    @MockBean
    GardenService mockGardenService;

    @MockBean
    UserService userService;
    private User user1;

    @MockBean
    private WeatherService weatherService;

    @Autowired
    UserRepository userRepository;

    @MockBean
    JavaMailSender mailSender;

    EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = Mockito.mock(EmailService.class);
        mockPlantService = Mockito.mock(PlantService.class);
        mockGardenService = Mockito.mock(GardenService.class);
        plantController = new PlantController(mockPlantService, mockGardenService, userService);
        mockRedirectAttributes = Mockito.mock(RedirectAttributes.class);
        mockModel = Mockito.mock(Model.class);
        mockMvc = MockMvcBuilders.standaloneSetup(plantController).build();

        user1 = userRepository.save(new User("Liam@email.com", "2000-01-01", "Liam", "Ceelen", false, "Password1!", "Password1!"));
        AddressDTO validLocation = new AddressDTO("31 Home Avenue", "Ilam", "8041", "Christchurch", "New Zealand", -143.54, 35.356);

        Mockito.when(userService.getCurrentUser()).thenReturn(user1);
        Mockito.when(userService.validateUserId(String.valueOf(user1.getId()))).thenReturn(true);
        Mockito.when(userService.getById(String.valueOf(user1.getId()))).thenReturn(user1);
        Assertions.assertEquals(userService.getCurrentUser(), user1);
        Mockito.when(mockGardenService.checkGardenOwnership(1L)).thenReturn(true);
        Mockito.when(mockGardenService.checkGardenOwnership(2L)).thenReturn(true);
        Mockito.when(mockGardenService.checkGardenOwnership(99L)).thenReturn(false);

        Date date = null;

        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getId()).thenReturn(1L);
        Mockito.when(userService.getCurrentUser()).thenReturn(mockUser);

        Garden mockGarden1 = new Garden("Vegetable Garden", validLocation, 4.5, user1);
        Mockito.when(mockGardenService.getGardenByID(1L)).thenReturn(Optional.of(mockGarden1));

        Garden mockGarden2 = new Garden("Vegetable Garden", validLocation, 4.5, user1);
        Mockito.when(mockGardenService.getGardenByID(2L)).thenReturn(Optional.of(mockGarden2));

        // For testing getting plants by ID
        Plant mockPlant1 = new Plant(mockGarden1, "Tomato", null, null, null, "");
        Mockito.when(mockPlantService.getPlantByID(1L)).thenReturn(Optional.of(mockPlant1));

        Plant mockPlant2 = new Plant(mockGarden2, "Carrot", 1, null, null, "");
        Mockito.when(mockPlantService.getPlantByID(2L)).thenReturn(Optional.of(mockPlant2));

        Plant mockPlant3 = new Plant(mockGarden1, "Carrot", null, "Test3", null, "");
        Mockito.when(mockPlantService.getPlantByID(3L)).thenReturn(Optional.of(mockPlant3));

        Plant mockPlant4 = new Plant(mockGarden1, "Carrot", null, null, date, "");
        Mockito.when(mockPlantService.getPlantByID(4L)).thenReturn(Optional.of(mockPlant4));

        Plant mockPlant5 = new Plant(mockGarden1, "Carrot", 1, "Test5", date, "");
        Mockito.when(mockPlantService.getPlantByID(4L)).thenReturn(Optional.of(mockPlant5));

        Mockito.when(mockPlantService.getPlantByIdAndGardenId(1L, 1L)).thenReturn(Optional.of(mockPlant1));
        Mockito.when(mockPlantService.getPlantByIdAndGardenId(3L, 1L)).thenReturn(Optional.of(mockPlant3));
        Mockito.when(mockPlantService.getPlantByIdAndGardenId(4L, 1L)).thenReturn(Optional.of(mockPlant4));
        Mockito.when(mockPlantService.getPlantByIdAndGardenId(5L, 1L)).thenReturn(Optional.of(mockPlant5));
        Mockito.when(mockPlantService.getPlantByIdAndGardenId(2L, 2L)).thenReturn(Optional.of(mockPlant2));

        Mockito.when(mockPlantService.addPlant(Mockito.any(Plant.class))).thenAnswer(i -> i.getArguments()[0]);

        mockGarden1.setPlants(List.of(mockPlant1, mockPlant3, mockPlant4, mockPlant5));
        mockGarden2.setPlants(List.of(mockPlant2));

        this.mockMvc = MockMvcBuilders.standaloneSetup(plantController).build();
    }

    @Test
    public void getEditPlant_invalidGardenId_invalidPlantId() {
        try {
            mockMvc.perform(get("/garden/99/plant/99"))
                    .andExpectAll(status().is(403));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getEditPlant_validGardenId_invalidPlantId() {
        try {
            mockMvc.perform(get("/garden/1/plant/99"))
                    .andExpectAll(status().is(404));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getEditPlant_invalidGardenId_validPlantId() {
        try {
            mockMvc.perform(get("/garden/99/plant/1"))
                    .andExpectAll(status().is(403));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void givenNewPlant_plantNameEmpty_returnsNameError() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        Long gardenId = 1L;
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, image, gardenId, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("nameError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc!", "abc*", "abc(", "abc_", "abc+", "abc=", "abc{", "abc[", "]", "abc:"})
    public void giveNewPlant_plantNameInvalidChars_returnsNameError(String plantName) {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("nameError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void getEditPlant_gardenIdNotMatchPlantId() {
        try {
            mockMvc.perform(get("/garden/1/plant/2"))
                    .andExpectAll(status().is(404));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_plantNameNull_returnsNameError() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(null, description, datePlanted, count, image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("nameError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void givenNewPlant_plantNameGreaterThan255_returnsNameError() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "a".repeat(256);
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("nameError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void putEditPlant_invalidGardenId_validPlantId() {
        try {
            mockMvc.perform(put("/garden/99/plant/1")
                            .param("plantName", "Tulip")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpect(
                            status().is(403)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_descriptionGreaterThan512_returnsDescriptionError() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "test";
        String description = "a".repeat(513);
        String datePlanted = "2021-01-01";
        String count = "1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("descriptionError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void putEditPlant_invalidGardenId_invalidPlantId() {
        try {
            mockMvc.perform(put("/garden/99/plant/99")
                            .param("plantName", "Tulip")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpect(
                            status().is(403)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_countNotANumber_returnsCountError() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "not a number";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("countError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void putEditPlant_gardenIdNotMatchPlantId() {
        try {
            mockMvc.perform(put("/garden/1/plant/2")
                            .param("plantName", "Tulip")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpect(
                            status().is(404)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_countNegative_returnsCountError() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "-1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("countError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void givenPlantHasName_displaysName() {
        try {
            mockMvc.perform(get("/garden/1/plant/1"))
                    .andExpectAll(
                            status().is(200),
                            model().attribute("plantName", "Tomato"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("count"),
                            model().attributeDoesNotExist("description"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"01/01/2021", "01.01.2021", "01 01 2021"})
    public void givenNewPlant_dateNotValid_returnsDateError(String datePlanted) {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "test";
        String description = "test";
        String count = "1";
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, null, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelAndView modelResult = (ModelAndView) result;
        // Assert
        Assertions.assertTrue(modelResult.getModel().containsKey("dateError"));
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, modelResult.getStatus());
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void givenPlantHasNameCount_displaysNameCount() {
        try {
            mockMvc.perform(get("/garden/2/plant/2"))
                    .andExpectAll(
                            status().is(200),
                            model().attribute("plantName", "Carrot"),
                            model().attribute("count", "1"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("description"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_dateEmpty_plantCreated() {
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "";
        String count = "1";
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, null, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        String redirectResult = (String) result;
        // Assert
        Mockito.verify(mockRedirectAttributes, Mockito.times(1)).addFlashAttribute("datePlanted", "");
        Assertions.assertEquals("redirect:/garden/1", redirectResult);
        Mockito.verify(mockPlantService, Mockito.times(1)).addPlant(Mockito.any());
    }

    @Test
    public void givenPlantHasNameDesc_displaysNameDesc() {
        try {
            mockMvc.perform(get("/garden/1/plant/3"))
                    .andExpectAll(
                            status().is(200),
                            model().attribute("plantName", "Carrot"),
                            model().attributeDoesNotExist("count"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attribute("description", "Test3"),
                            model().attributeDoesNotExist("datePlanted")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_onlyNameAndCount_plantCreated() {
        // Arrange
        String plantName = "test";
        String count = "1";
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, "", "", count, null, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        String redirectResult = (String) result;
        // Assert
        Assertions.assertEquals("redirect:/garden/1", redirectResult);
        Mockito.verify(mockPlantService, Mockito.times(1)).addPlant(Mockito.any());
    }

    @Test
    public void givenPlantHasNameDate_displaysNameDate() {
        try {
            mockMvc.perform(get("/garden/1/plant/4"))
                    .andExpectAll(
                            status().is(200),
                            model().attribute("plantName", "Carrot"),
                            model().attributeDoesNotExist("count"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("description"),
                            model().attribute("datePlanted", "2024-01-01")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_onlyName_plantCreated() {
        // Arrange
        String plantName = "test";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, "", "", "", image, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        String redirect = (String) result;
        // Assert
        Assertions.assertEquals("redirect:/garden/1", redirect);
        Mockito.verify(mockPlantService, Mockito.times(1)).addPlant(Mockito.any());
    }

    @Test
    public void givenPlantHasNameCountDescDate_displaysNameCountDescDate() {
        try {
            mockMvc.perform(get("/garden/1/plant/5"))
                    .andExpectAll(
                            status().is(200),
                            model().attribute("plantName", "Carrot"),
                            model().attribute("count", "1"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attribute("description", "Test5"),
                            model().attribute("datePlanted", "2024-01-01")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlant_validPlant_plantCreated() {
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        // Act
        Object result = null;
        try {
            result =
                    plantController.submitForm(plantName, description, datePlanted, count, null, 1L, mockRedirectAttributes);
        } catch (PlantNotFoundException e) {
            throw new RuntimeException(e);
        }
        String redirectResult = (String) result;
        // Assert
        Assertions.assertEquals("redirect:/garden/1", redirectResult);
        Mockito.verify(mockPlantService, Mockito.times(1)).addPlant(Mockito.any());
    }

    @Test
    public void editPlant_nameEmpty() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "Tomato"),
                            model().attributeDoesNotExist("count"),
                            model().attribute("nameError", "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("description"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlant_nameInvalid() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "@Test")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "@Test"),
                            model().attributeDoesNotExist("count"),
                            model().attribute("nameError", "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("description"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlant_nameValid() {
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(302)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlantNameValid_negativeCount() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", "-1")
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "Tomato"),
                            model().attribute("count", "-1"),
                            model().attributeDoesNotExist("nameError"),
                            model().attribute("countError", "Plant count must be a positive whole number"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("description"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenNewPlantForm_formNotEmpty_returnsForm() {
        Mockito.when(mockGardenService.checkGardenOwnership(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        // Act
        String result = plantController.form(plantName, description, datePlanted, count, null, null, mockModel);
        // Assert
        Mockito.verify(mockModel, Mockito.times(1)).addAttribute("plantName", plantName);
        Mockito.verify(mockModel, Mockito.times(1)).addAttribute("description", description);
        Mockito.verify(mockModel, Mockito.times(1)).addAttribute("datePlanted", datePlanted);
        Mockito.verify(mockModel, Mockito.times(1)).addAttribute("count", count);
        Assertions.assertEquals("addPlantForm", result);
    }

    @Test
    public void editPlantNameValid_invalidCount() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", "Invalid")
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "Tomato"),
                            model().attribute("count", "Invalid"),
                            model().attributeDoesNotExist("nameError"),
                            model().attribute("countError", "Plant count must be a positive whole number"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attributeDoesNotExist("dateError"),
                            model().attributeDoesNotExist("description"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlantNameValid_validCount() {
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", "1.5")
                            .param("description", (String) null)
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(302)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlantNameValid_invalidDesc() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", (String) null)
                            .param("description", "@Test")
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "Tomato"),
                            model().attributeDoesNotExist("count"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attribute("descriptionError", "Description must only include letters, numbers, spaces, dots, hyphens or apostrophes"),
                            model().attributeDoesNotExist("dateError"),
                            model().attribute("description", "@Test"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlantNameValid_validDesc() {
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", (String) null)
                            .param("description", "Test3")
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(302)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlantNameValid_tooLongDesc() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", (String) null)
                            .param("description", "TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestT")
                            .param("datePlanted", (String) null))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "Tomato"),
                            model().attributeDoesNotExist("count"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attribute("descriptionError", "Description must be less than 512 characters"),
                            model().attributeDoesNotExist("dateError"),
                            model().attribute("description", "TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestT"),
                            model().attributeDoesNotExist("datePlanted")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void editPlantNameValid_invalidDate() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        try {
            mockMvc.perform(put("/garden/1/plant/1")
                            .param("plantName", "Tomato")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", "x"))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "Tomato"),
                            model().attributeDoesNotExist("count"),
                            model().attributeDoesNotExist("nameError"),
                            model().attributeDoesNotExist("countError"),
                            model().attributeDoesNotExist("descriptionError"),
                            model().attribute("dateError", "Date in not valid format, DD-MM-YYYY"),
                            model().attributeDoesNotExist("description"),
                            model().attribute("datePlanted", "x")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenPostRequest_plantValid_returnsFound() throws Exception {
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);

        // Act
        mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/1/plant")
                        .file(image)
                        .param("plantName", plantName)
                        .param("description", description)
                        .param("datePlanted", datePlanted)
                        .param("count", count))
                .andExpectAll(status().isFound());
        // Assert
        Mockito.verify(mockPlantService, Mockito.times(1)).addPlant(Mockito.any());
    }

    @Test
    public void editPlantNameValid_validDate() {
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/garden/1/plant/1");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        try {
            mockMvc.perform(builder
                            .file(image)
                            .param("plantName", "Tomato")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", "2024-03-12"))
                    .andExpectAll(
                            status().is(302)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenPostRequest_plantInvalid_returnsBadRequest() throws Exception {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "-1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);

        // Act
        mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/1/plant")
                        .file(image)
                        .param("plantName", plantName)
                        .param("description", description)
                        .param("datePlanted", datePlanted)
                        .param("count", count))
                .andExpectAll(status().isBadRequest());
        // Assert
        Mockito.verify(mockPlantService, Mockito.never()).addPlant(Mockito.any());
    }

    @Test
    public void editPlant_allInvalid() {
        Mockito.when(mockPlantService.hasErrors(Mockito.any())).thenReturn(true);
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/garden/1/plant/1");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        try {
            mockMvc.perform(builder
                            .file(image)
                            .param("plantName", "@Tomato")
                            .param("count", "@Count")
                            .param("description", "@Test")
                            .param("datePlanted", "x"))
                    .andExpectAll(
                            status().is(400),
                            model().attribute("plantName", "@Tomato"),
                            model().attribute("count", "@Count"),
                            model().attribute("nameError", "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes"),
                            model().attribute("countError", "Plant count must be a positive whole number"),
                            model().attribute("descriptionError", "Description must only include letters, numbers, spaces, dots, hyphens or apostrophes"),
                            model().attribute("dateError", "Date in not valid format, DD-MM-YYYY"),
                            model().attribute("description", "@Test"),
                            model().attribute("datePlanted", "x")
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @WithMockUser
    public void editPlant_allValid() {
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/garden/1/plant/1");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        try {
            mockMvc.perform(builder
                            .param("plantName", (String) "Tomato")
                            .param("count", (String) "1.5")
                            .param("description", (String) "Test")
                            .param("datePlanted", (String) "2024-03-12"))
                    .andExpectAll(
                            status().is(302)
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenGetRequest_getRequestValid_returnsForm() throws Exception {
        // Arrange
        String plantName = "test";
        String description = "test";
        String datePlanted = "2021-01-01";
        String count = "1";
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);

        // Act
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/1/plant")
                        .file(image)
                        .param("plantName", plantName)
                        .param("description", description)
                        .param("datePlanted", datePlanted)
                        .param("count", count))
                .andExpectAll(status().isFound());
    }

    @Test
    public void GivenUser1CreateGardenPlantTheyOwn_ReturnGardenInfo () {
        Mockito.when(mockGardenService.checkGardenOwnership(1L)).thenReturn(true);
        try {
            mockMvc.perform(get("/garden/1/plant"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("plantName"))
                    .andExpect(view().name("addPlantForm"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenPlantDontOwn_Return403AndError () {
        Mockito.when(mockGardenService.checkGardenOwnership(2L)).thenReturn(false);
        try {
            String plantName = "test";
            String description = "test";
            String datePlanted = "2021-01-01";
            String count = "1";
            MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);

            // Act
            mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/2/plant")
                            .file(image)
                            .param("plantName", plantName)
                            .param("description", description)
                            .param("datePlanted", datePlanted)
                            .param("count", count))
                    .andExpectAll(status().isForbidden(),
                            model().attribute("status", 403),
                            model().attributeDoesNotExist("garden"),
                            view().name("error")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenPlantDoesntExist_Return403AndError () {
        Mockito.when(mockGardenService.checkGardenOwnership(99L)).thenReturn(false);
        try {
            String plantName = "test";
            String description = "test";
            String datePlanted = "2021-01-01";
            String count = "1";
            MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);

            // Act
            mockMvc.perform(MockMvcRequestBuilders.multipart("/garden/99/plant")
                            .file(image)
                            .param("plantName", plantName)
                            .param("description", description)
                            .param("datePlanted", datePlanted)
                            .param("count", count))
                    .andExpectAll(status().isForbidden(),
                            model().attribute("status", 403),
                            model().attributeDoesNotExist("garden"),
                            view().name("error")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenPlantTheyOwn_ReturnGardenInfo () {
        Mockito.when(mockGardenService.checkGardenOwnership(1L)).thenReturn(true);
        try {
            mockMvc.perform(get("/garden/1/plant/1"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("plantName"))
                    .andExpect(view().name("addPlantForm"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void GivenUser1AccessesGardenPlantTheyDontOwn_ReturnGardenInfo () {
        Mockito.when(mockGardenService.checkGardenOwnership(2L)).thenReturn(false);
        try {
            mockMvc.perform(get("/garden/2/plant/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(model().attributeDoesNotExist("plantName"))
                    .andExpect(view().name("error"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1AccessesGardenPlantDontExist_ReturnGardenInfo () {
        Mockito.when(mockGardenService.checkGardenOwnership(99L)).thenReturn(false);
        try {
            mockMvc.perform(get("/garden/99/plant/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(model().attributeDoesNotExist("plantName"))
                    .andExpect(view().name("error"))
                    .andReturn();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void GivenUser1UpdateGardenPlantDoesntExist_Return403AndError () {
        Mockito.when(mockGardenService.checkGardenOwnership(1L)).thenReturn(true);
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/garden/1/plant/1");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        try {
            mockMvc.perform(builder
                            .file(image)
                            .param("plantName", (String) "Tomato")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) "2024-03-12"))
                    .andExpectAll(
                            status().is(302)
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1UpdateGardenPlantNotOwned_Return403AndError () {
        Mockito.when(mockGardenService.checkGardenOwnership(2L)).thenReturn(false);
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/garden/2/plant/2");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        try {
            mockMvc.perform(builder
                            .file(image)
                            .param("plantName", (String) "Tomato")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) "2024-03-12"))
                    .andExpectAll(
                        model().attribute("status", 403),
                        model().attributeDoesNotExist("plantName"),
                        view().name("error")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void GivenUser1UpdateGardenPlantNotExist_Return403AndError () {
        Mockito.when(mockGardenService.checkGardenOwnership(99L)).thenReturn(false);
        MockMultipartFile image = new MockMultipartFile("plantImage", "image.jpg", "image/jpeg", new byte[1000]);
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/garden/99/plant/2");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        try {
            mockMvc.perform(builder
                            .file(image)
                            .param("plantName", (String) "Tomato")
                            .param("count", (String) null)
                            .param("description", (String) null)
                            .param("datePlanted", (String) "2024-03-12"))
                    .andExpectAll(
                            model().attribute("status", 403),
                            model().attributeDoesNotExist("plantName"),
                            view().name("error")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
