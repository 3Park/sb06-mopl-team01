package org.example.mopl.content.dto.request;

public record ReviewUpdateRequest(
    String text,
    Double rating
) {

}
