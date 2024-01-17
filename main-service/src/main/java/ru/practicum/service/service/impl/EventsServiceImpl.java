package ru.practicum.service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.client.Client;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.exception.NotFoundException;
import ru.practicum.service.exception.ValidationException;
import ru.practicum.service.model.*;
import ru.practicum.service.model.dto.*;
import ru.practicum.service.model.enums.EventAdminState;
import ru.practicum.service.model.enums.EventUserState;
import ru.practicum.service.model.enums.RequestStatus;
import ru.practicum.service.model.enums.State;
import ru.practicum.service.model.mapper.EventMapper;
import ru.practicum.service.model.mapper.LocationMapper;
import ru.practicum.service.model.mapper.RequestMapper;
import ru.practicum.service.paginator.Paginator;
import ru.practicum.service.repository.EventRepository;
import ru.practicum.service.repository.LocationRepository;
import ru.practicum.service.repository.RequestsRepository;
import ru.practicum.service.service.CategoryService;
import ru.practicum.service.service.EventsService;
import ru.practicum.service.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final RequestsRepository requestsRepository;
    private final Client client;
    private final ObjectMapper objectMapper;


    @Value("${server.application.name:ewm-service}")
    private String applicationName;


    @Override
    public EventFullDto postEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime createOn = LocalDateTime.now();
        User user = userService.checkExistUser(userId);
        Category category = categoryService.checkExistCategory(newEventDto.getCategory());
        checkValidDataTimeForUser(newEventDto.getEventDate());

        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0);
        } else if (newEventDto.getParticipantLimit() < 0) {
            throw new ValidationException("Значение ограничения участников не может быть отрицательным");
        }

        Event event = EventMapper.toEvent(newEventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventStatus(State.PENDING);
        event.setCreatedDate(createOn);

        if (newEventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));
            event.setLocation(location);
        }

        Event eventSaved = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventSaved);
        eventFullDto.setViews(0L);
        eventFullDto.setConfirmedRequests(0);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEventForOwner(long userId, int from, int size) {
        userService.checkExistUser(userId);
        Pageable pageable = Paginator.getPageable(from, size);


        Page<Event> events = eventRepository.findAllByInitiatorIdIs(userId, pageable);

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getFullEventForOwner(long userId, long eventId) {
        userService.checkExistUser(userId);
        Optional<Event> event = eventRepository.findFirstByIdAndInitiatorIdIs(eventId, userId);

        if (event.isEmpty()) {
            throw new NotFoundException("Event with id= " + eventId + " was not found");
        }
        return EventMapper.toEventFullDto(event.get());
    }

    @Override
    public EventFullDto updateEventOwner(long userId, long eventId, UpdateEventUserRequest updateEventRequest) {
        userService.checkExistUser(userId);
        Event oldEvent = checkEvenByInitiatorAndEventId(userId, eventId);

        if (oldEvent.getEventStatus().equals(State.PUBLISHED)) {
            throw new DataIntegrityViolationException("Нельзя изменить уже опубликованное событие");
        }

        Event eventUpdate = updateEventFields(oldEvent, updateEventRequest);

        LocalDateTime newDate = updateEventRequest.getEventDate();
        if (newDate != null) { //может этот блок поместить в мердж
            checkValidDataTimeForUser(newDate);
            eventUpdate.setEventDate(newDate);
        }

        EventUserState stateAction = updateEventRequest.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    eventUpdate.setEventStatus(State.PENDING);
                    break;
                case CANCEL_REVIEW:
                    eventUpdate.setEventStatus(State.CANCELED);
                    break;
            }
        }
        return EventMapper.toEventFullDto(eventRepository.save(eventUpdate));
    }

    @Override
    public List<EventFullDto> getAllEventFromAdmin(ParamsSearchForAdmin params) {
        Pageable pageable = Paginator.getPageable(params.getFrom(), params.getSize());
        Specification<Event> specification = Specification.where(null);

        List<Long> users = params.getUsers();
        List<String> states = params.getStates();
        List<Long> categories = params.getCategories();
        LocalDateTime rangeEnd = params.getRangeEnd();
        LocalDateTime rangeStart = params.getRangeStart();

        if (users != null && !users.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("eventStatus").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        Page<Event> events = eventRepository.findAll(specification, pageable);

        List<EventFullDto> result = events.getContent()
                .stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());

        Map<Long, List<Request>> confirmedRequestsCountMap = getConfirmedRequestsCount(events.toList());
        for (EventFullDto event : result) {
            List<Request> requests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }
        return result;
    }

    @Override
    public EventFullDto updateEventFromAdmin(long eventId, UpdateEventAdminRequest inputEvent) {
        Event eventOld = checkExistEvent(eventId);

        if (eventOld.getEventStatus().equals(State.PUBLISHED) || eventOld.getEventStatus().equals(State.CANCELED)) { //точно ли?
            throw new DataIntegrityViolationException("Можно изменить только неподтвержденное событие");
        }

        Event eventUpdate = updateEventFields(eventOld, inputEvent);

        LocalDateTime gotEventDate = inputEvent.getEventDate();
        if (gotEventDate != null) {
            if (gotEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Некорректные параметры даты.Дата начала изменяемого события должна " +
                        "быть не ранее чем за час от даты публикации.");
            }
            eventUpdate.setEventDate(inputEvent.getEventDate());
        }

        EventAdminState gotAction = inputEvent.getStateAction();
        if (gotAction != null) {
            if (EventAdminState.PUBLISH_EVENT.equals(gotAction)) {
                eventUpdate.setEventStatus(State.PUBLISHED);
            } else if (EventAdminState.REJECT_EVENT.equals(gotAction)) {
                eventUpdate.setEventStatus(State.CANCELED);
            }
        }

        return EventMapper.toEventFullDto(eventRepository.save(eventUpdate));
    }

    @Override
    public List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId) {
        userService.checkExistUser(userId);
        checkEvenByInitiatorAndEventId(userId, eventId);

        List<Request> requests = requestsRepository.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    private Event checkEvenByInitiatorAndEventId(Long userId, Long eventId) {
        Optional<Event> event = eventRepository.findFirstByIdAndInitiatorIdIs(eventId, userId);

        if (event.isEmpty()) {
            throw new NotFoundException("Event with id= " + eventId + " was not found");
        } else {
           return event.get();
        }
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest inputUpdate) {
        userService.checkExistUser(userId);
        Event event = checkEvenByInitiatorAndEventId(userId, eventId);


        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Это событие не требует подтверждения запросов");
        }

        RequestStatus status = inputUpdate.getStatus();

        int confirmedRequestsCount = requestsRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        switch (status) {
            case CONFIRMED:
                if (event.getParticipantLimit() == confirmedRequestsCount) {
                    throw new DataIntegrityViolationException("Лимит участников исчерпан");
                }
                CaseUpdatedStatusDto updatedStatusConfirmed = updatedStatusConfirmed(event,
                        CaseUpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(inputUpdate.getRequestIds())).build(),
                        RequestStatus.CONFIRMED, confirmedRequestsCount);

                List<Request> confirmedRequests = requestsRepository.findAllById(updatedStatusConfirmed.getProcessedIds());
                List<Request> rejectedRequests = new ArrayList<>();
                if (updatedStatusConfirmed.getIdsFromUpdateStatus().size() != 0) {
                    List<Long> ids = updatedStatusConfirmed.getIdsFromUpdateStatus();
                    rejectedRequests = rejectRequest(ids, eventId);
                }

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            case REJECTED:
                if (event.getParticipantLimit() == confirmedRequestsCount) {
                    throw new DataIntegrityViolationException("Лимит участников исчерпан");
                }

                final CaseUpdatedStatusDto updatedStatusReject = updatedStatusConfirmed(event,
                        CaseUpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(inputUpdate.getRequestIds())).build(),
                        RequestStatus.REJECTED, confirmedRequestsCount);
                List<Request> rejectRequest = requestsRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResult.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList()))
                        .build();
            default:
                throw new ValidationException("Некорректный статус - " + status);
        }
    }

    private List<Request> rejectRequest(List<Long> ids, Long eventId) {
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(eventId, ids);

        for (Request request : requestListLoaded) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }
            request.setStatus(RequestStatus.REJECTED);
            requestList.add(request);
            rejectedRequests.add(request);
        }
        requestsRepository.saveAll(requestList);
        return rejectedRequests;
    }
    private List<Request> checkRequestOrEventList(Long eventId, List<Long> requestId) {
        return requestsRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException("Запроса с id = " + requestId + " или события с id = "
                        + eventId + "не существуют"));
    }

    private CaseUpdatedStatusDto updatedStatusConfirmed(Event event, CaseUpdatedStatusDto caseUpdatedStatus,
                                                        RequestStatus status, int confirmedRequestsCount) {
        int freeRequest = event.getParticipantLimit() - confirmedRequestsCount;
        List<Long> ids = caseUpdatedStatus.getIdsFromUpdateStatus();
        List<Long> processedIds = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(event.getId(), ids);
        List<Request> requestList = new ArrayList<>();

        for (Request request : requestListLoaded) {
            if (freeRequest == 0) {
                break;
            }

            request.setStatus(status);
            requestList.add(request);

            processedIds.add(request.getId());
            freeRequest--;
        }

        requestsRepository.saveAll(requestList);
        caseUpdatedStatus.setProcessedIds(processedIds);
        return caseUpdatedStatus;
    }

    @Override
    public Event checkExistEvent(long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);

        if (event.isEmpty()) {
            throw new NotFoundException("Event with id=" + eventId + "was not found");
        } else {
            return event.get();
        }
    }

    @Override
    public List<EventShortDto> getAllEventFromPublic(SearchParamsForEvents searchParamsForEvents, HttpServletRequest request) {

        if (searchParamsForEvents.getRangeEnd() != null && searchParamsForEvents.getRangeStart() != null) {
            if (searchParamsForEvents.getRangeEnd().isBefore(searchParamsForEvents.getRangeStart())) {
                throw new ValidationException("Дата окончания не может быть раньше даты начала");
            }
        }

        addStatsClient(request);

        Pageable pageable = PageRequest.of(searchParamsForEvents.getFrom() / searchParamsForEvents.getSize(), searchParamsForEvents.getSize());

        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (searchParamsForEvents.getText() != null) {
            String searchText = searchParamsForEvents.getText().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (searchParamsForEvents.getCategories() != null && !searchParamsForEvents.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(searchParamsForEvents.getCategories()));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(searchParamsForEvents.getRangeStart(), now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (searchParamsForEvents.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), searchParamsForEvents.getRangeEnd()));
        }

        if (searchParamsForEvents.getOnlyAvailable() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("eventStatus"), State.PUBLISHED));

        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();
        List<EventShortDto> result = resultEvents
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
        Map<Long, Long> viewStatsMap = getViewsAllEvents(resultEvents);

        for (EventShortDto event : result) {
            Long viewsFromMap = viewStatsMap.getOrDefault(event.getId(), 0L);
            event.setViews(viewsFromMap);
        }

        return result;
    }

    private Map<Long, Long> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedDate)
                .collect(Collectors.toList());
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStatsMap = new HashMap<>();

        if (earliestDate != null) {
            ResponseEntity<Object> response = client.getStats(earliestDate, LocalDateTime.now(),
                    uris, true);

            List<ViewStats> viewStatsList = objectMapper.convertValue(response.getBody(), new TypeReference<>() {
            });

            viewStatsMap = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                            ViewStats::getHits
                    ));
        }
        return viewStatsMap;
    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        return null;
    }

    private void addStatsClient(HttpServletRequest request) {
        client.postStat(RequestStatsDto.builder()
                .app(applicationName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestsRepository.findAllByEventIdInAndStatus(events.stream()
                        .map(Event::getId)
                        .collect(Collectors.toList()),
                RequestStatus.CONFIRMED);
        return requests.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private Event updateEventFields(Event eventModifiable, UpdateEventRequest eventUpdate) {
        String gotAnnotation = eventUpdate.getAnnotation();
        if (gotAnnotation != null && !gotAnnotation.isBlank()) {
            eventModifiable.setAnnotation(gotAnnotation);
        }
        Long gotCategory = eventUpdate.getCategory();
        if (gotCategory != null) {
            Category category = categoryService.checkExistCategory(gotCategory);
            eventModifiable.setCategory(category);
        }
        String gotDescription = eventUpdate.getDescription();
        if (gotDescription != null && !gotDescription.isBlank()) {
            eventModifiable.setDescription(gotDescription);
        }
        if (eventUpdate.getLocation() != null) {
            Location location = LocationMapper.toLocation(eventUpdate.getLocation());
            eventModifiable.setLocation(location);
        }
        Integer gotParticipantLimit = eventUpdate.getParticipantLimit();
        if (gotParticipantLimit != null) {
            eventModifiable.setParticipantLimit(gotParticipantLimit);
        }
        if (eventUpdate.getPaid() != null) {
            eventModifiable.setPaid(eventUpdate.getPaid());
        }
        Boolean requestModeration = eventUpdate.getRequestModeration();
        if (requestModeration != null) {
            eventModifiable.setRequestModeration(requestModeration);
        }
        String gotTitle = eventUpdate.getTitle();
        if (gotTitle != null && !gotTitle.isBlank()) {
            eventModifiable.setTitle(gotTitle);
        }
        return eventModifiable;
    }

    private void checkValidDataTimeForUser(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (dateTime.isBefore(now.plusHours(2))) {
            throw new ValidationException("Начало события не должно начинаться раньше, чем через два часа.");
        }
    }
}
