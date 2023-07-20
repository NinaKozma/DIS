package se.magnus.microservices.core.post.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.post.*;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final PostService postService;

    @Autowired
    public MessageProcessor(PostService postService) {
        this.postService = postService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Post> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Post post = event.getData();
            LOG.info("Create post with ID: {}", post.getPostId());
            postService.createPost(post);
            break;

        case DELETE:
            int postId = event.getKey();
            LOG.info("Delete post with PostID: {}", postId);
            postService.deletePost(postId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
