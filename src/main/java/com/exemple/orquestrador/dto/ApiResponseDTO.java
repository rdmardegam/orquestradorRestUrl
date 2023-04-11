package com.exemple.orquestrador.dto;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class ApiResponseDTO {

	private String body;
    private HttpStatus status;
    private HttpHeaders headers;
   
    public ApiResponseDTO(String body, HttpStatus status, HttpHeaders headers) {
        this.body = body;
        this.status = status;
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

	public void setBody(String body) {
		this.body = body;
	}
    
    
}
