package com.exemple.orquestrador.controller.advice;

import java.util.HashMap;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.exemple.orquestrador.exception.ApiErrorException;
import com.exemple.orquestrador.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestControllerAdvice
public class ControllerAdvice{
	
	@Autowired
	public ObjectMapper mapper;
	
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>");
	
	@ExceptionHandler(value = ApiErrorException.class)
    protected ResponseEntity<String> handleApiErrorException(ApiErrorException ex) {
		System.out.println(ex.getMessage());
		HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.set("Content-Type", "application/json");
	    
	    String jsonRetoro = ex.getErrorBody();
	    
	    if(JsonUtil.isValidJson(jsonRetoro)) {
	    	try {
	   			JsonNode rootJson = mapper.readTree(jsonRetoro);
	   			if (rootJson.isObject()) {
	   				// Converte o JsonNode para um ObjectNode para permitir a remoção de campos
	   	            ObjectNode objectNode = (ObjectNode) rootJson;

	   	            // Remove o campo desejado
	   	            objectNode.remove("trace");
	   	            objectNode.remove("path");
	   	            
	   	            // Converte o ObjectNode de volta para um JSON
	   	            jsonRetoro = mapper.writeValueAsString(objectNode);	
	   			}
	   		}catch (Exception e) {
				e.printStackTrace();
			}	
	    // Caso conteudo HTML, pode representar um erro nao retornado pelo lado externo
	    } else if(HTML_PATTERN.matcher(jsonRetoro).find()) {
	    	jsonRetoro = ex.getMessage();
	    }
   			
	    
	    ResponseEntity<String> responseEntity = new ResponseEntity<String>(jsonRetoro, responseHeaders,ex.getStatusCode());
		return responseEntity;
    }
	
	@ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected HashMap<String, String> handleApiErrorException(NoHandlerFoundException ex, WebRequest request) {
		 	HashMap<String, String> response = new HashMap<>();
	        response.put("status", "fail");
	        response.put("message", ex.getLocalizedMessage());
	        return response;
    }
	
	@ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<String> runtimeExceptionException(RuntimeException ex) {
		ex.printStackTrace();
		System.out.println(ex.getMessage());
		HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.set("Content-Type", "application/json");
	    ResponseEntity<String> responseEntity = new ResponseEntity<String>(ex.toString(), responseHeaders,HttpStatus.INTERNAL_SERVER_ERROR);
		return responseEntity;
    }
	
	
	
//	@ExceptionHandler(value = RuntimeException.class)
//    protected ResponseEntity<String> handleApiErrorException(RuntimeException ex) {
//		System.out.println(ex.getMessage());
//		
//	    ResponseEntity<String> responseEntity = new ResponseEntity<String>("ERRO API ORQUESTRADORA",HttpStatus.INTERNAL_SERVER_ERROR);
//		return responseEntity;
//    }
}
	