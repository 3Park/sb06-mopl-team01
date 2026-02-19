package org.example.mopl.content.event;

public record DeleteS3ObjectEvent(
    String id
) {

  public static DeleteS3ObjectEvent of(String id) {
    return new DeleteS3ObjectEvent(id);
  }

}
