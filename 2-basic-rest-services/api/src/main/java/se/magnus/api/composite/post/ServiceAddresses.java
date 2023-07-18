package se.magnus.api.composite.post;

public class ServiceAddresses {
	private final String cmp;
	private final String post;
	private final String reaction;
	private final String comment;
	private final String image;

	public ServiceAddresses() {
		cmp = null;
		post = null;
		reaction = null;
		comment = null;
		image = null;
	}

	public ServiceAddresses(String compositeAddress, String postAddress, String reactionAddress, String commentAddress,
			String imageAddress) {
		this.cmp = compositeAddress;
		this.post = postAddress;
		this.reaction = reactionAddress;
		this.comment = commentAddress;
		this.image = imageAddress;
	}

	public String getCmp() {
		return cmp;
	}

	public String getPost() {
		return post;
	}

	public String getReaction() {
		return reaction;
	}

	public String getComment() {
		return comment;
	}

	public String getImage() {
		return image;
	}

}
