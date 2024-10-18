package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.PlantImageStorageProperties;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PlantValidator;
import nz.ac.canterbury.seng302.gardenersgrove.utility.GardenUtils;
import nz.ac.canterbury.seng302.gardenersgrove.exception.PlantNotFoundException;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ImageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.text.DateFormat;
import java.util.Map;

import static java.lang.Long.parseLong;

/**
 * Controller for the plant page
 */
@Controller
public class PlantController {

    Logger logger = LoggerFactory.getLogger(PlantController.class);

    private final PlantService plantService;

    private final GardenService gardenService;

    private final UserService userService;

    private static final String NAV_BAR_USER = "navBarUser";

    /**
     * Constructor for PlantController
     * @param plantService plant service
     * @param gardenService garden service
     */
    @Autowired
    public PlantController(PlantService plantService, GardenService gardenService, UserService userService) {
        this.plantService = plantService;
        this.gardenService = gardenService;
        this.userService = userService;
        this.plantService.setStorageProperties(new PlantImageStorageProperties());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ModelAndView modelAndView = new ModelAndView("404");
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return modelAndView;
    }

    /**
     * Gets form to be displayed, includes the ability to display results of previous form when linked to from POST form
     *
     * @param plantName   previous name entered into form to be displayed
     * @param description previous description entered into form to be displayed
     * @param datePlanted previous date planted entered into form to be displayed
     * @param count       previous count entered into form to be displayed
     * @param gardenId    id of the garden to add the plant to
     * @param plantImage  image of the plant
     * @param model       (map-like) representation of plantName, description, datePlanted and count for use in thymeleaf
     * @return thymeleaf addPlantForm
     */
    @GetMapping("/garden/{gardenId}/plant")
    public String form(@RequestParam(name = "plantName", required = false, defaultValue = "") String plantName,
                       @RequestParam(name = "description", required = false, defaultValue = "") String description,
                       @RequestParam(name = "datePlanted", required = false, defaultValue = "") String datePlanted,
                       @RequestParam(name = "count", required = false, defaultValue = "") String count,
                       @PathVariable("gardenId") Long gardenId,
                       @RequestParam(name = "plantImage", required = false) MultipartFile plantImage,
                       Model model) {
        logger.info("GET /plant/new");

        if (!gardenService.checkGardenOwnership(gardenId)) {
            model.addAttribute("error", "Forbidden, you do not own this garden");
            model.addAttribute("status", 403);
            return "error";
        }

        model.addAttribute("plantName", plantName);
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("description", description);
        model.addAttribute("datePlanted", datePlanted);
        model.addAttribute("count", count);
        if (plantImage != null) {
            model.addAttribute("plantImage", plantImage);
        }

        model.addAttribute("gardens", gardenService.getGardens());
        plantService.clearPlantImageCache(userService.getCurrentUser().getId().toString());
        model.addAttribute("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        model.addAttribute(NAV_BAR_USER, userService.getCurrentUser());

        return "addPlantForm";
    }

    /**
     * Posts a form response with plant name, description, date planted and count
     *
     * @param plantName          name of plant
     * @param description        description of plant
     * @param datePlanted        date the plant was planted
     * @param count              number of plants
     * @param plantImage         image of the plant
     * @param gardenId           id of the garden to add the plant to
     * @param redirectAttributes attributes to be added to the redirect
     * @return thymeleaf addPlantForm
     */
    @PostMapping("/garden/{gardenId}/plant")
    public Object submitForm(@RequestParam(name = "plantName") String plantName,
                             @RequestParam(name = "description") String description,
                             @RequestParam(name = "datePlanted") String datePlanted,
                             @RequestParam(name = "count") String count,
                             @RequestParam(name = "plantImage") MultipartFile plantImage,
                             @PathVariable("gardenId") Long gardenId,
                             RedirectAttributes redirectAttributes) throws PlantNotFoundException {
        logger.info(String.format("POST /garden/%d/plant", gardenId));

        ModelAndView modelAndView = new ModelAndView();

        Optional<Garden> garden = gardenService.getGardenByID(gardenId);
        if (!gardenService.checkGardenOwnership(gardenId)) {
            modelAndView.setStatus(HttpStatus.FORBIDDEN);
            modelAndView.setViewName("error");
            modelAndView.addObject("error", "Forbidden, you do not own this garden");
            modelAndView.addObject("status", 403);
            return modelAndView;
        }


        modelAndView.addObject("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        modelAndView.addObject(NAV_BAR_USER, userService.getCurrentUser());
        modelAndView.setViewName("addPlantForm");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);


        modelAndView.addObject("plantName", plantName);
        modelAndView.addObject("description", description);
        modelAndView.addObject("datePlanted", datePlanted);
        modelAndView.addObject("count", count);
        modelAndView.addObject("plantImage", plantImage);

        Integer parsedCount = 1;
        Date parsedDate = null;

        // Name Validation
        if (PlantValidator.isFieldEmpty(plantName) || !PlantValidator.isPlantNameValid(plantName)) {
            modelAndView.addObject("nameError",
                    "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        } else if (!PlantValidator.isNameValidLength(plantName)) {
            modelAndView.addObject("nameError", "Plant name must be less than 256 characters");
        }

        // Description Validation
        if (!PlantValidator.isFieldEmpty(description) && !PlantValidator.isDescriptionLengthValid(description)) {
            modelAndView.addObject("descriptionError", "Plant description must be less than 512 characters");
        }

        // Count Validation
        if (!PlantValidator.isFieldEmpty(count)) {
            if (!PlantValidator.isCountValid(count)) {
                modelAndView.addObject("countError", "Plant count must be a positive whole number");
            } else {
                if (!PlantValidator.isCountWithinRange(count)) {
                    modelAndView.addObject("countError", "Plant count must be less than 1,000,000,000");
                } else {
                    parsedCount = Integer.parseInt(count);
                }
            }
        }

        // Date Validation
        if (!(datePlanted == null || datePlanted.isEmpty())) {
            try {
                if (!PlantValidator.isDateValid(datePlanted)) {
                    throw new ParseException("Invalid date", 0);
                }
                parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(datePlanted);
            } catch (ParseException e) {
                modelAndView.addObject("dateError", "Date is not in valid format, DD/MM/YYYY");
            }
        }

        // Image Validation
        if (plantImage != null && !plantImage.isEmpty()) {
            if (!ImageValidator.isImageTypeCorrect(plantImage)) {
                modelAndView.addObject("imageError", "Image must be JPG, JPEG, SVG, or PNG");
            }
            if (!ImageValidator.isImageSizeValid(plantImage)) {
                modelAndView.addObject("imageError", "Image must be less than 10MB");
            }
        }

        String userId = userService.getCurrentUser().getId().toString();

        if (plantService.hasErrors(modelAndView)) {
            if ((!(plantImage == null || (plantImage.isEmpty()))) && !modelAndView.getModel().containsKey("imageError")) {
                plantService.cachePlantImage(plantImage, userId);
                try {
                    modelAndView.addObject("existingPlantImage", plantService.getCachedPlantImageBase64(userId));
                } catch (IOException e) {
                    modelAndView.addObject("imageError", "Error displaying image");
                }
            } else if (plantService.cachedPlantImageExists(userId)) {
                try {
                    modelAndView.addObject("existingPlantImage", plantService.getCachedPlantImageBase64(userId));
                } catch (IOException e) {
                    modelAndView.addObject("imageError", "Error displaying image");
                }
            }
            return modelAndView;
        }

        if (garden.isEmpty()) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }

        Plant addPlant;
        addPlant = new Plant(garden.get(), plantName, parsedCount, description, parsedDate, "");

        Plant addedPlant = plantService.addPlant(addPlant);

        // Can only store the plant image once the plant id has been generated in order for the file to have a correct name
        if (plantImage != null && !plantImage.isEmpty()) {
            String extension = plantImage.getContentType().substring(6);
            if (extension.contains("svg")) {
                extension = "svg";
            }
            plantService.store(plantImage, String.format("%d,%d", gardenId, addedPlant.getPlantId()), extension);
            try {
                plantService.updatePlant(addedPlant.getPlantId(), plantName, description, parsedCount, parsedDate, String.format("%d,%d.%s", gardenId, addedPlant.getPlantId(), extension));
            } catch (PlantNotFoundException e) {
                throw new PlantNotFoundException("Plant not found");
            }
        } else if (plantService.cachedPlantImageExists(userId)) {
            String imagePath = plantService.storeCachedPlantImage(userId, String.format("%d,%d", gardenId, addedPlant.getPlantId()));
            if (imagePath != null) {
                plantService.updatePlant(addedPlant.getPlantId(), plantName, description, parsedCount, parsedDate, imagePath);
            }
        }

        redirectAttributes.addFlashAttribute("count", parsedCount);
        redirectAttributes.addFlashAttribute("plantName", plantName);
        redirectAttributes.addFlashAttribute("description", description);
        redirectAttributes.addFlashAttribute("datePlanted", datePlanted);
        redirectAttributes.addFlashAttribute("plantImage", plantImage);

        modelAndView.clear();
        modelAndView.setStatus(HttpStatus.FOUND);
        return "redirect:/garden/" + gardenId;
    }

    /**
     * Gets form to be displayed, includes the ability to display results of previous form when linked to from PUT form
     *
     * @param model (map-like) representation of name, count, description, and date for use in thymeleaf
     * @return thymeleaf editPlantForm
     */
    @GetMapping("/garden/{gardenId}/plant/{plantId}")
    public ModelAndView editPlantGet(@PathVariable("plantId") String plantId,
                                     @PathVariable("gardenId") String gardenId,
                                     Model model) {
        logger.info(String.format("GET /garden/{gardenId}/plant/{plantId}"));
        ModelAndView modelAndView = new ModelAndView();

        if (! gardenService.checkGardenOwnership(parseLong(gardenId))) {
            modelAndView.setStatus(HttpStatus.FORBIDDEN);
            modelAndView.setViewName("error");
            modelAndView.addObject("error", "Forbidden, you do not own this garden");
            modelAndView.addObject("status", 403);
            return modelAndView;
        }

        modelAndView.addObject("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        modelAndView.addObject(NAV_BAR_USER, userService.getCurrentUser());
        plantService.clearPlantImageCache(userService.getCurrentUser().getId().toString());

        if (!(plantId.matches("^[0-9]+"))) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }

        Optional<Plant> plant = plantService.getPlantByIdAndGardenId(parseLong(plantId), parseLong(gardenId));

        if (plant.isEmpty()) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }

        modelAndView.addObject("editPlant", "true");
        modelAndView.addObject("plantName", plant.get().getName());
        if (plant.get().getCount() != null) {
            String count = Integer.toString(plant.get().getCount());
            modelAndView.addObject("count", count);
        }
        if (plant.get().getDescription() != null) {
            modelAndView.addObject("description", plant.get().getDescription());
        }
        if (plant.get().getDatePlanted() != null) {
            Date date = plant.get().getDatePlanted();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strDate = dateFormat.format(date);
            modelAndView.addObject("datePlanted", strDate);
        }

        if (plant.get().getPlantImageSize() != 0) {
            try {
                modelAndView.addObject("existingPlantImage", plant.get().getPlantImageBase64());
            } catch (IOException e) {
                modelAndView.addObject("imageError", "Error loading image");
            }
        }

        modelAndView.setStatus(HttpStatus.OK);
        modelAndView.setViewName("addPlantForm");
        return modelAndView;
    }

    @PutMapping("/garden/{gardenId}/plant/{plantId}")
    public Object editPlantPut(@PathVariable("gardenId") Long gardenId,
                                     @PathVariable("plantId") Long plantId,
                                     @RequestParam(name = "plantName") String plantName,
                                     @RequestParam(name = "count", required = false) String count,
                                     @RequestParam(name = "description", required = false) String description,
                                     @RequestParam(name = "datePlanted", required = false) String datePlanted,
                                     @RequestParam(name = "plantImage", required = false) MultipartFile plantImage,
                                     RedirectAttributes redirectAttributes) {

        logger.info(String.format("PUT /garden/%d/plant/%d", gardenId, plantId));
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("gardens", gardenService.getGardens());

        Date parsedDate = null;
        String userId = userService.getCurrentUser().getId().toString();
        // Plant id validation

        if (!(plantId.toString().matches("^[0-9]+"))) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }
        if (!gardenService.checkGardenOwnership(gardenId)) {
            modelAndView.setStatus(HttpStatus.FORBIDDEN);
            modelAndView.setViewName("error");
            modelAndView.addObject("error", "Forbidden, you do not own this garden");
            modelAndView.addObject("status", 403);
            return modelAndView;
        }
        modelAndView.addObject("gardens", gardenService.getGardensByUserId(userService.getCurrentUser().getId()));
        modelAndView.addObject(NAV_BAR_USER, userService.getCurrentUser());

        // Plant name validation
        Optional<Plant> plant = plantService.getPlantByIdAndGardenId(plantId, gardenId);

        if (plant.isEmpty()) {
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            modelAndView.setViewName("404");
            return modelAndView;
        }
        if (PlantValidator.isFieldEmpty(plantName)) {
            modelAndView.addObject("nameError", "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        } else if (!PlantValidator.isPlantNameValid(plantName)) {
            modelAndView.addObject("nameError", "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        } else if (!PlantValidator.isNameValidLength(plantName)) {
            modelAndView.addObject("nameError", "Plant name must be less than 256 characters");
        }
        // description validation
        if (PlantValidator.isFieldEmpty(description)) {
            description = null;
        } else if (!PlantValidator.isDescriptionValid(description)) {
            modelAndView.addObject("descriptionError", "Description must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        } else if (!PlantValidator.isDescriptionLengthValid(description)) {
            modelAndView.addObject("descriptionError", "Description must be less than 512 characters");
        }
        if (!(datePlanted == null || datePlanted.isEmpty())) {
            try {
                if (!PlantValidator.isDateValid(datePlanted)) {
                    throw new ParseException("Invalid date", 0);
                }
                parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(datePlanted);
            } catch (ParseException e) {
                modelAndView.addObject("dateError", "Date in not valid format, DD-MM-YYYY");
                modelAndView.addObject("datePlanted", datePlanted);
            }
        }
        //Count Validation
        Integer parsedCount = null;
        if (!PlantValidator.isFieldEmpty(count)) {
            if (!PlantValidator.isCountValid(count)) {
                modelAndView.addObject("countError", "Plant count must be a positive whole number");
            } else {
                if (!PlantValidator.isCountWithinRange(count)) {
                    modelAndView.addObject("countError", "Plant count must be less than 1,000,000,000");
                } else {
                    parsedCount = Integer.parseInt(count);
                }
            }
        }

        String plantImagePath = "";
        if (!(plantImage == null || (plantImage.isEmpty()))) {
            if (!ImageValidator.isImageTypeCorrect(plantImage)) {
                modelAndView.addObject("imageError", "Image must be JPG, JPEG, SVG, or PNG");
                modelAndView.addObject("plantImage",  plant.get().getPlantImage());
            }
            if (!ImageValidator.isImageSizeValid(plantImage)) {
                modelAndView.addObject("imageError", "Image must be less than 10MB");
                modelAndView.addObject("plantImage",  plant.get().getPlantImage());
            }
            // Only store the image if the edit form is valid
            if (!plantService.hasErrors(modelAndView)) {
                plantService.store(plantImage, String.format("%d,%d", gardenId, plant.get().getPlantId()), plantImage.getContentType().substring(6));
                plantImagePath = String.format("%d,%d", gardenId, plantId) + '.' + plantImage.getContentType().substring(6);
            }
        } else if (plant.get().getPlantImageSize() != 0) {
            plantImagePath = plant.get().getPlantImage();
        }
        if (!plantService.hasErrors(modelAndView)) {
            redirectAttributes.addFlashAttribute("plantName", plantName);
            try {
                // If the user has a cached image then use that instead of the stored image in the database
                if (plantService.cachedPlantImageExists(userId) && (plantImage == null || plantImage.isEmpty())) {
                    String imagePath = plantService.storeCachedPlantImage(userId, String.format("%d,%d", gardenId, plant.get().getPlantId()));
                    if (imagePath != null) {
                        plantImagePath = imagePath;
                    }
                }
                plantService.updatePlant(plantId, plantName, description, parsedCount, parsedDate, plantImagePath);
            } catch (PlantNotFoundException e) {
                modelAndView.setStatus(HttpStatus.NOT_FOUND);
                modelAndView.setView(new RedirectView("404"));
                return modelAndView;
            }
            modelAndView.clear();
            modelAndView.setView(new RedirectView(String.format("garden/%d", gardenId)));
            return String.format("redirect:/garden/%d", gardenId);
        } else {
            modelAndView.addObject("count",  count);
            modelAndView.addObject("description",  description);
            modelAndView.addObject("datePlanted",  datePlanted);
            modelAndView.addObject("plantImage",  plantImage);
        }
        Optional<Plant> originalPlant = plantService.getPlantByID(plantId);
        if (originalPlant.isPresent()) {
            modelAndView.addAllObjects(
                    Map.of("plantName", GardenUtils.gardenNameFormRepopulation(plantName, originalPlant.get().getName())));
        } else {
            modelAndView.addAllObjects(
                    Map.of("plantName", GardenUtils.gardenNameFormRepopulation(plantName)));
        }
        // If the user has submitted a valid image but the rest of the form is not correct...
        if ((!(plantImage == null || (plantImage.isEmpty()))) && !modelAndView.getModel().containsKey("imageError")) {
            plantService.cachePlantImage(plantImage, userId);
            try {
                modelAndView.addObject("existingPlantImage", plantService.getCachedPlantImageBase64(userId));
            } catch (IOException e) {
                modelAndView.addObject("imageError", "Error displaying image");
            }
        } else if (plantService.cachedPlantImageExists(userId)) {
            try {
                modelAndView.addObject("existingPlantImage", plantService.getCachedPlantImageBase64(userId));
            } catch (IOException e) {
                modelAndView.addObject("imageError", "Error displaying image");
            }
        } else if (plant.get().getPlantImageSize() != 0){
            // The image the user did (or didn't) provide just wasn't good enough so revert to image in database
            try {
                modelAndView.addObject("existingPlantImage", plant.get().getPlantImageBase64());
            } catch (IOException e) {
                modelAndView.addObject("imageError", "Error displaying image");
            }
        }
        modelAndView.setViewName("addPlantForm");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        return modelAndView;
    }
}