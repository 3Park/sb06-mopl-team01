package org.example.mopl.content.entity.dto.request;

public record ReviewCreateRequest(
    String contentId,
    String text,
    Double rating
) {

}
