package se.magnus.microservices.core.reaction.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="reactions")
@CompoundIndex(name = "post-rea-id", unique = true, def = "{'postId': 1, 'reactionId' : 1}")
public class ReactionEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private int postId;
    private int reactionId;
    private String typeOfReaction;

    public ReactionEntity() {
    }

    public ReactionEntity(int postId, int reactionId, String typeOfReaction) {
        this.postId = postId;
        this.reactionId = reactionId;
        this.typeOfReaction = typeOfReaction;
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

    public int getReactionId() {
        return reactionId;
    }

    public String getTypeOfReaction() {
        return typeOfReaction;
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

    public void setReactionId(int reactionId) {
        this.reactionId = reactionId;
    }

    public void setTypeOfReaction(String typeOfReaction) {
        this.typeOfReaction = typeOfReaction;
    }
}