package com.exemple.orquestrador.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {


	@Bean
	public WebClient webClient(ObjectMapper mapper) {
		return WebClient.builder().baseUrl("http://localhost:3001")
				.clientConnector(new ReactorClientHttpConnector(	
				HttpClient.create().responseTimeout(Duration.ofSeconds(1)))).build();
	}
	
}
