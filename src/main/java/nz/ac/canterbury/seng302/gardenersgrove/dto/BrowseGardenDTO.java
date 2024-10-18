package nz.ac.canterbury.seng302.gardenersgrove.dto;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;

import java.util.List;

/**
 * Data transfer object for filtering parameters and data of the browse public gardens
 */
public class BrowseGardenDTO {
    private String tagsString;
    private List<String> tags;
    private String search;
    private String page;
    private int pageSize = 9;

    private int parsedPage;

    private List<Garden> gardens;

    private int searchSize;

    private String searchError;
    private int totalPages;

    public BrowseGardenDTO(String tagsString, String search, String page) {
        this.tagsString = tagsString;
        this.search = search;
        this.page = page;
    }

    /**
     * Gets the string of Tags
     * @return string of Tags
     */
    public String getTagsString() {
        return this.tagsString;
    }

    /**
     * Set the List of tags
     * @param tags a List of tags that are strings
     */
    public void setTags(List<String> tags){
        this.tags = tags;
    }

    /**
     * Gets the current page
     * @return the current page
     */
    public String getPage(){
        return this.page;
    }

    /**
     * Sets the parsedPage
     * @param parsedPage a parsedPage that can be trusted to be true
     */
    public void setParsedPage(int parsedPage){
        this.parsedPage = parsedPage;
    }

    /**
     * @return The string the user searched for
     */
    public String getSearch(){
        return this.search;
    }

    /**
     * @return a parsedPage that can be trusted to be true
     */
    public int getParsedPage(){
        return this.parsedPage;
    }

    /**
     * @return How many gardens each page contains
     */
    public int getPageSize(){
        return this.pageSize;
    }

    /**
     * @return A list of all the tags a user filtered by
     */
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * @param pageSize How many gardens per page
     */
    public void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }

    /**
     * @param gardens A list of garden objects
     */
    public void setGardens(List<Garden> gardens) {
        this.gardens = gardens;
    }

    /**
     * @return A list of filtered gardens
     */
    public List<Garden> getGardens() {
        return this.gardens;
    }

    /**
     * @param searchSize How many gardens were returned from the search
     */
    public void setSearchSize(int searchSize){
        this.searchSize = searchSize;
    }

    /**
     * @return How many gardens were returned from the search
     */
    public int getSearchSize() {
        return this.searchSize;
    }

    /**
     * Set a search error, if a search error occurred when filtering gardens
     * @param error The message you want to display to the user
     */
    public void setSearchError(String error){
        this.searchError = error;
    }

    /**
     * Get an error message for filtering gardens
     * @return and informative error message
     */
    public String getSearchError(){
        return this.searchError;
    }

    /**
     * @param pages Total amount of pages required to display all gardens
     */
    public void setTotalPages(int pages){
        this.totalPages = pages;
    }

    /**
     * @return Total amount of pages required to display all gardens
     */
    public int getTotalPages() {
        return this.totalPages;
    }

    /**
     * @return The tag list, in the form of a string, with each element connected by a ','
     */
    public String getTagsToString() {
        return String.join(",", this.getTags());
    }
}
