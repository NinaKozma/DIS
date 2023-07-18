package se.magnus.microservices.core.image;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.image.Image;
import se.magnus.microservices.core.image.persistence.ImageRepository;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:image-db"})

public class ImageServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ImageRepository repository;


	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getImagesByPostId() {

		int postId = 1;

		assertEquals(0, repository.findByPostId(postId).size());

		postAndVerifyImage(postId, 1, OK);
		postAndVerifyImage(postId, 2, OK);
		postAndVerifyImage(postId, 3, OK);

		assertEquals(3, repository.findByPostId(postId).size());

		getAndVerifyImagesByPostId(postId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].postId").isEqualTo(postId)
			.jsonPath("$[2].imageId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int postId = 1;
		int imageId = 1;

		assertEquals(0, repository.count());

		postAndVerifyImage(postId, imageId, OK)
			.jsonPath("$.postId").isEqualTo(postId)
			.jsonPath("$.imageId").isEqualTo(imageId);

		assertEquals(1, repository.count());

		postAndVerifyImage(postId, imageId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/image")
			.jsonPath("$.message").isEqualTo("Duplicate key, Post Id: 1, Image Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteImages() {

		int postId = 1;
		int recommendationId = 1;

		postAndVerifyImage(postId, recommendationId, OK);
		assertEquals(1, repository.findByPostId(postId).size());

		deleteAndVerifyImagesByPostId(postId, OK);
		assertEquals(0, repository.findByPostId(postId).size());

		deleteAndVerifyImagesByPostId(postId, OK);
	}

	@Test
	public void getImagesMissingParameter() {

		getAndVerifyImagesByPostId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/image")
			.jsonPath("$.message").isEqualTo("Required int parameter 'postId' is not present");
	}

	@Test
	public void getImagesInvalidParameter() {

		getAndVerifyImagesByPostId("?postId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/image")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getImagesNotFound() {

		getAndVerifyImagesByPostId("?postId=213", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getImagesInvalidParameterNegativeValue() {

		int postIdInvalid = -1;

		getAndVerifyImagesByPostId("?postId=" + postIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/image")
			.jsonPath("$.message").isEqualTo("Invalid postId: " + postIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyImagesByPostId(int postId, HttpStatus expectedStatus) {
		return getAndVerifyImagesByPostId("?postId=" + postId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyImagesByPostId(String postIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/image" + postIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyImage(int postId, int imageId, HttpStatus expectedStatus) {
		Image image = new Image(postId, imageId, "Some image URL", LocalDate.now(), "SA");
		return client.post()
			.uri("/image")
			.body(just(image), Image.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyImagesByPostId(int postId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/image?postId=" + postId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}