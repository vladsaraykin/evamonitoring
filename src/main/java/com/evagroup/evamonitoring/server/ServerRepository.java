package com.evagroup.evamonitoring.server;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ServerRepository extends ReactiveCrudRepository<Server, Long> {
}
