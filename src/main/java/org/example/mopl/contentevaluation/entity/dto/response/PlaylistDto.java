package org.example.mopl.contentevaluation.entity.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.example.mopl.content.entity.dto.response.ContentDto;

@Builder
public record PlaylistDto(
    @JsonProperty(value = "id")
    UUID uuid,
    Object UserDto,
    String title,
    String description,
    LocalDateTime updatedAt,
    Long subscriberCount,
    Boolean subscribeByMe,
    List<ContentDto> contents
) {

}
