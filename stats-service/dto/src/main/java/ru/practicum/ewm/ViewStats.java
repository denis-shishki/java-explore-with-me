package ru.practicum.ewm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@AllArgsConstructor
@ToString
//todo поменяй стринг на дата и попробуй
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;
}