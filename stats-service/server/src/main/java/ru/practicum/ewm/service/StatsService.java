package ru.practicum.ewm.service;

import ru.practicum.ewm.RequestStatsDto;
import ru.practicum.ewm.ResponseStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    RequestStatsDto postStat(RequestStatsDto requestStatsDto);

    List<ResponseStatsDto> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique);

}
