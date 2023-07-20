package se.magnus.microservices.core.comment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.post.Post;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.comment.persistence.CommentRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "spring.datasource.url=jdbc:h2:mem:comment-db" })

public class CommentServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private CommentRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getCommentsByPostId() {

		int postId = 1;

		assertEquals(0, repository.findByPostId(postId).size());

		sendCreateCommentEvent(postId, 1);
		sendCreateCommentEvent(postId, 2);
		sendCreateCommentEvent(postId, 3);

		assertEquals(3, repository.findByPostId(postId).size());

		getAndVerifyCommentsByPostId(postId, OK).jsonPath("$.length()").isEqualTo(3).jsonPath("$[2].postId")
				.isEqualTo(postId).jsonPath("$[2].commentId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int postId = 1;
		int commentId = 1;

		assertEquals(0, repository.count());

		sendCreateCommentEvent(postId, commentId);

		assertEquals(1, repository.count());

		try {
			sendCreateCommentEvent(postId, commentId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException) {
				InvalidInputException iie = (InvalidInputException) me.getCause();
				assertEquals("Duplicate key, Post Id: 1, Comment Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteComments() {

		int postId = 1;
		int commentId = 1;

		sendCreateCommentEvent(postId, commentId);
		assertEquals(1, repository.findByPostId(postId).size());

		sendDeleteCommentEvent(postId);
		assertEquals(0, repository.findByPostId(postId).size());

		sendDeleteCommentEvent(postId);
	}

	@Test
	public void getCommentsMissingParameter() {

		getAndVerifyCommentsByPostId("", BAD_REQUEST).jsonPath("$.path").isEqualTo("/comment").jsonPath("$.message")
				.isEqualTo("Required int parameter 'postId' is not present");
	}

	@Test
	public void getCommentsInvalidParameter() {

		getAndVerifyCommentsByPostId("?postId=no-integer", BAD_REQUEST).jsonPath("$.path").isEqualTo("/comment")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getCommentsNotFound() {

		getAndVerifyCommentsByPostId("?postId=213", OK).jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getCommentsInvalidParameterNegativeValue() {

		int postIdInvalid = -1;

		getAndVerifyCommentsByPostId("?postId=" + postIdInvalid, UNPROCESSABLE_ENTITY).jsonPath("$.path")
				.isEqualTo("/comment").jsonPath("$.message").isEqualTo("Invalid postId: " + postIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyCommentsByPostId(int postId, HttpStatus expectedStatus) {
		return getAndVerifyCommentsByPostId("?postId=" + postId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyCommentsByPostId(String postIdQuery,
			HttpStatus expectedStatus) {
		return client.get().uri("/comment" + postIdQuery).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
	}

	private void sendCreateCommentEvent(int postId, int commentId) {
		Comment comment = new Comment(postId, commentId, "Comment Text", LocalDate.now(), "SA");
		Event<Integer, Post> event = new Event(CREATE, postId, comment);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteCommentEvent(int postId) {
		Event<Integer, Post> event = new Event(DELETE, postId, null);
		input.send(new GenericMessage<>(event));
	}
}