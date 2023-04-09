package com.exemple.orquestrador.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.exemple.orquestrador.service.OrquestradorService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class OrquestradorController {

	@Autowired
	ObjectMapper mapper;

	@Autowired
	OrquestradorService orquestradorService;
	
	// Os paths sao carregados automaticamente via  "orquestador.urls" no application.propeties
	public ResponseEntity<String> callApi(HttpServletRequest request, 
			@RequestBody(required = false) Map<String, Object> requestBody /*String requestBody*/,
			@RequestParam Map<String, Object> requestQueryParams, 
			@RequestHeader Map<String, Object> requestHeaders)  {
	    
		// Metodo requisitado que será a base para o metodo seguinte
	    String requestUrl = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
	    
	    // Recuperado o tipo do metodo chamado GET, POST, PUT, PATCH ...
	    HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
	    
	    // Extrai os path parametros
	    Map<String, Object> requestPaths = this.getPaths(request);
	    
	    boolean isCallExternal = requestHeaders.get("x-external") != null && "true".equals(requestHeaders.get("x-external")) ? true : false;  
	    
	    // Chama url Externa
	    String jsonValue = orquestradorService.callApi(requestUrl, httpMethod, requestQueryParams, requestPaths, requestBody, requestHeaders, isCallExternal);
	    
	    // Monta headerResposta
	    HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.set("Content-Type", "application/json");
	    ResponseEntity<String> responseEntity = new ResponseEntity<String>(jsonValue, responseHeaders, HttpStatus.OK);
	    
	    return responseEntity;
	}

	private Map<String, Object> getPaths(HttpServletRequest request) {
	    Map<String, Object> requestPaths = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
	    Map<String, Object> path = new HashMap<String, Object>();
	    if (requestPaths != null) {
	        requestPaths.forEach((key, value) -> path.put(key, value != null ? value.toString() : null));
	    }
	    return path;
	}
}
