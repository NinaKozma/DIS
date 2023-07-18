package se.magnus.api.composite.post;

import java.time.LocalDate;

public class CommentSummary {
	private final int commentId;
	private final String commentText;
	private final LocalDate commentDate;

	public CommentSummary() {
		this.commentId = 0;
		this.commentText = null;
		this.commentDate = null;
	}

	public CommentSummary(int commentId, String commentText, LocalDate commentDate) {
		this.commentId = commentId;
		this.commentText = commentText;
		this.commentDate = commentDate;
	}

	public int getCommentId() {
		return commentId;
	}

	public String getCommentText() {
		return commentText;
	}

	public LocalDate getCommentDate() {
		return commentDate;
	}

}
