package com.evagroup.evamonitoring;

import reactor.core.publisher.Mono;

public interface NotificationService {

    Mono<String> sendNotification(String msg);
}
