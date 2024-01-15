package ru.practicum.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.service.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
