package se.magnus.microservices.core.comment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.comment.Comment;
import se.magnus.microservices.core.comment.persistence.CommentRepository;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:comment-db"})

public class CommentServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private CommentRepository repository;


	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getCommentsByPostId() {

		int postId = 1;

		assertEquals(0, repository.findByPostId(postId).size());

		postAndVerifyComment(postId, 1, OK);
		postAndVerifyComment(postId, 2, OK);
		postAndVerifyComment(postId, 3, OK);

		assertEquals(3, repository.findByPostId(postId).size());

		getAndVerifyCommentsByPostId(postId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].postId").isEqualTo(postId)
			.jsonPath("$[2].commentId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int postId = 1;
		int commentId = 1;

		assertEquals(0, repository.count());

		postAndVerifyComment(postId, commentId, OK)
			.jsonPath("$.postId").isEqualTo(postId)
			.jsonPath("$.commentId").isEqualTo(commentId);

		assertEquals(1, repository.count());

		postAndVerifyComment(postId, commentId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Duplicate key, Post Id: 1, Comment Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteComments() {

		int postId = 1;
		int commentId = 1;

		postAndVerifyComment(postId, commentId, OK);
		assertEquals(1, repository.findByPostId(postId).size());

		deleteAndVerifyCommentsByPostId(postId, OK);
		assertEquals(0, repository.findByPostId(postId).size());

		deleteAndVerifyCommentsByPostId(postId, OK);
	}

	@Test
	public void getCommentsMissingParameter() {

		getAndVerifyCommentsByPostId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Required int parameter 'postId' is not present");
	}

	@Test
	public void getCommentsInvalidParameter() {

		getAndVerifyCommentsByPostId("?postId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getCommentsNotFound() {

		getAndVerifyCommentsByPostId("?postId=213", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getCommentsInvalidParameterNegativeValue() {

		int postIdInvalid = -1;

		getAndVerifyCommentsByPostId("?postId=" + postIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Invalid postId: " + postIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyCommentsByPostId(int postId, HttpStatus expectedStatus) {
		return getAndVerifyCommentsByPostId("?postId=" + postId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyCommentsByPostId(String postIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/comment" + postIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyComment(int postId, int commentId, HttpStatus expectedStatus) {
		Comment comment = new Comment(postId, commentId, "Some comment text ", LocalDate.now(), "SA");
		return client.post()
			.uri("/comment")
			.body(just(comment), Comment.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyCommentsByPostId(int postId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/comment?postId=" + postId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}