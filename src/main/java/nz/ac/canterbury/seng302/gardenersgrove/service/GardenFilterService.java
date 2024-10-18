package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseGardenDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service class for Filtering gardens in browse gardens, defined by the @link{Service} annotation.
 */
@Service
public class GardenFilterService {
    private final GardenRepository gardenRepository;
    @Autowired
    public GardenFilterService(GardenRepository gardenRepository) {
        this.gardenRepository = gardenRepository;
    }

    /**
     * Takes a browseGardenDTO, takes the tags in the DTO in the form of the string, and sets the tagList in the DTO
     * with the corresponding tags in a list.
     * @param browseGardenDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void parseTags(BrowseGardenDTO browseGardenDTO) {
        String tags = browseGardenDTO.getTagsString();
        if (tags == null || tags.isEmpty()) { // On render of browse gardens page
            browseGardenDTO.setTags(Collections.emptyList());
            return;
        }
        browseGardenDTO.setTags(Arrays.asList(tags.split(",")));
    }

    /**
     * Checks that the DTO pages are integers and larger than 0, if not sets them the page in the DTO as 1
     * @param browseGardenDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void parsePages(BrowseGardenDTO browseGardenDTO){
        int parsedPage;
        try {
            parsedPage = Integer.parseInt(browseGardenDTO.getPage());
            if (parsedPage < 1) {
                parsedPage = 1;
            }
        } catch (NumberFormatException e) {
            parsedPage = 1;
        }

        browseGardenDTO.setParsedPage(parsedPage);
    }

    /**
     * Pass in a DTO with all the required information, and it will query the database, returning filtered gardens
     * @param browseGardenDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void getGardens(BrowseGardenDTO browseGardenDTO) {
        Page<Garden> gardensPage = queryRepository(browseGardenDTO); // Handles Making the correct query based off filter params

        // If the user has input a page size that is larger than possible:
        int searchSize = (int) gardensPage.getTotalElements();
        if (browseGardenDTO.getParsedPage() > gardensPage.getTotalPages()) {
            browseGardenDTO.setParsedPage(gardensPage.getTotalPages());
            if (searchSize != 0) {
                gardensPage = queryRepository(browseGardenDTO); // Handles Making the correct query based off filter params
            }
        }

        List<Garden> searchedGardens = gardensPage.getContent(); //List of resulting Gardens
        browseGardenDTO.setTotalPages(gardensPage.getTotalPages());

        if (searchedGardens.isEmpty()) {
            browseGardenDTO.setSearchError("No gardens match your search");
        } else {
            browseGardenDTO.setSearchSize(searchSize);
            browseGardenDTO.setGardens(searchedGardens);
        }
    }

    /**
     * Handles Making the correct query based off filter params
     * @param browseGardenDTO is a DTO to carry and transfer all data between service layer, controller and view
     * @return returns a Page of Gardens
     */
    private Page<Garden> queryRepository(BrowseGardenDTO browseGardenDTO) {
        Page<Garden> gardensPage;
        if (!browseGardenDTO.getSearch().isEmpty() && !browseGardenDTO.getTags().isEmpty()) {
            // Filtering By Tags and Search
            browseGardenDTO.setPageSize(9);
            gardensPage = gardenRepository.findAllGardensByNameAndTagsPageable(browseGardenDTO.getSearch(), browseGardenDTO.getTags(), PageRequest.of(browseGardenDTO.getParsedPage() - 1, browseGardenDTO.getPageSize()));
        } else if (!browseGardenDTO.getSearch().isEmpty()) {
            // Filtering By Search Only
            browseGardenDTO.setPageSize(9);
            gardensPage = gardenRepository.findAllPublicGardensByNamePageable(browseGardenDTO.getSearch(), PageRequest.of(browseGardenDTO.getParsedPage() - 1, browseGardenDTO.getPageSize()));
        } else if (!browseGardenDTO.getTags().isEmpty()) {
            // Filtering By Tags Only
            browseGardenDTO.setPageSize(9);
            gardensPage = gardenRepository.findAllPublicGardensByTagsPageable(browseGardenDTO.getTags(), PageRequest.of(browseGardenDTO.getParsedPage() - 1, browseGardenDTO.getPageSize()));
        }else {
            // Get 10 most recent gardens
            browseGardenDTO.setPageSize(10);
            gardensPage = gardenRepository.findRecentPublicGardens(PageRequest.of(browseGardenDTO.getParsedPage() - 1, browseGardenDTO.getPageSize()));
        }
        return gardensPage;
    }
}
