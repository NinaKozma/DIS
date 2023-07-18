package se.magnus.microservices.core.post.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.post.Post;
import se.magnus.microservices.core.post.persistence.PostEntity;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Post entityToApi(PostEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    PostEntity apiToEntity(Post api);
}
