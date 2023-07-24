package se.magnus.microservices.composite.post.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.post.*;
import se.magnus.api.core.post.Post;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.image.Image;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;


import java.net.URL;
import java.io.Console;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PostCompositeServiceImpl implements PostCompositeService {

	private static final Logger LOG = LoggerFactory.getLogger(PostCompositeServiceImpl.class);

	private final SecurityContext nullSC = new SecurityContextImpl();

	private final ServiceUtil serviceUtil;
	private PostCompositeIntegration integration;

	@Autowired
	public PostCompositeServiceImpl(ServiceUtil serviceUtil, PostCompositeIntegration integration) {
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}

	@Override
	public Mono<Void> createCompositePost(PostAggregate body) {
		return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalCreateCompositePost(sc, body))
				.then();
	}

	public void internalCreateCompositePost(SecurityContext sc, PostAggregate body) {

		try {

			logAuthorizationInfo(sc);

			LOG.debug("createCompositePost: creates a new composite entity for postId: {}", body.getPostId());

			Post post = new Post(body.getPostId(), body.getTypeOfPost(), body.getPostCaption(), body.getPostedOn(),
					null);
			integration.createPost(post);

			if (body.getReactions() != null) {
				body.getReactions().forEach(r -> {
					Reaction reaction = new Reaction(body.getPostId(), r.getReactionId(), r.getTypeOfReaction(), null);
					integration.createReaction(reaction);
				});
			}

			if (body.getComments() != null) {
				body.getComments().forEach(r -> {
					Comment comment = new Comment(body.getPostId(), r.getCommentId(), r.getCommentText(),
							r.getCommentDate(), null);
					integration.createComment(comment);
				});
			}

			if (body.getImages() != null) {
				body.getImages().forEach(r -> {
					Image image = new Image(body.getPostId(), r.getImageId(), r.getImageUrl(), r.getUploadDate(), null);
					integration.createImage(image);
				});
			}

			LOG.debug("createCompositePost: composite entities created for postId: {}", body.getPostId());

		} catch (RuntimeException re) {
			LOG.warn("createCompositePost failed: {}", re.toString());
			throw re;
		}
	}

	@Override
	public Mono<PostAggregate> getCompositePost(int postId) {

		return Mono.zip(
				values -> createPostAggregate((SecurityContext) values[0], (Post) values[1], (List<Reaction>) values[2],
						(List<Comment>) values[3], (List<Image>) values[4], serviceUtil.getServiceAddress()),
				ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC), integration.getPost(postId),
				integration.getReactions(postId).collectList(), integration.getComments(postId).collectList(),
				integration.getImages(postId).collectList())
				.doOnError(ex -> LOG.warn("getCompositePost failed: {}", ex.toString())).log();
	}

	@Override
	public Mono<Void> deleteCompositePost(int postId) {
		return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositePost(sc, postId))
				.then();
	}

	private void internalDeleteCompositePost(SecurityContext sc, int postId) {
		try {
			logAuthorizationInfo(sc);

			LOG.debug("deleteCompositePost: Deletes a post aggregate for postId: {}", postId);

			integration.deletePost(postId);
			integration.deleteReactions(postId);
			integration.deleteComments(postId);
			integration.deleteImages(postId);

			LOG.debug("deleteCompositePost: aggregate entities deleted for postId: {}", postId);

		} catch (RuntimeException re) {
			LOG.warn("deleteCompositePost failed: {}", re.toString());
			throw re;
		}
	}

	private PostAggregate createPostAggregate(SecurityContext sc, Post post, List<Reaction> reactions,
			List<Comment> comments, List<Image> images, String serviceAddress) {

		logAuthorizationInfo(sc);

		// 1. Setup post info
		int postId = post.getPostId();
		String typeOfPost = post.getTypeOfPost();
		String postCaption = post.getPostCaption();
		LocalDate postedOn = post.getPostedOn();

		// 2. Copy summary reaction info, if available
		List<ReactionSummary> reactionSummaries = (reactions == null) ? null
				: reactions.stream().map(r -> new ReactionSummary(r.getReactionId(), r.getTypeOfReaction()))
						.collect(Collectors.toList());

		// 3. Copy summary comment info, if available
		List<CommentSummary> commentSummaries = (comments == null) ? null
				: comments.stream()
						.map(r -> new CommentSummary(r.getCommentId(), r.getCommentText(), r.getCommentDate()))
						.collect(Collectors.toList());

		// 4. Copy summary image info, if available
		List<ImageSummary> imageSummaries = (images == null) ? null
				: images.stream().map(r -> new ImageSummary(r.getImageId(), r.getImageUrl(), r.getUploadDate()))
						.collect(Collectors.toList());

		// 5. Create info regarding the involved microservices addresses
		String postAddress = post.getServiceAddress();
		String commentAddress = (comments != null && comments.size() > 0) ? comments.get(0).getServiceAddress() : "";
		String reactionAddress = (reactions != null && reactions.size() > 0) ? reactions.get(0).getServiceAddress()
				: "";
		String imageAddress = (images != null && images.size() > 0) ? images.get(0).getServiceAddress() : "";
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, postAddress, reactionAddress,
				commentAddress, imageAddress);

		return new PostAggregate(postId, typeOfPost, postCaption, postedOn, reactionSummaries, commentSummaries,
				imageSummaries, serviceAddresses);
	}

	private void logAuthorizationInfo(SecurityContext sc) {
		if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
			Jwt jwtToken = ((JwtAuthenticationToken) sc.getAuthentication()).getToken();
			logAuthorizationInfo(jwtToken);
		} else {
			LOG.warn("No JWT based Authentication supplied, running tests are we?");
		}
	}

	private void logAuthorizationInfo(Jwt jwt) {
		if (jwt == null) {
			LOG.warn("No JWT supplied, running tests are we?");
		} else {
			if (LOG.isDebugEnabled()) {
				URL issuer = jwt.getIssuer();
				List<String> audience = jwt.getAudience();
				Object subject = jwt.getClaims().get("sub");
				Object scopes = jwt.getClaims().get("scope");
				Object expires = jwt.getClaims().get("exp");

				LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject,
						scopes, expires, issuer, audience);
			}
		}
	}
}