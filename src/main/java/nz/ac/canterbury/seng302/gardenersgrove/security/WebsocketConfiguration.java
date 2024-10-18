package nz.ac.canterbury.seng302.gardenersgrove.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import nz.ac.canterbury.seng302.gardenersgrove.utility.EnvironmentUtils;

/**
 * WebSocket configuration class that enables and configures WebSocket messaging
 * using the STOMP protocol.
 * */

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker with a simple in-memory broker for broadcasting messages
     * and sets the application destination prefix for messaging routes.
     * @param config the {@link MessageBrokerRegistry} used to configure the message broker
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    /**
     * Registers the STOMP endpoint that clients will use to connect to the WebSocket server.
     * The endpoint is "/grapevine".
     * @param registry the {@link StompEndpointRegistry} used to register WebSocket endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/grapevine/").setAllowedOrigins("*");
    }

}