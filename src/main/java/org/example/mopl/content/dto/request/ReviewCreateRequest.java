package org.example.mopl.content.dto.request;

public record ReviewCreateRequest(
    String contentId,
    String text,
    Double rating
) {

}
