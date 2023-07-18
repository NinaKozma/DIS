package se.magnus.microservices.core.reaction.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.microservices.core.reaction.persistence.ReactionEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Reaction entityToApi(ReactionEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    ReactionEntity apiToEntity(Reaction api);

    List<Reaction> entityListToApiList(List<ReactionEntity> entity);
    List<ReactionEntity> apiListToEntityList(List<Reaction> api);
}