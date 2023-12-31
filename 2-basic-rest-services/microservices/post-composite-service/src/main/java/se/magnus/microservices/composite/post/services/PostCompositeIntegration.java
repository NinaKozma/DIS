package se.magnus.microservices.composite.post.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.post.Post;
import se.magnus.api.core.post.PostService;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.api.core.reaction.ReactionService;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.api.core.image.Image;
import se.magnus.api.core.image.ImageService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@EnableBinding(PostCompositeIntegration.MessageSources.class)
@Component
public class PostCompositeIntegration implements PostService, ReactionService, CommentService, ImageService {

	private static final Logger LOG = LoggerFactory.getLogger(PostCompositeIntegration.class);

	private WebClient webClient;
	private final ObjectMapper mapper;

	private final WebClient.Builder webClientBuilder;

	private final String postServiceUrl = "http://post";;
	private final String reactionServiceUrl = "http://reaction";;
	private final String commentServiceUrl = "http://comment";;
	private final String imageServiceUrl = "http://image";;

	private MessageSources messageSources;

	private final int postServiceTimeoutSec;

	public interface MessageSources {

		String OUTPUT_POSTS = "output-posts";
		String OUTPUT_REACTIONS = "output-reactions";
		String OUTPUT_COMMENTS = "output-comments";
		String OUTPUT_IMAGES = "output-images";

		@Output(OUTPUT_POSTS)
		MessageChannel outputPosts();

		@Output(OUTPUT_REACTIONS)
		MessageChannel outputReactions();

		@Output(OUTPUT_COMMENTS)
		MessageChannel outputComments();

		@Output(OUTPUT_IMAGES)
		MessageChannel outputImages();
	}

	@Autowired
	public PostCompositeIntegration(WebClient.Builder webClientBuilder, ObjectMapper mapper,
			MessageSources messageSources, @Value("${app.post-service.timeoutSec}") int postServiceTimeoutSec) {
		this.webClientBuilder = webClientBuilder;
		this.mapper = mapper;
		this.messageSources = messageSources;
		this.postServiceTimeoutSec = postServiceTimeoutSec;
	}

	@Override
	public Post createPost(Post body) {
		messageSources.outputPosts()
				.send(MessageBuilder.withPayload(new Event(CREATE, body.getPostId(), body)).build());
		return body;
	}

	@Retry(name = "post")
	@CircuitBreaker(name = "post")
	public Mono<Post> getPost(int postId, int delay, int faultPercent) {

		URI url = UriComponentsBuilder
				.fromUriString(postServiceUrl + "/post/{postId}?delay={delay}&faultPercent={faultPercent}")
				.build(postId, delay, faultPercent);
		LOG.debug("Will call the getPost API on URL: {}", url);

		return getWebClient().get().uri(url).retrieve().bodyToMono(Post.class).log()
				.onErrorMap(WebClientResponseException.class, ex -> handleException(ex))
				.timeout(Duration.ofSeconds(postServiceTimeoutSec));
	}

	@Override
	public void deletePost(int postId) {
		messageSources.outputPosts().send(MessageBuilder.withPayload(new Event(DELETE, postId, null)).build());
	}

	@Override
	public Reaction createReaction(Reaction body) {
		messageSources.outputReactions()
				.send(MessageBuilder.withPayload(new Event(CREATE, body.getPostId(), body)).build());
		return body;
	}

	@Override
	public Flux<Reaction> getReactions(int postId) {

		String url = reactionServiceUrl + "/reaction?postId=" + postId;

		LOG.debug("Will call the getReactions API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the
		// composite service to return partial responses
		return getWebClient().get().uri(url).retrieve().bodyToFlux(Reaction.class).log()
				.onErrorResume(error -> empty());
	}

	@Override
	public void deleteReactions(int postId) {
		messageSources.outputReactions().send(MessageBuilder.withPayload(new Event(DELETE, postId, null)).build());
	}

	@Override
	public Comment createComment(Comment body) {
		messageSources.outputComments()
				.send(MessageBuilder.withPayload(new Event(CREATE, body.getPostId(), body)).build());
		return body;
	}

	@Override
	public Flux<Comment> getComments(int postId) {

		String url = commentServiceUrl + "/comment?postId=" + postId;

		LOG.debug("Will call the getComments API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the
		// composite service to return partial responses
		return getWebClient().get().uri(url).retrieve().bodyToFlux(Comment.class).log().onErrorResume(error -> empty());

	}

	@Override
	public void deleteComments(int postId) {
		messageSources.outputComments().send(MessageBuilder.withPayload(new Event(DELETE, postId, null)).build());
	}

	@Override
	public Image createImage(Image body) {
		messageSources.outputImages()
				.send(MessageBuilder.withPayload(new Event(CREATE, body.getPostId(), body)).build());
		return body;
	}

	@Override
	public Flux<Image> getImages(int postId) {

		String url = imageServiceUrl + "/image?postId=" + postId;

		LOG.debug("Will call the getImages API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the
		// composite service to return partial responses
		return getWebClient().get().uri(url).retrieve().bodyToFlux(Image.class).log().onErrorResume(error -> empty());

	}

	@Override
	public void deleteImages(int postId) {
		messageSources.outputImages().send(MessageBuilder.withPayload(new Event(DELETE, postId, null)).build());
	}

	private WebClient getWebClient() {
		if (webClient == null) {
			webClient = webClientBuilder.build();
		}
		return webClient;
	}

	private Throwable handleException(Throwable ex) {

		if (!(ex instanceof WebClientResponseException)) {
			LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
			return ex;
		}

		WebClientResponseException wcre = (WebClientResponseException) ex;

		switch (wcre.getStatusCode()) {

		case NOT_FOUND:
			return new NotFoundException(getErrorMessage(wcre));

		case UNPROCESSABLE_ENTITY:
			return new InvalidInputException(getErrorMessage(wcre));

		default:
			LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
			LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
			return ex;
		}
	}

	private String getErrorMessage(WebClientResponseException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}
}
