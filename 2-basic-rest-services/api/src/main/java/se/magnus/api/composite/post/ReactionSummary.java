package se.magnus.api.composite.post;

public class ReactionSummary {
	private final int reactionId;
	private final String typeOfReaction;

	public ReactionSummary() {
		this.reactionId = 0;
		this.typeOfReaction = null;
	}

	public ReactionSummary(int reactionId, String typeOfReaction) {
		this.reactionId = reactionId;
		this.typeOfReaction = typeOfReaction;
	}

	public int getReactionId() {
		return reactionId;
	}

	public String getTypeOfReaction() {
		return typeOfReaction;
	}
	
	
}
