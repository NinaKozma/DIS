package se.magnus.microservices.core.comment.persistence;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@Table(name = "comments", indexes = { @Index(name = "comments_unique_idx", unique = true, columnList = "postId,commentId") })
public class CommentEntity {

    @Id @GeneratedValue
    private int id;

    @Version
    private int version;

    private int postId;
    private int commentId;
    private String commentText;
	private LocalDate commentDate;

    public CommentEntity() {
    }

    public CommentEntity(int postId, int commentId, String commentText, LocalDate commentDate) {
		this.postId = postId;
		this.commentId = commentId;
		this.commentText = commentText;
		this.commentDate = commentDate;
	}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
}