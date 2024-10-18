package nz.ac.canterbury.seng302.gardenersgrove.service;

import nz.ac.canterbury.seng302.gardenersgrove.dto.BrowseLiveStreamsDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.LivestreamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The service class for live-streaming.
 */
@Service
public class LivestreamService {

    @Autowired
    LivestreamRepository livestreamRepository;

    @Autowired
    UserService userService;

    @Autowired
    ImageService imageService;

    /**
     * Gets a live stream given an id.
     *
     * @param id    The id of the live stream
     * @return      The live stream or null if not found
     */
    public Livestream getLivestream(Long id) {
        return livestreamRepository.findById(id).orElse(null);
    }

    /**
     * Creates a live stream given a DTO and owner.
     *
     * @param liveStreamDTO     The DTO containing title and description and an optional image
     * @return                  The created live stream object
     */
    public Livestream createLiveStream(LiveStreamDTO liveStreamDTO, User livestreamOwner) {
        String fileName = null;

        if (liveStreamDTO.getImage() != null && !liveStreamDTO.getImage().isEmpty()) {
            fileName = imageService.saveImage(liveStreamDTO.getImage());
        }

        return livestreamRepository.save(new Livestream(livestreamOwner, liveStreamDTO.getTitle(), liveStreamDTO.getDescription(), fileName));
    }


    public Livestream getLivestreamByUser(User livestreamOwner) {
        return livestreamRepository.findByOwner(livestreamOwner).orElse(null);
    }

    /**
     * Checks that the DTO pages are integers and larger than 0, if not sets them the page in the DTO as 1
     *
     * @param browseLiveStreamsDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void parsePages(BrowseLiveStreamsDTO browseLiveStreamsDTO) {
        int parsedPage;
        try {
            parsedPage = Integer.parseInt(browseLiveStreamsDTO.getPage());
            if (parsedPage < 1) {
                parsedPage = 1;
            }
        } catch (NumberFormatException e) {
            parsedPage = 1;
        }

        browseLiveStreamsDTO.setParsedPage(parsedPage);
    }

    /**
     * Pass in a DTO with all the required information, and it will query the database, returning posts
     *
     * @param browseLiveStreamsDTO is a DTO to carry and transfer all data between service layer, controller and view
     */
    public void getLiveStreamsPaginated(BrowseLiveStreamsDTO browseLiveStreamsDTO) {
        //Gets the pages using the query seen below
        Page<Livestream> livestreamsPage = queryRepository(browseLiveStreamsDTO);
        //For total livestreams number (the 'of' for pagination)
        int numberOfLivestreams = (int) livestreamsPage.getTotalElements();
        // If the user has input a page size that is larger than possible:
        if (browseLiveStreamsDTO.getParsedPage() > livestreamsPage.getTotalPages()) {
            browseLiveStreamsDTO.setParsedPage(livestreamsPage.getTotalPages());
            if (numberOfLivestreams != 0) {
                livestreamsPage = queryRepository(browseLiveStreamsDTO);
            }
        }
        //Setting the browseLiveStreamsDTO
        List<Livestream> feedLivestreams = livestreamsPage.getContent();
        browseLiveStreamsDTO.setTotalPages(livestreamsPage.getTotalPages());
        browseLiveStreamsDTO.setNumberOfLivestreams(numberOfLivestreams);
        browseLiveStreamsDTO.setLivestreams(feedLivestreams);
    }

    /**
     * Handles Making the query
     * @param browseLiveStreamsDTO is a DTO to carry and transfer all data between service layer, controller and view
     * @return returns a Page of Livestreams
     */
    private Page<Livestream> queryRepository(BrowseLiveStreamsDTO browseLiveStreamsDTO) {
        Page<Livestream> livestreamsPage = livestreamRepository.findCurrentLivestreams(PageRequest.of(browseLiveStreamsDTO.getParsedPage() - 1, browseLiveStreamsDTO.getPageSize()));
        return livestreamsPage != null ? livestreamsPage : Page.empty();
    }


    /**
     * Deletes a livestream by ID, returns true if successful otherwise false
     * @param id of live stream to be deleted
     * @return true if successful false otherwise
     */
    public boolean deleteLivestream(Long id, User user) {
        Optional<Livestream> livestream = livestreamRepository.findById(id);

        if(livestream.isPresent() && Objects.equals(livestream.get().getOwner().getId(), user.getId())) {
            livestreamRepository.deleteById(id);
            return true;  
        }

        return false;
    }
}
