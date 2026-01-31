package org.example.mopl.content.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PROTECTED)
public record ReviewDto(
    @JsonProperty(value = "id")
    UUID uuid,
    UUID contentId,
    AuthorDto author,
    String text,
    Double rating
) {

    public static ReviewDto of(UUID uuid, UUID contentId, AuthorDto author, String text, double rating) {
        return ReviewDto.builder()
            .uuid(uuid)
            .contentId(contentId)
            .author(author)
            .text(text)
            .rating(rating)
            .build();
    }

}
