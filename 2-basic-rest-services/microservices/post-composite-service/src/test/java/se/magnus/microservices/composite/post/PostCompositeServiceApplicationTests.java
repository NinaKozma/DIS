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
import se.magnus.api.composite.post.PostAggregate;
import se.magnus.api.composite.post.ReactionSummary;
import se.magnus.api.composite.post.CommentSummary;
import se.magnus.api.composite.post.ImageSummary;
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
import static reactor.core.publisher.Mono.just;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
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
	public void createCompositePost1() {

		PostAggregate compositePost = new PostAggregate(1, "type of post", LocalDate.now(), "post caption", null, null,
				null, null);

		postAndVerifyPost(compositePost, OK);
	}

	@Test
	public void createCompositePost2() {
		PostAggregate compositePost = new PostAggregate(1, "type of post", LocalDate.now(), "post caption",
				singletonList(new ReactionSummary(1, "type of reaction")),
				singletonList(new CommentSummary(1, "comment text", LocalDate.now())),
				singletonList(new ImageSummary(1, LocalDate.now(), "image url")), null);

		postAndVerifyPost(compositePost, OK);
	}

	@Test
	public void deleteCompositePost() {
		PostAggregate compositePost = new PostAggregate(1, "type of post", LocalDate.now(), "post caption",
				singletonList(new ReactionSummary(1, "type of reaction")),
				singletonList(new CommentSummary(1, "comment text", LocalDate.now())),
				singletonList(new ImageSummary(1, LocalDate.now(), "image url")), null);

		postAndVerifyPost(compositePost, OK);

		deleteAndVerifyPost(compositePost.getPostId(), OK);
		deleteAndVerifyPost(compositePost.getPostId(), OK);
	}

	@Test
	public void getPostById() {

		client.get().uri("/post-composite/" + POST_ID_OK).accept(APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON).expectBody().jsonPath("$.postId").isEqualTo(POST_ID_OK)
				.jsonPath("$.reactions.length()").isEqualTo(1).jsonPath("$.comments.length()").isEqualTo(1)
				.jsonPath("$.images.length()").isEqualTo(1);
	}

	@Test
	public void getPostNotFound() {

		client.get().uri("/post-composite/" + POST_ID_NOT_FOUND).accept(APPLICATION_JSON).exchange().expectStatus()
				.isNotFound().expectHeader().contentType(APPLICATION_JSON).expectBody().jsonPath("$.path")
				.isEqualTo("/post-composite/" + POST_ID_NOT_FOUND).jsonPath("$.message")
				.isEqualTo("NOT FOUND: " + POST_ID_NOT_FOUND);
	}

	@Test
	public void getPostInvalidInput() {

		client.get().uri("/post-composite/" + POST_ID_INVALID).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(UNPROCESSABLE_ENTITY).expectHeader().contentType(APPLICATION_JSON).expectBody()
				.jsonPath("$.path").isEqualTo("/post-composite/" + POST_ID_INVALID).jsonPath("$.message")
				.isEqualTo("INVALID: " + POST_ID_INVALID);
	}

	private WebTestClient.BodyContentSpec getAndVerifyPost(int postId, HttpStatus expectedStatus) {
		return client.get().uri("/post-composite/" + postId).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(expectedStatus).expectHeader().contentType(APPLICATION_JSON).expectBody();
	}

	private void postAndVerifyPost(PostAggregate compositePost, HttpStatus expectedStatus) {
		client.post().uri("/post-composite").body(just(compositePost), PostAggregate.class).exchange().expectStatus()
				.isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyPost(int postId, HttpStatus expectedStatus) {
		client.delete().uri("/post-composite/" + postId).exchange().expectStatus().isEqualTo(expectedStatus);
	}
}
