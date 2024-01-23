package ru.practicum.ewm.model.mapper;

import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.dto.CommentDto;
import ru.practicum.ewm.model.dto.NewCommentDto;

import java.time.LocalDateTime;


public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment, Long eventId) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .eventId(eventId)
                .created(comment.getCreated())
                .isEdited(comment.getIsEdited())
                .build();
    }

    public static Comment toComment(NewCommentDto commentDto, Event event, User user) {
        return Comment.builder()
                .text(commentDto.getText())
                .event(event)
                .author(user)
                .created(LocalDateTime.now())
                .isEdited(false)
                .build();
    }
}