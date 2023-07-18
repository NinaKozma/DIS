package se.magnus.microservices.core.reaction;

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
import se.magnus.api.core.reaction.Reaction;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.reaction.persistence.ReactionRepository;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "spring.data.mongodb.port: 0" })
public class ReactionServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReactionRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getReactionsByPostId() {

		int postId = 1;

		sendCreateReactionEvent(postId, 1);
		sendCreateReactionEvent(postId, 2);
		sendCreateReactionEvent(postId, 3);

		assertEquals(3, (long) repository.findByPostId(postId).count().block());

		getAndVerifyReactionsByPostId(postId, OK).jsonPath("$.length()").isEqualTo(3).jsonPath("$[2].postId")
				.isEqualTo(postId).jsonPath("$[2].reactionId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int postId = 1;
		int reactionId = 1;

		sendCreateReactionEvent(postId, reactionId);

		assertEquals(1, (long) repository.count().block());

		try {
			sendCreateReactionEvent(postId, reactionId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException) {
				InvalidInputException iie = (InvalidInputException) me.getCause();
				assertEquals("Duplicate key, Post Id: 1, Reaction Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long) repository.count().block());
	}

	@Test
	public void deleteReactions() {

		int postId = 1;
		int reactionId = 1;

		sendCreateReactionEvent(postId, reactionId);
		assertEquals(1, (long) repository.findByPostId(postId).count().block());

		sendDeleteReactionEvent(postId);
		assertEquals(0, (long) repository.findByPostId(postId).count().block());

		sendDeleteReactionEvent(postId);
	}

	@Test
	public void getReactionsMissingParameter() {

		getAndVerifyReactionsByPostId("", BAD_REQUEST).jsonPath("$.path").isEqualTo("/reaction").jsonPath("$.message")
				.isEqualTo("Required int parameter 'postId' is not present");
	}

	@Test
	public void getReactionsInvalidParameter() {

		getAndVerifyReactionsByPostId("?postId=no-integer", BAD_REQUEST).jsonPath("$.path").isEqualTo("/reaction")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getReactionsNotFound() {

		getAndVerifyReactionsByPostId("?postId=113", OK).jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReactionsInvalidParameterNegativeValue() {

		int postIdInvalid = -1;

		getAndVerifyReactionsByPostId("?postId=" + postIdInvalid, UNPROCESSABLE_ENTITY).jsonPath("$.path")
				.isEqualTo("/reaction").jsonPath("$.message").isEqualTo("Invalid postId: " + postIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReactionsByPostId(int postId, HttpStatus expectedStatus) {
		return getAndVerifyReactionsByPostId("?postId=" + postId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReactionsByPostId(String postIdQuery, HttpStatus expectedStatus) {
		return client.get().uri("/reaction" + postIdQuery).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
	}

	private void sendCreateReactionEvent(int postId, int reactionId) {
		Reaction reaction = new Reaction(postId, reactionId, "Heart <3", "SA");
		Event<Integer, Post> event = new Event(CREATE, postId, reaction);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteReactionEvent(int postId) {
		Event<Integer, Post> event = new Event(DELETE, postId, null);
		input.send(new GenericMessage<>(event));
	}
}