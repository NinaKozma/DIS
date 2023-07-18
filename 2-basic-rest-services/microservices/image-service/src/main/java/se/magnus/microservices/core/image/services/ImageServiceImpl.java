package se.magnus.microservices.core.image.services;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.image.Image;
import se.magnus.api.core.image.ImageService;
import se.magnus.microservices.core.image.persistence.ImageEntity;
import se.magnus.microservices.core.image.persistence.ImageRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;
import java.util.function.Supplier;

import static java.util.logging.Level.FINE;

@RestController
public class ImageServiceImpl implements ImageService {

    private static final Logger LOG = LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageRepository repository;

    private final ImageMapper mapper;

    private final ServiceUtil serviceUtil;

    private final Scheduler scheduler;

    @Autowired
    public ImageServiceImpl(Scheduler scheduler, ImageRepository repository, ImageMapper mapper, ServiceUtil serviceUtil) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Image createImage(Image body) {
        try {
            ImageEntity entity = mapper.apiToEntity(body);
            ImageEntity newEntity = repository.save(entity);

            LOG.debug("createImage: created a image entity: {}/{}", body.getPostId(), body.getImageId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Post Id: " + body.getPostId() + ", Image Id:" + body.getImageId());
        }
    }

    @Override
    public Flux<Image> getImages(int postId) {

        if (postId < 1) throw new InvalidInputException("Invalid postId: " + postId);

        LOG.info("Will get images for post with id={}", postId);

        return asyncFlux(() -> Flux.fromIterable(getByPostId(postId))).log(null, FINE);
    }

	protected List<Image> getByPostId(int postId) {

        List<ImageEntity> entityList = repository.findByPostId(postId);
        List<Image> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getImages: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteImages(int postId) {
        LOG.debug("deleteImages: tries to delete images for the post with postId: {}", postId);
        repository.deleteAll(repository.findByPostId(postId));
    }
    
    private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}
