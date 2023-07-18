package se.magnus.api.core.image;

import java.time.LocalDate;


public class Image {
	private int postId;
	private int imageId;
	private String imageUrl;
	private LocalDate uploadDate;
	private String serviceAddress;

	public Image() {
		this.postId = 0;
		this.imageId = 0;
		this.imageUrl = null;
		this.uploadDate = null;
		this.serviceAddress = null;
	}

	public Image(int postId, int imageId, String imageUrl, LocalDate uploadDate, String serviceAddress) {
		this.postId = postId;
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.uploadDate = uploadDate;
		this.serviceAddress = serviceAddress;
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

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

}
