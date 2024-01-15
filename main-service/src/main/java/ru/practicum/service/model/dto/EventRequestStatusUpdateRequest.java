package ru.practicum.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.service.model.enums.RequestStatus;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    private RequestStatus status;
}
