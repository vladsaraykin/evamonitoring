package com.evagroup.evamonitoring.healthcheck;

import com.evagroup.evamonitoring.NotificationService;
import com.evagroup.evamonitoring.server.ServerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@AllArgsConstructor
public class HealthCheckManager {

    private final NotificationService notificationService;
    private final ServerRepository serverRepository;
    private final WebClient webClient;


    @Scheduled(cron = "0 */5 * * * *", zone = "Europe/Moscow")
    public void healthCheck() {
        log.info("starting health check");
        serverRepository.findAll().doOnNext(server -> {
             webClient.get().uri(server.url()).exchangeToMono(response -> {
                 if (response.statusCode().isError()) {
                     return notificationService.sendNotification(String.format("Url %s is unavailable(response)", server.url()));
                 }
                 return Mono.empty();
             }).onErrorResume(ex ->
                  notificationService.sendNotification(String.format("Url %s is unavailable(connection)", server.url()))
             ).subscribe();
        }).subscribe();
    }

}
