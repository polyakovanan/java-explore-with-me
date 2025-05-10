package ru.practicum.server.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Stats {
    String app;

    String uri;

    Long hits;
}
