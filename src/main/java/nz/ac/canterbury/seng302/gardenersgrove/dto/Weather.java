package nz.ac.canterbury.seng302.gardenersgrove.dto;

/**
 * The Weather class represents weather information for a specific day.
 */
public class Weather {

    private String icon;
    private String day;
    private String date;
    private String description;
    private float tempC;
    private float humidity;
    private float maxWind;

    /**
     * Constructor.
     */
    public Weather() {}

    /**
     * Gets the icon representing the weather condition.
     * @return the weather condition icon.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the icon representing the weather condition.
     * @param icon the weather condition icon.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Gets the day of the week for the weather data.
     * @return the day of the week.
     */
    public String getDay() {
        return day;
    }

    /**
     * Sets the day of the week for the weather data.
     * @param day the day of the week.
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * Gets the date of the weather data.
     * @return the date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date of the weather data.
     * @param date the date.
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets the description of the weather condition.
     * @return the weather condition description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the weather description.
     * @param description the weather description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the temperature in degrees celsius.
     * @return the temperature in degrees celsius.
     */
    public float getTempC() {
        return tempC;
    }

    /**
     * Sets the temperature in degrees celsius.
     * @param tempC the temperature in degrees celsius.
     */
    public void setTempC(float tempC) {
        this.tempC = tempC;
    }

    /**
     * Gets the humidity percentage.
     * @return the humidity percentage.
     */
    public float getHumidity() {
        return humidity;
    }

    /**
     * Sets the humidity percentage.
     * @param humidity the humidity percentage.
     */
    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    /**
     * Gets the maximum wind speed.
     * @return the maximum wind speed.
     */
    public float getMaxWind() {
        return maxWind;
    }

    /**
     * Sets the maximum wind speed.
     * @param maxWind the maximum wind speed.
     */
    public void setMaxWind(float maxWind) {
        this.maxWind = maxWind;
    }
}