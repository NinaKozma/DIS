package se.magnus.microservices.core.image.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.image.Image;
import se.magnus.microservices.core.image.persistence.ImageEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Image entityToApi(ImageEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    ImageEntity apiToEntity(Image api);

    List<Image> entityListToApiList(List<ImageEntity> entity);
    List<ImageEntity> apiListToEntityList(List<Image> api);
}