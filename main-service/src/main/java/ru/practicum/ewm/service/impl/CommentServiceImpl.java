package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.model.dto.CommentDto;
import ru.practicum.ewm.model.dto.NewCommentDto;
import ru.practicum.ewm.model.dto.UpdateCommentDto;
import ru.practicum.ewm.model.enums.EventStatus;
import ru.practicum.ewm.model.mapper.CommentMapper;
import ru.practicum.ewm.paginator.Paginator;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.service.EventsService;
import ru.practicum.ewm.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventsService eventsService;


    @Override
    @Transactional
    public CommentDto patchByUser(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        User user = userService.checkExistUser(userId);
        Comment comment = checkComment(commentId);
        checkAuthorComment(user, comment);
        LocalDateTime updateTime = LocalDateTime.now();

        if (updateTime.isAfter(comment.getCreated().plusMinutes(30L))) {
            throw new DataIntegrityViolationException("Редактировать комментарий можно не позже 30 минут после публикации");
        }

        comment.setText(updateCommentDto.getText());
        comment.setIsEdited(true);
        return CommentMapper.toCommentDto(commentRepository.save(comment), comment.getEvent().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentUser(Long userId) {
        userService.checkExistUser(userId);
        List<Comment> commentList = commentRepository.findByAuthor_Id(userId);
        return commentList.stream()
                .map((comment -> CommentMapper.toCommentDto(comment, comment.getEvent().getId())))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsEvent(Long eventId, Integer from, Integer size) {
        eventsService.checkExistEvent(eventId);
        Pageable pageable = Paginator.getPageable(from, size);
        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId, pageable);

        return comments.stream()
                .map((comment -> CommentMapper.toCommentDto(comment, comment.getEvent().getId())))
                .collect(Collectors.toList());

    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        User user = userService.checkExistUser(userId);
        Comment comment = checkComment(commentId);
        checkAuthorComment(user, comment);
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        checkComment(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public List<CommentDto> search(String text, Integer from, Integer size) {
        Pageable pageable = Paginator.getPageable(from, size);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Comment> comments = commentRepository.search(text, pageable);

        return comments.stream()
                .map((comment -> CommentMapper.toCommentDto(comment, comment.getEvent().getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        Event event = eventsService.checkExistEvent(eventId);
        User user = userService.checkExistUser(userId);
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new DataIntegrityViolationException("Чтобы добавить комментарий событие должно быть опубликовано");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(newCommentDto, event, user));
        return CommentMapper.toCommentDto(comment, eventId);
    }

    private Comment checkComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException(
                String.format("Комментарий c id= " + id + "  не найден")));
    }

    private void checkAuthorComment(User user, Comment comment) {
        if (!comment.getAuthor().equals(user)) {
            throw new DataIntegrityViolationException("Пользователь не является автором комментария");
        }
    }
}