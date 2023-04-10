package com.exemple.orquestrador;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import com.exemple.orquestrador.controller.OrquestradorController;

@SpringBootApplication
@EnableWebMvc
@EnableScheduling
public class OrquestradoraApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrquestradoraApplication.class, args);
	}

	
	@Autowired
    OrquestradorController orquestradorController;

	@Value("${orquestador.urls}")
    List<String> paths;
	
	@Bean
	public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
		System.out.println("Current time is :: " + LocalDateTime.now());
		SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
		Map<String, Object> map = new HashMap<>();
		
		Method callApi = null;
		for(Method method :ReflectionUtils.getDeclaredMethods(OrquestradorController.class)) {
			if(method.getName().equals("callApi")) {
				callApi = method;
				break;
			}
		}
		final HandlerMethod handlerMethod = new HandlerMethod(orquestradorController, callApi);
		for (String path : paths) {
			map.put(path, handlerMethod);
		}
		
		simpleUrlHandlerMapping.setUrlMap(map);
		simpleUrlHandlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);

		System.out.println(simpleUrlHandlerMapping.getUrlMap()); 
		
		return simpleUrlHandlerMapping;
	}
	
//	@Scheduled(cron = "0 */1 * ? * *")
//	public void runEvey5Minutes() {
//		System.out.println("Current time is :: " + LocalDateTime.now());
//	}
}