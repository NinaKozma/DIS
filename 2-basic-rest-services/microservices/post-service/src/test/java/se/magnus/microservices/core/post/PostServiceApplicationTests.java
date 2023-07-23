package se.magnus.microservices.core.post;

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
import se.magnus.api.event.Event;
import se.magnus.microservices.core.post.persistence.*;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "spring.data.mongodb.port: 0",
		"eureka.client.enabled=false" })
public class PostServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private PostRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getPostById() {

		int postId = 1;

		assertNull(repository.findByPostId(postId).block());
		assertEquals(0, (long) repository.count().block());

		sendCreatePostEvent(postId);

		assertNotNull(repository.findByPostId(postId).block());
		assertEquals(1, (long) repository.count().block());

		getAndVerifyPost(postId, OK).jsonPath("$.postId").isEqualTo(postId);
	}

	@Test
	public void duplicateError() {

		int postId = 1;

		assertNull(repository.findByPostId(postId).block());

		sendCreatePostEvent(postId);

		assertNotNull(repository.findByPostId(postId).block());

		try {
			sendCreatePostEvent(postId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException) {
				InvalidInputException iie = (InvalidInputException) me.getCause();
				assertEquals("Duplicate key, Post Id: " + postId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
	}

	@Test
	public void deletePost() {

		int postId = 1;

		sendCreatePostEvent(postId);
		assertNotNull(repository.findByPostId(postId).block());

		sendDeletePostEvent(postId);
		assertNull(repository.findByPostId(postId).block());

		sendDeletePostEvent(postId);
	}

	@Test
	public void getPostInvalidParameterString() {

		getAndVerifyPost("/no-integer", BAD_REQUEST).jsonPath("$.path").isEqualTo("/post/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getPostNotFound() {

		int postIdNotFound = 13;
		getAndVerifyPost(postIdNotFound, NOT_FOUND).jsonPath("$.path").isEqualTo("/post/" + postIdNotFound)
				.jsonPath("$.message").isEqualTo("No post found for postId: " + postIdNotFound);
	}

	@Test
	public void getPostInvalidParameterNegativeValue() {

		int postIdInvalid = -1;

		getAndVerifyPost(postIdInvalid, UNPROCESSABLE_ENTITY).jsonPath("$.path").isEqualTo("/post/" + postIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid postId: " + postIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyPost(int postId, HttpStatus expectedStatus) {
		return getAndVerifyPost("/" + postId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyPost(String postIdPath, HttpStatus expectedStatus) {
		return client.get().uri("/post" + postIdPath).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
	}

	private void sendCreatePostEvent(int postId) {
		Post post = new Post(postId, "Type of Post", "Post Caption", LocalDate.now(), "SA");
		Event<Integer, Post> event = new Event(CREATE, postId, post);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeletePostEvent(int postId) {
		Event<Integer, Post> event = new Event(DELETE, postId, null);
		input.send(new GenericMessage<>(event));
	}
}
