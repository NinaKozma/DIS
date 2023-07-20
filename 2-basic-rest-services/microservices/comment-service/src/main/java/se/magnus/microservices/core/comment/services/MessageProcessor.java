package se.magnus.microservices.core.comment.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final CommentService commentService;

    @Autowired
    public MessageProcessor(CommentService commentService) {
        this.commentService = commentService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Comment> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Comment comment = event.getData();
            LOG.info("Create comment with ID: {}/{}", comment.getPostId(), comment.getCommentId());
            commentService.createComment(comment);
            break;

        case DELETE:
            int postId = event.getKey();
            LOG.info("Delete comments with PostID: {}", postId);
            commentService.deleteComments(postId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}