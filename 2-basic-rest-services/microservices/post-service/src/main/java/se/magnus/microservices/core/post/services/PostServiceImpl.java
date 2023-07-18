package se.magnus.microservices.core.post.services;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.core.post.Post;
import se.magnus.api.core.post.PostService;
import se.magnus.microservices.core.post.persistence.PostEntity;
import se.magnus.microservices.core.post.persistence.PostRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import static reactor.core.publisher.Mono.error;

@RestController
public class PostServiceImpl implements PostService {

	private static final Logger LOG = LoggerFactory.getLogger(PostServiceImpl.class);

    private final ServiceUtil serviceUtil;

    private final PostRepository repository;

    private final PostMapper mapper;

    @Autowired
    public PostServiceImpl(PostRepository repository, PostMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Post createPost(Post body) {

        if (body.getPostId() < 1) throw new InvalidInputException("Invalid postId: " + body.getPostId());

        PostEntity entity = mapper.apiToEntity(body);
        Mono<Post> newEntity = repository.save(entity)
            .log()
            .onErrorMap(
                DuplicateKeyException.class,
                ex -> new InvalidInputException("Duplicate key, Post Id: " + body.getPostId()))
            .map(e -> mapper.entityToApi(e));

        return newEntity.block();
    }

    @Override
    public Mono<Post> getPost(int postId) {

        if (postId < 1) throw new InvalidInputException("Invalid postId: " + postId);

        return repository.findByPostId(postId)
            .switchIfEmpty(error(new NotFoundException("No post found for postId: " + postId)))
            .log()
            .map(e -> mapper.entityToApi(e))
            .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public void deletePost(int postId) {

        if (postId < 1) throw new InvalidInputException("Invalid postId: " + postId);

        LOG.debug("deletePost: tries to delete an entity with postId: {}", postId);
        repository.findByPostId(postId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();
    }
}