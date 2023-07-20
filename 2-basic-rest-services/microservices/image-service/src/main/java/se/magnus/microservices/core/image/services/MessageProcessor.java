package se.magnus.microservices.core.image.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.image.Image;
import se.magnus.api.core.image.ImageService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final ImageService imageService;

    @Autowired
    public MessageProcessor(ImageService imageService) {
        this.imageService = imageService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Image> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

        case CREATE:
            Image image = event.getData();
            LOG.info("Create image with ID: {}/{}", image.getPostId(), image.getImageId());
            imageService.createImage(image);
            break;

        case DELETE:
            int postId = event.getKey();
            LOG.info("Delete images with PostID: {}", postId);
            imageService.deleteImages(postId);
            break;

        default:
            String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
            LOG.warn(errorMessage);
            throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}