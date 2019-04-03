package org.weishao.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.weishao.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
	
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}