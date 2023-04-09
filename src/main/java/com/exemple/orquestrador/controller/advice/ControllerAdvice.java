package com.exemple.orquestrador.controller.advice;

import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.exemple.orquestrador.exception.ApiErrorException;

@RestControllerAdvice
public class ControllerAdvice{
	
	@ExceptionHandler(value = ApiErrorException.class)
    protected ResponseEntity<String> handleApiErrorException(ApiErrorException ex) {
		System.out.println(ex.getMessage());
		HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.set("Content-Type", "application/json");
	    ResponseEntity<String> responseEntity = new ResponseEntity<String>(ex.getErrorBody(), responseHeaders,ex.getStatusCode());
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
	