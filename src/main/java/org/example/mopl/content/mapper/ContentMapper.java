package org.example.mopl.content.mapper;

import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.entity.Content;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentMapper {

  public Content requestToEntity(ContentCreateRequest request);

}
