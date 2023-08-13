package se.magnus.microservices.core.reaction;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.microservices.core.reaction.persistence.ReactionEntity;
import se.magnus.microservices.core.reaction.services.ReactionMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ReactionMapperTests {

    private ReactionMapper mapper = Mappers.getMapper(ReactionMapper.class);

    @Test
    public void reactionMapperTests() {

        assertNotNull(mapper);

        Reaction api = new Reaction(1, 2, "type of reaction", "adr");

        ReactionEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getPostId(), entity.getPostId());
        assertEquals(api.getReactionId(), entity.getReactionId());
        assertEquals(api.getTypeOfReaction(), entity.getTypeOfReaction());

        Reaction api2 = mapper.entityToApi(entity);

        assertEquals(api.getPostId(), api2.getPostId());
        assertEquals(api.getReactionId(), api2.getReactionId());
        assertEquals(api.getTypeOfReaction(), api2.getTypeOfReaction());
        assertNull(api2.getServiceAddress());
    }

    @Test
    public void mapperListTests() {

        assertNotNull(mapper);

        Reaction api = new Reaction(1, 2, "type of reaction", "adr");
        List<Reaction> apiList = Collections.singletonList(api);

        List<ReactionEntity> entityList = mapper.apiListToEntityList(apiList);
        assertEquals(apiList.size(), entityList.size());

        ReactionEntity entity = entityList.get(0);

        assertEquals(api.getPostId(), entity.getPostId());
        assertEquals(api.getReactionId(), entity.getReactionId());
        assertEquals(api.getTypeOfReaction(), entity.getTypeOfReaction());

        List<Reaction> api2List = mapper.entityListToApiList(entityList);
        assertEquals(apiList.size(), api2List.size());

        Reaction api2 = api2List.get(0);

        assertEquals(api.getPostId(), api2.getPostId());
        assertEquals(api.getReactionId(), api2.getReactionId());
        assertEquals(api.getTypeOfReaction(), api2.getTypeOfReaction());
        assertNull(api2.getServiceAddress());
    }
}
