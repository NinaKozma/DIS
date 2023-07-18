package se.magnus.api.composite.post;

import java.time.LocalDate;

public class ImageSummary {
	private final int imageId;
	private final String imageUrl;
	private final LocalDate uploadDate;

	public ImageSummary() {
		this.imageId = 0;
		this.imageUrl = null;
		this.uploadDate = null;
	}

	public ImageSummary(int imageId, String imageUrl, LocalDate uploadDate) {
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.uploadDate = uploadDate;
	}

	public int getImageId() {
		return imageId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public LocalDate getUploadDate() {
		return uploadDate;
	}

}
