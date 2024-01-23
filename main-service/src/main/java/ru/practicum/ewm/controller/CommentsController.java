package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.model.dto.CommentDto;
import ru.practicum.ewm.model.dto.NewCommentDto;
import ru.practicum.ewm.model.dto.UpdateCommentDto;
import ru.practicum.ewm.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentsController {
    private final CommentService commentService;

    @PostMapping("/comments/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto postComment(@PathVariable Long userId,
                                  @PathVariable Long eventId,
                                  @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/comments/{commentId}/users/{userId}/")
    public CommentDto patchCommentByUser(@PathVariable Long userId, @PathVariable Long commentId,
                                         @Valid @RequestBody UpdateCommentDto updateCommentDto) {

        return commentService.patchByUser(userId, commentId, updateCommentDto);
    }

    @GetMapping("/comments/users/{userId}")
    public List<CommentDto> getUserComments(@PathVariable Long userId) {
        return commentService.getUserComments(userId);
    }

    @DeleteMapping("/comments/{commentId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/admin/comments/search")
    public List<CommentDto> searchComments(@RequestParam(name = "text") String text,
                                           @RequestParam(value = "from", defaultValue = "0") Integer from,
                                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return commentService.search(text, from, size);
    }

    @GetMapping("/comments/{eventId}")
    public List<CommentDto> getCommentsAllCommentsByEvent(@PathVariable Long eventId,
                                                        @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        return commentService.getEventComments(eventId, from, size);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}
