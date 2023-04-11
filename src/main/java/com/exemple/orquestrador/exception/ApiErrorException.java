package com.exemple.orquestrador.exception;

import org.springframework.http.HttpStatus;

public class ApiErrorException extends RuntimeException {

	private final HttpStatus statusCode;
	private final String errorBody;
	
	public ApiErrorException(HttpStatus httpStatus, String errorBody) {
		super("Api externa: " + httpStatus);
		this.statusCode = httpStatus;
		this.errorBody = errorBody;
	}

	public HttpStatus getStatusCode() {
		return statusCode;
	}

	public String getErrorBody() {
		return errorBody;
	}
}
