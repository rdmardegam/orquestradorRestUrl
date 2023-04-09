package com.exemple.orquestrador.service;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.exemple.orquestrador.dto.OfuscadorDTO;
import com.exemple.orquestrador.enums.EncodingTypeEnum;
import com.exemple.orquestrador.exception.ApiErrorException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.timeout.ReadTimeoutException;

@Service
public class OrquestradorService {

	private WebClient webClient;
	private ObjectMapper mapper;
	
	@Value("${fields.encode}")
	private List<String> fildsToEncodeDecode;
	
	@Autowired
    public OrquestradorService(ObjectMapper mapper, WebClient webClient) {
		this.webClient = webClient;
        this.mapper = mapper;
    }
		
    public String callApi(String apiUrl, HttpMethod httpMethod, Map<String, Object> queryParams, Map<String, Object> paths,
    		Map<String, Object> body, Map<String, Object> headers, boolean externalCall) {

    	// Aplica Encode em body, query e path se necessario
    	Set<OfuscadorDTO> listOfuscador = this.decodeAndGetOfuscador(body, queryParams, paths, externalCall);
    	
    	// Aplica paths na url e obtem a url de destino
    	String urlToCall = this.prepareUrl(apiUrl,paths);

    	// Prepara os Headers
    	HttpHeaders headersToSend = this.prepareHeaders(headers); 
        
    	// Perar os queryParametros a serem enviados
    	MultiValueMap<String, String> queryParamsToSend = this.prepareQueryParams(queryParams); 
    	
    	// Executa api
    	String jsonRetorno = this.executeApi(httpMethod,urlToCall,queryParamsToSend,headersToSend,body);
    	
    	// Aplica encode no body de retorno
    	return this.encode(jsonRetorno, listOfuscador, externalCall);
    } 
	

	private String executeApi(HttpMethod httpMethod, String urlToCall, MultiValueMap<String, String> queryParamsToSend,
			HttpHeaders headersToSend, Map<String, Object> body) {

		return webClient.method(httpMethod)
				.uri(uriBuilder -> uriBuilder.path(urlToCall).queryParams(queryParamsToSend).build())
				.headers(httpHeaders -> httpHeaders.addAll(headersToSend))
				.body(body != null ? BodyInserters.fromValue(body) : null)
				.retrieve()
				.onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class).flatMap(errorBody -> {
					throw new ApiErrorException(response.statusCode(), errorBody);
				}))
				.bodyToMono(String.class)
				.doOnError(throwable ->handleApiError(throwable))
				.block();
	}
	
	
	private void handleApiError(Throwable throwable) {
		if (throwable instanceof ApiErrorException) {
			throw (ApiErrorException) throwable;
		}
		if (throwable instanceof WebClientRequestException
				&& throwable.getCause() instanceof ReadTimeoutException) {
			// Tratar o erro de timeout
			throw new ApiErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
					"ERRO INTERNO WEBCLIENT - TIMEOUT");
		} else {
			// Tratar outros erros de comunicação
			throw new ApiErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO INTERNO WEBCLIENT");
		}
	}

	private HttpHeaders prepareHeaders(Map<String, Object> headers) {
		 HttpHeaders headersSend = new HttpHeaders();
		 headers.forEach((key, value) -> headersSend.add(key, value.toString()));
		 return headersSend;	
	}

	private String prepareUrl(String apiUrl, Map<String, Object> pathParams) {
	    StringBuilder urlBuilder = new StringBuilder(apiUrl);
	    
	    // Replace path parameters in the URL
	    for (Map.Entry<String, Object> entry : pathParams.entrySet()) {
	        urlBuilder.replace(urlBuilder.indexOf("{" + entry.getKey() + "}"), urlBuilder.indexOf("{" + entry.getKey() + "}") + entry.getKey().length() + 2, entry.getValue().toString());
	    }
	    
	    return urlBuilder
	            .toString()
	            /*.replaceFirst("api", "api2")
	            .replaceFirst("tokenization", "tokenization2")*/;
	}

	private MultiValueMap<String, String> prepareQueryParams(Map<String, Object> queryParams) {
		MultiValueMap<String, String> multiValueQueryParam = new LinkedMultiValueMap<>();
		queryParams.forEach((key, value) -> multiValueQueryParam.add(key, value.toString()));
		return multiValueQueryParam;
	}
	
	private Set<OfuscadorDTO> decodeAndGetOfuscador(Map<String, Object> body, Map<String, Object> queryParams, Map<String, Object> paths, boolean externalCall) {
		Set<OfuscadorDTO> listOfuscador = null;
		if(externalCall) {
			// Os mapas que sera usados para encontrar os valores
			List<Map<String, Object>> listMapToOfusc = Arrays.asList(body,queryParams,paths);
			
			// Procura os valores a serem abertos ou seja Decode
			listOfuscador = this.findValuesToEncodeOrDecode(listMapToOfusc, fildsToEncodeDecode, EncodingTypeEnum.DECODE);
			
			// Aplica da lista de ofuscador... procura e monta o objeto OfuscadorDTO
			this.ofuscBuild(listOfuscador,EncodingTypeEnum.DECODE);
			
			// Decode os campos
			applyEncodeOrDecodeByOfuscador(listMapToOfusc, listOfuscador, EncodingTypeEnum.DECODE);
			System.out.println(listOfuscador);
		}
		
		
		return listOfuscador;	
	}

	private Set<OfuscadorDTO> findValuesToEncodeOrDecode(List<Map<String, Object>> listMap, List<String> attributeNames, EncodingTypeEnum encodingType) {
		Set<OfuscadorDTO> listOfuscador = new HashSet<OfuscadorDTO>();
	    for (Map<String, Object> map : listMap) {
	        findValuesInMap(map, attributeNames, listOfuscador,encodingType);
	    }
	    return listOfuscador;
	}

	private void findValuesInMap(Map<String, ?> map, List<String> attributeNames, Set<OfuscadorDTO> listOfuscador, EncodingTypeEnum encodingType) {
	    if (map != null) {
	        map.entrySet().forEach(entry -> {
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            
	            if (value instanceof Map) {
	                // Recursivamente procurar no mapa interno
	                findValuesInMap((Map<String, ?>) value, attributeNames, listOfuscador, encodingType);
	            } else if (value instanceof List) {
	                // Recursivamente procurar em cada elemento da lista
	                List<?> listValue = (List<?>) value;
	                listValue.forEach(listElement -> {
	                    if (listElement instanceof Map) {
	                        findValuesInMap((Map<String, ?>) listElement, attributeNames, listOfuscador, encodingType);
	                    }
	                });
	            } else if (map.containsKey(key) && attributeNames.contains(key)) {
	                // Encontrou o ID, adicionar os valores desejados ao mapa resultante
	                OfuscadorDTO ofuscador = new OfuscadorDTO();
	                ofuscador.setField(key);
	                if (encodingType == EncodingTypeEnum.ENCODE) {
	                    ofuscador.setValueDecoded(value.toString());
	                } else {
	                    ofuscador.setValueEncod(value.toString());
	                }
	                listOfuscador.add(ofuscador);
	            }
	        });
	    }
	}
	
	private void ofuscBuild(Set<OfuscadorDTO> listOfuscador,EncodingTypeEnum encodingType) {
		Base64.Encoder encoder = Base64.getEncoder();
	    Base64.Decoder decoder = Base64.getDecoder();
	    
	    // AQUI DEVE SER CHAMADO O SERVIÇO DE DECODE OU ENCODE APENAS PARA OS VALORES QUE NÃO ESTÃO PREVIAMENTE MAPEADOS
	    listOfuscador.forEach(e -> {
	        if (encodingType == EncodingTypeEnum.ENCODE && e.getValueEncod() == null) {
	            String encodedValue = encoder.encodeToString(((String) e.getValueDecoded()).getBytes());
	            e.setValueEncod(encodedValue);
	        } else if (encodingType == EncodingTypeEnum.DECODE && e.getValueDecoded() == null) {
	            String decodedValue = new String(decoder.decode(((String) e.getValueEncod()).getBytes()));
	            e.setValueDecoded(decodedValue);
	        }
	    });
	}
	
	private void applyEncodeOrDecodeByOfuscador(List<Map<String, Object>> listMap, Set<OfuscadorDTO> listOfuscador, EncodingTypeEnum encodingType) {
	    for (Map<String, Object> map : listMap) {
	        applyEncodeOrDecodeByOfuscadorRecursive(map, listOfuscador,encodingType);
	    }
	}

	private void applyEncodeOrDecodeByOfuscadorRecursive(Map<String, Object> map, Set<OfuscadorDTO> ofuscadorList, EncodingTypeEnum encodingType) {
	    if (map != null && !CollectionUtils.isEmpty(ofuscadorList)) {
	        map.forEach((key, value) -> {
	            if (value instanceof Map) {
	                applyEncodeOrDecodeByOfuscadorRecursive((Map<String, Object>) value, ofuscadorList, encodingType);
	            } else if (value instanceof List) {
	                ((List<?>) value).forEach(obj -> {
	                    if (obj instanceof Map) {
	                        applyEncodeOrDecodeByOfuscadorRecursive((Map<String, Object>) obj, ofuscadorList, encodingType);
	                    }
	                });
	            } else {
	                ofuscadorList.forEach(ofuscadorDTO -> {
	                    if (ofuscadorDTO.getField().equals(key)) {
	                        if (encodingType == EncodingTypeEnum.ENCODE && ofuscadorDTO.getValueDecoded().equals(value.toString())) {
	                            String valueEncod = ofuscadorDTO.getValueEncod().toString();
	                            map.put(key, valueEncod);
	                        } else if (encodingType == EncodingTypeEnum.DECODE && ofuscadorDTO.getValueEncod().equals(value.toString())) {
	                            String valueDecode = ofuscadorDTO.getValueDecoded().toString();
	                            map.put(key, valueDecode);
	                        }
	                    }
	                });
	            }
	        });
	    }
	}
	
	private String encode(String json, Set<OfuscadorDTO> listOfuscador, boolean externalCall) {
	    if (externalCall && isValidJson(json)) {
	        try {
	            Map<String, Object> mapBody = mapper.readValue(json, HashMap.class);
	            
	            Set<OfuscadorDTO> listOfuscadorDecoded = this.findValuesToEncodeOrDecode(Arrays.asList(mapBody), fildsToEncodeDecode, EncodingTypeEnum.ENCODE);
	            
	            listOfuscador = joinOfuscadorList(listOfuscador, listOfuscadorDecoded);
	            
	            ofuscBuild(listOfuscador, EncodingTypeEnum.ENCODE);
	            
	            applyEncodeOrDecodeByOfuscador(Arrays.asList(mapBody), listOfuscador, EncodingTypeEnum.ENCODE);
	            
	            System.out.println(listOfuscador);
	            
	            return mapper.writeValueAsString(mapBody);
	        } catch (JsonProcessingException e) {
	            // Caso ocorra algum erro
	            throw new RuntimeException("Erro ao realizar encoding do JSON.");
	        }
	    }
	    // Retorno padrão, caso a condição acima não seja atendida
	    return json;
	}
	
	private Set<OfuscadorDTO> joinOfuscadorList(Set<OfuscadorDTO> listOfuscador, Set<OfuscadorDTO> listOfuscadorDecoded) {
	    // Merge as 2 listas
	    if (StringUtils.isEmpty(listOfuscador)) {
	        return listOfuscadorDecoded;
	    } else if (StringUtils.isEmpty(listOfuscadorDecoded)) {
	        return listOfuscador;
	    } else {
	        Set<OfuscadorDTO> mergedList = new HashSet<>(listOfuscador);
	        for (OfuscadorDTO ofuscador : listOfuscadorDecoded) {
	            boolean alreadyExists = false;
	            for (OfuscadorDTO existingOfuscador : mergedList) {
	                if (existingOfuscador.getField().equals(ofuscador.getField()) &&
	                    existingOfuscador.getValueDecoded().equals(ofuscador.getValueDecoded())) {
	                    alreadyExists = true;
	                    break;
	                }
	            }
	            if (!alreadyExists) {
	                mergedList.add(ofuscador);
	            }
	        }
	        return mergedList;
	    }
	}
	
	public boolean isValidJson(String json) {
	    try {
	        mapper.readTree(json);
	    } catch (JacksonException e) {
	        return false;
	    }
	    return true;
	}
}