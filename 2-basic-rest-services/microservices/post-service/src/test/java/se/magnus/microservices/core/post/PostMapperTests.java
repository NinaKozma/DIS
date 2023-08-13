package se.magnus.microservices.core.post;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.post.Post;
import se.magnus.microservices.core.post.persistence.PostEntity;
import se.magnus.microservices.core.post.services.PostMapper;

import static org.junit.Assert.*;

import java.time.LocalDate;

public class PostMapperTests {

    private PostMapper mapper = Mappers.getMapper(PostMapper.class);

    @Test
    public void postMapperTests() {

        assertNotNull(mapper);

        Post api = new Post(1, "type of post", "post caption", LocalDate.now(), "sa");

        PostEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getPostId(), entity.getPostId());
        assertEquals(api.getTypeOfPost(), entity.getTypeOfPost());
        assertEquals(api.getPostCaption(), entity.getPostCaption());
        assertEquals(api.getPostedOn(), entity.getPostedOn());

        Post api2 = mapper.entityToApi(entity);

        assertEquals(api.getPostId(), api2.getPostId());
        assertEquals(api.getTypeOfPost(),      api2.getTypeOfPost());
        assertEquals(api.getPostCaption(),    api2.getPostCaption());
        assertEquals(api.getPostedOn(), api2.getPostedOn());
        assertNull(api2.getServiceAddress());
    }
}