package cta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cta.InfoConexionSistema;

@Configuration
public class EngineConfiguration {

	@Bean
	InfoConexionSistema IOTInfoConexionSistema() {
		System.out.println("Registrado Bean InfoConexion Sistema");
		return new InfoConexionSistema();
	}
}
