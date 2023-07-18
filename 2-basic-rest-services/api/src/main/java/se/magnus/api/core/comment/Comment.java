package se.magnus.api.core.comment;

import java.time.LocalDate;


public class Comment {
	private int postId;
	private int commentId;
	private String commentText;
	private LocalDate commentDate;
	private String serviceAddress;

	public Comment() {
		this.postId = 0;
		this.commentId = 0;
		this.commentText = null;
		this.commentDate = null;
		this.serviceAddress = null;
	}

	public Comment(int postId, int commentId, String commentText, LocalDate commentDate, String serviceAddress) {
		this.postId = postId;
		this.commentId = commentId;
		this.commentText = commentText;
		this.commentDate = commentDate;
		this.serviceAddress = serviceAddress;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public int getCommentId() {
		return commentId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}

	public String getCommentText() {
		return commentText;
	}

	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

	public LocalDate getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(LocalDate commentDate) {
		this.commentDate = commentDate;
	}
	
	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

}
