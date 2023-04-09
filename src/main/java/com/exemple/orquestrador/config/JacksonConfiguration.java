package com.exemple.orquestrador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfiguration {

    
    @Primary
    @Bean
    public ObjectMapper objectMapper() {
    	ObjectMapper objectMapper =  new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		//objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCaseStrategy.SNAKE_CASE);
		//objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
		//.serializationInclusion(JsonInclude.Include.NON_NULL)
        //.serializationInclusion(JsonInclude.Include.NON_EMPTY)
		return objectMapper;
    } 
}