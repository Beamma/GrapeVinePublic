package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.gardenersgrove.Agora.RtcTokenBuilder2;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Livestream;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.security.AgoraTokenGenerator;
import nz.ac.canterbury.seng302.gardenersgrove.service.LivestreamService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


/**
 * Endpoint for getting an Agora token.
 */
@RestController
public class AgoraController {

    Logger logger = LoggerFactory.getLogger(AgoraController.class);

    @Autowired
    UserService userService;

    @Autowired
    LivestreamService livestreamService;

    @Autowired
    AgoraTokenGenerator agoraTokenGenerator;

    public AgoraController() {}

    /**
     * Generates a token based on if the user owns the stream.
     *
     * @param channelName The livestream id
     * @param userId The user id
     * @return the token
     */
    @GetMapping("/agora/rtcToken")
    public String getRtcToken(@RequestParam String channelName, @RequestParam int userId) {
         try {
             // Get user and livestream
             User user = userService.getCurrentUser();
             Livestream livestream = livestreamService.getLivestream(Long.valueOf(channelName));

             // Role
             RtcTokenBuilder2.Role agoraRole;

             // Check if user owns stream
             if (user.getId().equals((long) userId) && user.getId().equals(livestream.getOwner().getId())) {
                 // Host role
                 agoraRole = RtcTokenBuilder2.Role.ROLE_PUBLISHER;
             } else {
                 // Audience role
                 agoraRole = RtcTokenBuilder2.Role.ROLE_SUBSCRIBER;
             }

             return agoraTokenGenerator.generateToken(channelName, userId, agoraRole);
         } catch (Exception e) {
             return "Error generating token: " + e.getMessage();
         }
    }

    /**
     * Made so the appId isn't hard coded into the frontend
     * @return Agora AppID
     */
    @GetMapping("/agora/appId")
    public String getAgoraAppId() {
        return agoraTokenGenerator.getAppId();
    }

    /**
     * Get livestream viewer count
     * @param livestreamId The livestream id
     * @return the viewer count for the livestream
     */
    @GetMapping("/agora/{livestreamId}/viewer-count")
    public int getViewerCount(@PathVariable Long livestreamId) {
        // Agora URL
        String url = String.format("https://api.sd-rtn.com/dev/v1/channel/user/%s/%s", agoraTokenGenerator.getAppId(), livestreamId);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // Create request
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + agoraTokenGenerator.getAPICredentials());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // Send request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            // Get view count
            return objectMapper.readTree(response.getBody()).path("data").path("audience_total").asInt(0);
        } catch (Exception e) {
            logger.error("Error parsing response from Agora API", e);
        }
        return 0;

    }

    /**
     * REST endpoint for ending livestreams
     * @param livestreamId, the id of the stream to end
     * @return success or error
     */
    @DeleteMapping("/livestream/end/{livestreamId}")
    public String endStream(@PathVariable Long livestreamId) {
        User user = userService.getCurrentUser();

        if (livestreamService.deleteLivestream(livestreamId, user)) {
            return "success";
        }

        return "error";
    }
}
