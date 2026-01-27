package org.example.mopl.content.entity.dto.request;

public record ReviewUpdateRequest(
    String text,
    Double rating
) {

}
