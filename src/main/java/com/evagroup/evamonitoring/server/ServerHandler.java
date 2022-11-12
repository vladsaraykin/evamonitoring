package com.evagroup.evamonitoring.server;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;

@Component
@AllArgsConstructor
public class ServerHandler {

    private final ServerRepository serverRepository;

    public Mono<ServerResponse> findAllServers(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(serverRepository.findAll(), Server.class);
    }

    public Mono<ServerResponse> saveServer(ServerRequest serverRequest) {
        return serverRequest.body(toMono(Server.class))
                    .doOnNext(serverRepository::save)
                .then(ServerResponse.ok().build());
    }
}
