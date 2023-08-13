package se.magnus.microservices.core.comment;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.comment.Comment;
import se.magnus.microservices.core.comment.persistence.CommentEntity;
import se.magnus.microservices.core.comment.services.CommentMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CommentMapperTests {

	private CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

	@Test
	public void commentMapperTests() {

		assertNotNull(mapper);

		Comment api = new Comment(1, 2, "comment text", LocalDate.now(), "adr");

		CommentEntity entity = mapper.apiToEntity(api);

		assertEquals(api.getPostId(), entity.getPostId());
		assertEquals(api.getCommentId(), entity.getCommentId());
		assertEquals(api.getCommentText(), entity.getCommentText());
		assertEquals(api.getCommentDate(), entity.getCommentDate());

		Comment api2 = mapper.entityToApi(entity);

		assertEquals(api.getPostId(), api2.getPostId());
		assertEquals(api.getCommentId(), api2.getCommentId());
		assertEquals(api.getCommentText(), api2.getCommentText());
		assertEquals(api.getCommentDate(), api2.getCommentDate());
		assertNull(api2.getServiceAddress());
	}

	@Test
	public void mapperListTests() {

		assertNotNull(mapper);

		Comment api = new Comment(1, 2, "comment text", LocalDate.now(), "adr");
		List<Comment> apiList = Collections.singletonList(api);

		List<CommentEntity> entityList = mapper.apiListToEntityList(apiList);
		assertEquals(apiList.size(), entityList.size());

		CommentEntity entity = entityList.get(0);

		assertEquals(api.getPostId(), entity.getPostId());
		assertEquals(api.getCommentId(), entity.getCommentId());
		assertEquals(api.getCommentText(), entity.getCommentText());
		assertEquals(api.getCommentDate(), entity.getCommentDate());

		List<Comment> api2List = mapper.entityListToApiList(entityList);
		assertEquals(apiList.size(), api2List.size());

		Comment api2 = api2List.get(0);

		assertEquals(api.getPostId(), api2.getPostId());
		assertEquals(api.getCommentId(), api2.getCommentId());
		assertEquals(api.getCommentText(), api2.getCommentText());
		assertEquals(api.getCommentDate(), api2.getCommentDate());
		assertNull(api2.getServiceAddress());
	}
}
