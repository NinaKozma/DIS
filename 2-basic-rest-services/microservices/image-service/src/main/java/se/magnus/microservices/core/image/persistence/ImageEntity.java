package se.magnus.microservices.core.image.persistence;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@Table(name = "images", indexes = { @Index(name = "images_unique_idx", unique = true, columnList = "postId,imageId") })
public class ImageEntity {

    @Id @GeneratedValue
    private int id;

    @Version
    private int version;

    private int postId;
    private int imageId;
    private String imageUrl;
	private LocalDate uploadDate;

    public ImageEntity() {
    }

    public ImageEntity(int postId, int imageId, String imageUrl, LocalDate uploadDate) {
		this.postId = postId;
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.uploadDate = uploadDate;
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

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDate getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDate uploadDate) {
        this.uploadDate = uploadDate;
    }
}