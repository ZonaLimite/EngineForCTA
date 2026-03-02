package cta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfigRemoteEngineConfig implements WebSocketMessageBrokerConfigurer {
	@Value("${url.allowed.crossorigin}")
	private String crossOriginAllowed; // mapeo a la propertie que define la urls permitidas CORS

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/topwebsocket")
				.setAllowedOriginPatterns(crossOriginAllowed)
				.withSockJS();
		System.out.println("RegistradoStompEndpoint");
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/channel/");// Canal de envio
		registry.setApplicationDestinationPrefixes("/app");// Canal de recepcion
		System.out.println("Configurado Broker");
	}

}
