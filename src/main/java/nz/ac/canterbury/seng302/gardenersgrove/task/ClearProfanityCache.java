package nz.ac.canterbury.seng302.gardenersgrove.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Clear task for the profanity cache, useful to prevent spamming.
 */
@Component
public class ClearProfanityCache {

    @Autowired
    private CacheManager cacheManager;
    Logger logger = LoggerFactory.getLogger(ClearProfanityCache.class);

    /**
     * Clear the weather cache every hour
     * 60mins * 60secs * 1000ms = 1 hour
     */
    @Scheduled(fixedRate = 60*60*1000)
    public void clearCache() {
        try{
            Objects.requireNonNull(cacheManager.getCache("profanity")).clear();
            logger.info("Profanity cache cleared");
        } catch (Exception e) {
            logger.info("Failed to clear profanity cache");
        }
    }
}
