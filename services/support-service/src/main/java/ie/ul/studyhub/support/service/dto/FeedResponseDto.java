package ie.ul.studyhub.support.service.dto;

import java.util.List;

public record FeedResponseDto(
    List<FeedItemDto> items,
    int page,
    int size,
    long total) {}

