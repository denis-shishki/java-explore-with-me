package ru.practicum.ewm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseStatsDto {
    private String app;
    private String uri;
    private long hits;
}
