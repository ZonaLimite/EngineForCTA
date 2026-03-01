package cta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;



	@Configuration
	@EnableWebSocketMessageBroker
	public class WebSocketConfigRemoteEngineConfig implements WebSocketMessageBrokerConfigurer {
		@Value("${url.allowed.crossorigin}") 
		private String crossOriginAllowed; //mapeo a la propertie que define la urls permitidas CORS
		
		@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			registry.addEndpoint("/topwebsocket")
			.setAllowedOriginPatterns(crossOriginAllowed)
			.withSockJS();
			System.out.println("RegistradoStompEndpoint");
		}
		
		
		public void configureClientInboundChannel(ChannelRegistration registration) {
			registration.taskExecutor()
    		.corePoolSize(1)   // ← Solo 1 hilo procesando en paralelo
    		.maxPoolSize(1);   
		// → procesamiento SECUENCIAL, orden garantizado de los mensajes entrantes enviados desde el cliente
		}

		@Override
		public void configureMessageBroker(MessageBrokerRegistry registry) {
			registry.enableSimpleBroker("/channel/");
			registry.setApplicationDestinationPrefixes("/app");
			System.out.println("Configurado Broker");
		}
		
	}

