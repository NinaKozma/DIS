package se.magnus.api.core.post;

import java.time.LocalDate;


public class Post {
	private int postId;
	private String typeOfPost;
	private String postCaption;
	private LocalDate postedOn;
	private String serviceAddress;

	public Post() {
		postId = 0;
		typeOfPost = null;
		postCaption = null;
		postedOn = null;
		serviceAddress = null;
	}

	public Post(int postId, String typeOfPost, String postCaption, LocalDate postedOn,
			String serviceAddress) {
		this.postId = postId;
		this.typeOfPost = typeOfPost;
		this.postCaption = postCaption;
		this.postedOn = postedOn;
		this.serviceAddress = serviceAddress;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public String getTypeOfPost() {
		return typeOfPost;
	}

	public void setTypeOfPost(String typeOfPost) {
		this.typeOfPost = typeOfPost;
	}

	public String getPostCaption() {
		return postCaption;
	}

	public void setPostCaption(String postCaption) {
		this.postCaption = postCaption;
	}

	public LocalDate getPostedOn() {
		return postedOn;
	}

	public void setPostedOn(LocalDate postedOn) {
		this.postedOn = postedOn;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	

}
