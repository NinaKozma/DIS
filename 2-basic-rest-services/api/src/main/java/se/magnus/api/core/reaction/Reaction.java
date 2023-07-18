package se.magnus.api.core.reaction;


public class Reaction {
	private int postId;
	private int reactionId;
	private String typeOfReaction;
	private String serviceAddress;

	public Reaction() {
		this.postId = 0;
		this.reactionId = 0;
		this.typeOfReaction = null;
		this.serviceAddress = null;
	}

	public Reaction(int postId, int reactionId, String typeOfReaction, String serviceAddress) {
		this.postId = postId;
		this.reactionId = reactionId;
		this.typeOfReaction = typeOfReaction;
		this.serviceAddress = serviceAddress;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public int getReactionId() {
		return reactionId;
	}

	public void setReactionId(int reactionId) {
		this.reactionId = reactionId;
	}

	public String getTypeOfReaction() {
		return typeOfReaction;
	}

	public void setTypeOfReaction(String typeOfReaction) {
		this.typeOfReaction = typeOfReaction;
	}
	
	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

}
