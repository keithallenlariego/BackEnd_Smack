package com.example.securewebchatroom.config;

import com.example.securewebchatroom.security.JwtService;
import com.example.securewebchatroom.security.StompAuthInterceptor;
import com.example.securewebchatroom.service.MembershipService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Live chat: the browser connects to /ws and subscribes to /topic/rooms/{id}.
// Messages are SENT with the normal REST endpoint; the server pushes them to subscribers.
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final MembershipService membershipService;
    private final String allowedOrigin;

    public WebSocketConfig(JwtService jwtService, MembershipService membershipService,
                           @Value("${app.cors.allowed-origin}") String allowedOrigin) {
        this.jwtService = jwtService;
        this.membershipService = membershipService;
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigin);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new StompAuthInterceptor(jwtService, membershipService));
    }
}
