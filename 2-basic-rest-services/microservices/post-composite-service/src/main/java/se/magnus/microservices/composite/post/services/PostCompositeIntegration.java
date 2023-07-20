package se.magnus.microservices.composite.post.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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

import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@EnableBinding(PostCompositeIntegration.MessageSources.class)
@Component
public class PostCompositeIntegration implements PostService, ReactionService, CommentService, ImageService {

	private static final Logger LOG = LoggerFactory.getLogger(PostCompositeIntegration.class);

	private final WebClient webClient;
	private final ObjectMapper mapper;

	private final String postServiceUrl;
	private final String reactionServiceUrl;
	private final String commentServiceUrl;
	private final String imageServiceUrl;

	private MessageSources messageSources;

	public interface MessageSources {

		String OUTPUT_PRODUCTS = "output-posts";
		String OUTPUT_RECOMMENDATIONS = "output-reactions";
		String OUTPUT_REVIEWS = "output-comments";
		String OUTPUT_IMAGES = "output-images";

		@Output(OUTPUT_PRODUCTS)
		MessageChannel outputPosts();

		@Output(OUTPUT_RECOMMENDATIONS)
		MessageChannel outputReactions();

		@Output(OUTPUT_REVIEWS)
		MessageChannel outputComments();

		@Output(OUTPUT_IMAGES)
		MessageChannel outputImages();
	}

	@Autowired
	public PostCompositeIntegration(WebClient.Builder webClient, ObjectMapper mapper, MessageSources messageSources,

			@Value("${app.post-service.host}") String postServiceHost,
			@Value("${app.post-service.port}") int postServicePort,

			@Value("${app.reaction-service.host}") String reactionServiceHost,
			@Value("${app.reaction-service.port}") int reactionServicePort,

			@Value("${app.comment-service.host}") String commentServiceHost,
			@Value("${app.comment-service.port}") int commentServicePort,

			@Value("${app.image-service.host}") String imageServiceHost,
			@Value("${app.image-service.port}") int imageServicePort) {

		this.webClient = webClient.build();
		this.mapper = mapper;
		this.messageSources = messageSources;

		postServiceUrl = "http://" + postServiceHost + ":" + postServicePort;
		reactionServiceUrl = "http://" + reactionServiceHost + ":" + reactionServicePort;
		commentServiceUrl = "http://" + commentServiceHost + ":" + commentServicePort;
		imageServiceUrl = "http://" + imageServiceHost + ":" + imageServicePort;
	}

	@Override
	public Post createPost(Post body) {
		messageSources.outputPosts()
				.send(MessageBuilder.withPayload(new Event(CREATE, body.getPostId(), body)).build());
		return body;
	}

	@Override
	public Mono<Post> getPost(int postId) {
		String url = postServiceUrl + "/post/" + postId;
		LOG.debug("Will call the getPost API on URL: {}", url);

		return webClient.get().uri(url).retrieve().bodyToMono(Post.class).log()
				.onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
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
		return webClient.get().uri(url).retrieve().bodyToFlux(Reaction.class).log().onErrorResume(error -> empty());
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
		return webClient.get().uri(url).retrieve().bodyToFlux(Comment.class).log().onErrorResume(error -> empty());

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
		return webClient.get().uri(url).retrieve().bodyToFlux(Image.class).log().onErrorResume(error -> empty());

	}

	@Override
	public void deleteImages(int postId) {
		messageSources.outputImages().send(MessageBuilder.withPayload(new Event(DELETE, postId, null)).build());
	}

	public Mono<Health> getPostHealth() {
		return getHealth(postServiceUrl);
	}

	public Mono<Health> getReactionHealth() {
		return getHealth(reactionServiceUrl);
	}

	public Mono<Health> getCommentHealth() {
		return getHealth(commentServiceUrl);
	}

	public Mono<Health> getImageHealth() {
		return getHealth(imageServiceUrl);
	}

	private Mono<Health> getHealth(String url) {
		url += "/actuator/health";
		LOG.debug("Will call the Health API on URL: {}", url);
		return webClient.get().uri(url).retrieve().bodyToMono(String.class).map(s -> new Health.Builder().up().build())
				.onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build())).log();
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
