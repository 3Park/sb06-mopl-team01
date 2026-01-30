package org.example.mopl.content.mapper;

import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.entity.Content;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentMapper {

  Content createRequestToEntity(ContentCreateRequest request);

  Content updateRequestToEntity(ContentUpdateRequest request);

}
