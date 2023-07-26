package se.magnus.microservices.composite.post;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.post.Post;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.image.Image;
import se.magnus.microservices.composite.post.services.PostCompositeIntegration;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = { PostCompositeServiceApplication.class,
		TestSecurityConfig.class }, properties = { "spring.main.allow-bean-definition-overriding=true",
				"eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@RunWith(SpringRunner.class)
public class PostCompositeServiceApplicationTests {

	private static final int POST_ID_OK = 1;
	private static final int POST_ID_NOT_FOUND = 2;
	private static final int POST_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;

	@MockBean
	private PostCompositeIntegration compositeIntegration;

	@Before
	public void setUp() {

		when(compositeIntegration.getPost(POST_ID_OK)).thenReturn(
				Mono.just(new Post(POST_ID_OK, "instagram post", "Enjoying...", LocalDate.now(), "mock-address")));
		when(compositeIntegration.getReactions(POST_ID_OK))
				.thenReturn(Flux.fromIterable(singletonList(new Reaction(POST_ID_OK, 1, "heart", "mock address"))));
		when(compositeIntegration.getComments(POST_ID_OK)).thenReturn(Flux.fromIterable(
				singletonList(new Comment(POST_ID_OK, 1, "nice video!", LocalDate.now(), "mock address"))));
		when(compositeIntegration.getImages(POST_ID_OK)).thenReturn(Flux.fromIterable(
				singletonList(new Image(POST_ID_OK, 1, "Some image URL...", LocalDate.now(), "mock address"))));

		when(compositeIntegration.getPost(POST_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + POST_ID_NOT_FOUND));

		when(compositeIntegration.getPost(POST_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + POST_ID_INVALID));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void getPostById() {

		getAndVerifyPost(POST_ID_OK, OK).jsonPath("$.postId").isEqualTo(POST_ID_OK).jsonPath("$.reactions.length()")
				.isEqualTo(1).jsonPath("$.comments.length()").isEqualTo(1).jsonPath("$.images.length()")
				.isEqualTo(1);
	}

	@Test
	public void getPostNotFound() {

		getAndVerifyPost(POST_ID_NOT_FOUND, NOT_FOUND).jsonPath("$.path")
				.isEqualTo("/post-composite/" + POST_ID_NOT_FOUND).jsonPath("$.message")
				.isEqualTo("NOT FOUND: " + POST_ID_NOT_FOUND);
	}

	@Test
	public void getPostInvalidInput() {

		getAndVerifyPost(POST_ID_INVALID, UNPROCESSABLE_ENTITY).jsonPath("$.path")
				.isEqualTo("/post-composite/" + POST_ID_INVALID).jsonPath("$.message")
				.isEqualTo("INVALID: " + POST_ID_INVALID);
	}

	private WebTestClient.BodyContentSpec getAndVerifyPost(int postId, HttpStatus expectedStatus) {
		return client.get().uri("/post-composite/" + postId).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
	}
}
