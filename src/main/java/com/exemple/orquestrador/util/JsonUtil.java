package com.exemple.orquestrador.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonUtil {

	@Autowired
	private static ObjectMapper mapper;
	
	@Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtil.mapper = objectMapper;
    }
	
	
	public static boolean isValidJson(String json) {
		if (StringUtils.isEmpty(json)) {
			return false;
		}
		try {
			mapper.readTree(json);
		} catch (JacksonException e) {
			return false;
		}
		return true;
	}
	
}

