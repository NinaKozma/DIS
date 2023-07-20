package se.magnus.api.composite.post;

import java.time.LocalDate;
import java.util.List;

public class PostAggregate {
	private final int postId;
	private final String typeOfPost;
	private final String postCaption;
	private final LocalDate postedOn;
	private final List<ReactionSummary> reactions;
	private final List<CommentSummary> comments;
	private final List<ImageSummary> images;
	private final ServiceAddresses serviceAddresses;

	public PostAggregate() {
		postId = 0;
		typeOfPost = null;
		postCaption = null;
		postedOn = null;
		reactions = null;
		comments = null;
		images = null;
		serviceAddresses = null;
	}

	public PostAggregate(int postId, String typeOfPost, String postCaption, LocalDate postedOn,
			List<ReactionSummary> reactions, List<CommentSummary> comments, List<ImageSummary> images,
			ServiceAddresses serviceAddresses) {

		this.postId = postId;
		this.typeOfPost = typeOfPost;
		this.postCaption = postCaption;
		this.postedOn = postedOn;
		this.reactions = reactions;
		this.comments = comments;
		this.images = images;
		this.serviceAddresses = serviceAddresses;
	}

	public int getPostId() {
		return postId;
	}

	public String getTypeOfPost() {
		return typeOfPost;
	}

	public String getPostCaption() {
		return postCaption;
	}

	public LocalDate getPostedOn() {
		return postedOn;
	}

	public List<ReactionSummary> getReactions() {
		return reactions;
	}

	public List<CommentSummary> getComments() {
		return comments;
	}

	public List<ImageSummary> getImages() {
		return images;
	}

	public ServiceAddresses getServiceAddresses() {
		return serviceAddresses;
	}

}