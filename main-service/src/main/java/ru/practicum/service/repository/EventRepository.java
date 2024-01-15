package ru.practicum.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.service.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByInitiatorIdIs(long userId, Pageable pageable);
    Optional<Event> findFirstByIdAndInitiatorIdIs(long eventId, long userId);
    Page<Event> findAll(Specification<Event> specification, Pageable pageable);
}
