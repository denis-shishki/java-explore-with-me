package ru.practicum.service.model.mapper;

import ru.practicum.service.model.Event;
import ru.practicum.service.model.Request;
import ru.practicum.service.model.dto.EventFullDto;
import ru.practicum.service.model.dto.EventShortDto;
import ru.practicum.service.model.dto.NewEventDto;
import ru.practicum.service.model.dto.ParticipationRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .created(request.getCreated())
                .requester(request.getId())
                .status(request.getStatus())
                .build();
    }

    public Request toRequest(ParticipationRequestDto participationRequestDto) {
        return Request.builder()
                .id(participationRequestDto.getId())
                .event(null)
                .created(participationRequestDto.getCreated())
                .requester(null)
                .status(participationRequestDto.getStatus())
                .build();
    }
}
