package se.magnus.microservices.core.comment.services;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.microservices.core.comment.persistence.CommentEntity;
import se.magnus.microservices.core.comment.persistence.CommentRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

import java.util.List;
import java.util.function.Supplier;

import static java.util.logging.Level.FINE;

@RestController
public class CommentServiceImpl implements CommentService {

    private static final Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository repository;

    private final CommentMapper mapper;

    private final ServiceUtil serviceUtil;

    private final Scheduler scheduler;

    @Autowired
    public CommentServiceImpl(Scheduler scheduler, CommentRepository repository, CommentMapper mapper, ServiceUtil serviceUtil) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

	@Override
	public Comment createComment(Comment body) {
		try {
			CommentEntity entity = mapper.apiToEntity(body);
			CommentEntity newEntity = repository.save(entity);

			LOG.debug("createComment: created a comment entity: {}/{}", body.getPostId(), body.getCommentId());
			return mapper.entityToApi(newEntity);

		} catch (DataIntegrityViolationException dive) {
			throw new InvalidInputException(
					"Duplicate key, Post Id: " + body.getPostId() + ", Comment Id:" + body.getCommentId());
		}
	}
	
	@Override
    public Flux<Comment> getComments(int postId) {

        if (postId < 1) throw new InvalidInputException("Invalid postId: " + postId);

        LOG.info("Will get comments for post with id={}", postId);

        return asyncFlux(() -> Flux.fromIterable(getByPostId(postId))).log(null, FINE);
    }

	protected List<Comment> getByPostId(int postId) {

        List<CommentEntity> entityList = repository.findByPostId(postId);
        List<Comment> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getComments: response size: {}", list.size());

        return list;
    }

	@Override
	public void deleteComments(int postId) {
		LOG.debug("deleteComments: tries to delete comments for the post with postId: {}", postId);
		repository.deleteAll(repository.findByPostId(postId));
	}
	
	private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler);
    }
}
