package ru.practicum.ewm.controller;

import io.micrometer.core.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.RequestStatsDto;
import ru.practicum.ewm.ResponseStatsDto;
import ru.practicum.ewm.exception.ValidTimeException;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestStatsDto postStat(@RequestBody RequestStatsDto requestStatsDto) {
        return statsService.postStat(requestStatsDto);
    }

    @GetMapping("/stats")
    public List<ResponseStatsDto> getStat(@RequestParam("start") @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                          @RequestParam("end") @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                          @RequestParam(name = "uris", required = false) ArrayList<String> uris,
                                          @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        if (start == null || end == null) throw new ValidTimeException("Отсутствует дата старта или конца");
        return statsService.getStats(start, end, uris, unique);
    }
}
