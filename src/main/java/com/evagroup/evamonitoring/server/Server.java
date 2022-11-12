package com.evagroup.evamonitoring.server;

import org.springframework.data.annotation.Id;

public record Server(@Id Long id, String url) {
    public Server(String url) {
        this(null, url);
    }

    public Server(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
