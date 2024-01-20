package ru.practicum.ewm;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@ToString
//todo поменяй стринг на дата и попробуй
public class ResponseStatsDto {
    private String app;
    private String uri;
    private Long hits;
}
