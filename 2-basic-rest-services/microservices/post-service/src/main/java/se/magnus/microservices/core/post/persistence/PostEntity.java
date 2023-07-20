package se.magnus.microservices.core.post.persistence;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection="posts")
public class PostEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int postId;

    private String typeOfPost;
    private String postCaption;
    private LocalDate postedOn;

    public PostEntity() {
    }

    public PostEntity(int postId, String typeOfPost, String postCaption, LocalDate postedOn) {
        this.postId = postId;
        this.typeOfPost = typeOfPost;
        this.postCaption = postCaption;
        this.postedOn = postedOn;
    }

    public String getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public void setTypeOfPost(String typeOfPost) {
        this.typeOfPost = typeOfPost;
    }
    
    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }
    
    public void setPostedOn(LocalDate postedOn) {
        this.postedOn = postedOn;
    }
}