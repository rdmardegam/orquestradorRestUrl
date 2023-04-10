package com.exemple.orquestrador.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import com.exemple.orquestrador.controller.OrquestradorController;

@Configuration
public class UrlHandlerMapping {

	@Autowired
	private SimpleUrlHandlerMapping simpleUrlHandlerMapping;

	@Bean
	public ScheduledExecutorService urlUpdater() {
	    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	    executorService.scheduleAtFixedRate(() -> {
			try {
				updateUrlMappings();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, 0, 1, TimeUnit.MINUTES);
	    return executorService;
	}
	
	
	public void updateUrlMappings() throws NoSuchMethodException {
        Map<String, Object> novoUrlMap = new HashMap<>();

        // Adicionar novas URLs ao mapa aqui
		novoUrlMap.put("/nova-rota", new HandlerMethod(new OrquestradorController(), "callApi",
				HttpServletRequest.class, Map.class, Map.class, Map.class));

		System.out.println(simpleUrlHandlerMapping.getUrlMap());
		
		simpleUrlHandlerMapping.getUrlMap().clear();
        simpleUrlHandlerMapping.setUrlMap(novoUrlMap);
        
        System.out.println(simpleUrlHandlerMapping.getUrlMap());
    }
	
	
//	private void updateUrlMappings() {
//		System.out.println("SCHELUDER ACIONADO ");
//	    List<String> newUrls = Arrays.asList("/tokenization/**");
//	    
//	    HandlerMethod handlerMethod = (HandlerMethod)simpleUrlHandlerMapping.getHandlerMap().entrySet().iterator().next().getValue();
//		
//		Map<String, Object> urlMap = new HashMap<>();
//		for (String url : newUrls) {
//		    urlMap.put(url, handlerMethod);
//		}
//		simpleUrlHandlerMapping.getUrlMap().clear();
//		simpleUrlHandlerMapping.setUrlMap(urlMap);
//	}
	
	
	
	
//	public void updateUrlMappings() {
//		List<String> paths = Arrays.asList("/tokenization/**"); // Obtém as novas rotas do serviço externo
//        Map<String, Object> urlMap = new HashMap<>();
//        for (String path : paths) {
//            urlMap.put(path, handlerMethod());
//        }
//        simpleUrlHandlerMapping.setUrlMap(urlMap); // Atualiza o mapa de rotas do SimpleUrlHandlerMapping
//        System.out.println(simpleUrlHandlerMapping.getUrlMap());
//    }
//	
//	
//	
//	private HandlerMethod handlerMethod() {
//        // Define o método que será invocado quando a rota for acessada
//        OrquestradorController orquestradorController = new OrquestradorController();
//        //Method method = ReflectionUtils.findMethod(OrquestradorController.class, "callApi", HttpServletRequest.class);
//        Method callApi = null;
//		for(Method method :ReflectionUtils.getDeclaredMethods(OrquestradorController.class)) {
//			if(method.getName().equals("callApi")) {
//				callApi = method;
//				break;
//			}
//		}
//        return new HandlerMethod(orquestradorController, callApi);
//    }
	
	
}
