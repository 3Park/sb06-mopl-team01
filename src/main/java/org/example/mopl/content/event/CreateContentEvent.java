package org.example.mopl.content.event;

import org.example.mopl.content.entity.Content;

public record CreateContentEvent(
    Content content
) {

  public static CreateContentEvent of(Content content) {
    return new CreateContentEvent(content);
  }

}
