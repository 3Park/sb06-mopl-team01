package org.example.mopl.content.mapper;

import org.example.mopl.content.dto.request.ContentCreateRequest;
import org.example.mopl.content.dto.request.ContentUpdateRequest;
import org.example.mopl.content.entity.Content;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ContentMapper {

  @Mappings({
      @Mapping(target = "thumbnailUrl", ignore = true)
  })
  Content createRequestToEntity(ContentCreateRequest request);

  Content updateRequestToEntity(ContentUpdateRequest request);

}
