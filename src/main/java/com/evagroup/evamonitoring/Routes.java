package com.evagroup.evamonitoring;

import com.evagroup.evamonitoring.server.ServerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class Routes {

    @Bean
    public RouterFunction<ServerResponse> serverRoutes(ServerHandler serverHandler) {
        return route(GET("servers").and(accept(MediaType.APPLICATION_JSON)), serverHandler::findAllServers)

                .and(route(POST("server").and(accept(MediaType.APPLICATION_JSON)), serverHandler::saveServer));
    }
}
