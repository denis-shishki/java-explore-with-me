package ru.practicum.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.model.dto.ParticipationRequestDto;
import ru.practicum.service.service.RequestService;

import javax.validation.constraints.Min;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class RequestController {
    private final RequestService requestService;

    @PostMapping("users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable(value = "userId") Long userId,
                                              @RequestParam(name = "eventId") Long eventId) {
        return requestService.addNewRequest(userId, eventId);
    }

    @GetMapping("users/{userId}/requests")
    public List<ParticipationRequestDto> getAllRequests(@PathVariable(value = "userId") Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

    @PatchMapping("users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto canceledRequest(@PathVariable(value = "userId") Long userId,
                                                   @PathVariable(value = "requestId") Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
