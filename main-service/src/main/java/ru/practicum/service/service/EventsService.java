package ru.practicum.service.service;

import ru.practicum.service.model.ParamsSearchForAdmin;
import ru.practicum.service.model.dto.*;

import java.util.List;

public interface EventsService {
    EventFullDto postEvent(Long userId, NewEventDto newEventDto);
    List<EventShortDto> getEventForOwner(long userId, int from, int size);
    EventFullDto getFullEventForOwner(long userId, long eventId);
    EventFullDto updateEventOwner(long userId, long eventId, UpdateEventUserRequest eventUserRequest);
    List<EventFullDto> getAllEventFromAdmin(ParamsSearchForAdmin params);
    EventFullDto updateEventFromAdmin(long eventId, UpdateEventAdminRequest inputUpdate);
    List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId);
    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest inputUpdate);


}
