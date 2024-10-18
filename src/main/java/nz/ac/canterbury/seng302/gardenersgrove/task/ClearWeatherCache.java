package nz.ac.canterbury.seng302.gardenersgrove.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Task to clear the weather cache every hour
 */
@Component
public class ClearWeatherCache {

    @Autowired
    private CacheManager cacheManager;
    Logger logger = LoggerFactory.getLogger(ClearWeatherCache.class);

    /**
     * Clear the weather cache every hour
     * 60mins * 60secs * 1000ms = 1 hour
     */
    @Scheduled(fixedRate = 60*60*1000)
    public void clearCache() {
        try{
            Objects.requireNonNull(cacheManager.getCache("weather")).clear();
            Objects.requireNonNull(cacheManager.getCache("weatherHistory")).clear();
            logger.info("Weather cache cleared");
        } catch (Exception e) {
            logger.info("Failed to clear weather cache");
        }
    }
}
