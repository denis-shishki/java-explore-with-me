package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.RequestStatsDto;
import ru.practicum.ewm.ResponseStatsDto;
import ru.practicum.ewm.exception.ValidTimeException;
import ru.practicum.ewm.model.Mapper;
import ru.practicum.ewm.model.ResponseStat;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public RequestStatsDto postStat(RequestStatsDto requestStatsDto) {
        Stat stat = Mapper.toStatRequest(requestStatsDto);
        Stat stat1 = statsRepository.save(stat);

        return Mapper.toRequestStatDto(stat1);
    }

    @Override
    public List<ResponseStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        checkValidTime(start, end);

        List<ResponseStat> stats;

        if (unique) {
            stats = statsRepository.getStatByUrisAndTimeIsUnique(uris, start, end);
        } else {
            stats = statsRepository.getStatByUrisAndTime(uris, start, end);
        }

        return stats.stream().map(Mapper::toResponseStatsDto).collect(Collectors.toList());
    }

    private void checkValidTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.equals(start)) {
            throw new ValidTimeException("Время начала не может быть позже или равно времени окончания мероприятия");
        }
    }
}
