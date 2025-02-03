package org.github.seonwkim.springpekko.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    // Configures the message broker. Here, a simple in-memory message broker is enabled with a destination prefix "/topic".
    // The application destination prefix is set to "/app", meaning messages sent to destinations starting with "/app" will be routed to message-handling methods.
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app")
    }

    // Registers the STOMP endpoints. The "/ws" endpoint is registered and SockJS is enabled to support fallback options for browsers that don’t support WebSocket.
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS()
    }
}
