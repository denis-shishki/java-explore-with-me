package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestsRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventIdInAndStatus(List<Long> eventsIds, RequestStatus requestStatus);

    List<Request> findAllByEventId(Long eventId);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    Optional<List<Request>> findByEventIdAndIdIn(Long eventId, List<Long> id);

}
