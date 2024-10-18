package nz.ac.canterbury.seng302.gardenersgrove.dto;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;

import java.util.List;

/**
 * DTO for carrying information about the browse livestreams route
 */
public class BrowseLiveStreamsDTO {
    private String page;
    private int parsedPage;
    private static final int PAGE_SIZE = 10;
    private int totalPages;
    private int numberOfLivestreams;
    private List<Livestream> livestreams;

    public BrowseLiveStreamsDTO(String page) {
        this.page = page;
    }

    /**
     * gets the page the user is requesting
     * @return the page the user is requesting
     */
    public String getPage() {
            return this.page;
    }

    /**
     * Set the page the user is requesting
     * @param page the user is requesting
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Sets a valid parsed page
     * @param page the parsed page
     */
    public void setParsedPage(int page) {
        this.parsedPage = page;
    }

    /**
     * Gets a valid parsed page
     * @return a valid parsed page
     */
    public int getParsedPage() {
        return this.parsedPage;
    }

    /**
     * For getting the amount of items on a page
     * @return the amount of livestreams per page on livestream browse page
     */
    public int getPageSize() {
        return PAGE_SIZE;
    }

    /**
     * Get the total amount of pages needed to display all current livestreams
     * @return the total amount of pages needed to display all current livestreams
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Get the amount of current occurring livestreams
     * @return the amount of current occurring livestreams
     */
    public int getNumberOfLivestreams() {
        return numberOfLivestreams;
    }

    /**
     * Get the live streams to display on the given page
     * @return a list of livestreams based of page number and size
     */
    public List<Livestream> getLivestreams() {
        return livestreams;
    }

    /**
     * Set the amount of pages it takes to display all the current livestreams
     * @param totalPages amount of pages it takes to display all the current livestreams
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Total number of livestreams currently occurring
     * @param numberOfLivestreams currently occurring
     */
    public void setNumberOfLivestreams(int numberOfLivestreams) {
        this.numberOfLivestreams = numberOfLivestreams;
    }

    /**
     * Set the list of livestreams based of page number and size
     * @param livestreams a list based of page number and size
     */
    public void setLivestreams(List<Livestream> livestreams) {
        this.livestreams = livestreams;
    }
}
