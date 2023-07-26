package se.magnus.microservices.core.image;

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
import se.magnus.api.core.image.Image;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.image.persistence.ImageRepository;
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
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "logging.level.se.magnus=DEBUG",
		"eureka.client.enabled=false", "spring.datasource.url=jdbc:h2:mem:image-db",
		"spring.cloud.config.enabled=false", "server.error.include-message=always" })
public class ImageServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ImageRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getImagesByPostId() {

		int postId = 1;

		assertEquals(0, repository.findByPostId(postId).size());

		sendCreateImageEvent(postId, 1);
		sendCreateImageEvent(postId, 2);
		sendCreateImageEvent(postId, 3);

		assertEquals(3, repository.findByPostId(postId).size());

		getAndVerifyImagesByPostId(postId, OK).jsonPath("$.length()").isEqualTo(3).jsonPath("$[2].postId")
				.isEqualTo(postId).jsonPath("$[2].imageId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int postId = 1;
		int imageId = 1;

		assertEquals(0, repository.count());

		sendCreateImageEvent(postId, imageId);

		assertEquals(1, repository.count());

		try {
			sendCreateImageEvent(postId, imageId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException) {
				InvalidInputException iie = (InvalidInputException) me.getCause();
				assertEquals("Duplicate key, Post Id: 1, Image Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteImages() {

		int postId = 1;
		int imageId = 1;

		sendCreateImageEvent(postId, imageId);
		assertEquals(1, repository.findByPostId(postId).size());

		sendDeleteImageEvent(postId);
		assertEquals(0, repository.findByPostId(postId).size());

		sendDeleteImageEvent(postId);
	}

	@Test
	public void getImagesMissingParameter() {

		getAndVerifyImagesByPostId("", BAD_REQUEST).jsonPath("$.path").isEqualTo("/image").jsonPath("$.message")
				.isEqualTo("Required int parameter 'postId' is not present");
	}

	@Test
	public void getImagesInvalidParameter() {

		getAndVerifyImagesByPostId("?postId=no-integer", BAD_REQUEST).jsonPath("$.path").isEqualTo("/image")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getImagesNotFound() {

		getAndVerifyImagesByPostId("?postId=213", OK).jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getImagesInvalidParameterNegativeValue() {

		int postIdInvalid = -1;

		getAndVerifyImagesByPostId("?postId=" + postIdInvalid, UNPROCESSABLE_ENTITY).jsonPath("$.path")
				.isEqualTo("/image").jsonPath("$.message").isEqualTo("Invalid postId: " + postIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyImagesByPostId(int postId, HttpStatus expectedStatus) {
		return getAndVerifyImagesByPostId("?postId=" + postId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyImagesByPostId(String postIdQuery, HttpStatus expectedStatus) {
		return client.get().uri("/image" + postIdQuery).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
	}

	private void sendCreateImageEvent(int postId, int imageId) {
		Image image = new Image(postId, imageId, "Image Url", LocalDate.now(), "SA");
		Event<Integer, Post> event = new Event(CREATE, postId, image);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteImageEvent(int postId) {
		Event<Integer, Post> event = new Event(DELETE, postId, null);
		input.send(new GenericMessage<>(event));
	}
}