package se.magnus.microservices.core.reaction.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.api.core.reaction.ReactionService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final ReactionService reactionService;

    @Autowired
    public MessageProcessor(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Reaction> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Reaction reaction = event.getData();
            LOG.info("Create reaction with ID: {}/{}", reaction.getPostId(), reaction.getReactionId());
            reactionService.createReaction(reaction);
            break;

        case DELETE:
            int postId = event.getKey();
            LOG.info("Delete reactions with PostID: {}", postId);
            reactionService.deleteReactions(postId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
