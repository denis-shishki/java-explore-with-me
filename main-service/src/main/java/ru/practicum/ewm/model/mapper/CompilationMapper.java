package ru.practicum.ewm.model.mapper;

import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.dto.CompilationDto;
import ru.practicum.ewm.model.dto.NewCompilationDto;

import java.util.stream.Collectors;

public class CompilationMapper {
    public static CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toSet()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static Compilation toCompilation(NewCompilationDto compilationDto) {
        return Compilation.builder()
                .pinned(compilationDto.getPinned())
                .title(compilationDto.getTitle())
                .build();
    }
}
