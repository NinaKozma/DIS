package se.magnus.microservices.core.reaction.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.reaction.Reaction;
import se.magnus.api.core.reaction.ReactionService;
import se.magnus.microservices.core.reaction.persistence.ReactionEntity;
import se.magnus.microservices.core.reaction.persistence.ReactionRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ReactionServiceImpl implements ReactionService {

	private static final Logger LOG = LoggerFactory.getLogger(ReactionServiceImpl.class);

	private final ReactionRepository repository;

	private final ReactionMapper mapper;

	private final ServiceUtil serviceUtil;

	@Autowired
	public ReactionServiceImpl(ReactionRepository repository, ReactionMapper mapper, ServiceUtil serviceUtil) {
		this.repository = repository;
		this.mapper = mapper;
		this.serviceUtil = serviceUtil;
	}

	@Override
	public Reaction createReaction(Reaction body) {

		if (body.getPostId() < 1)
			throw new InvalidInputException("Invalid postId: " + body.getPostId());

		ReactionEntity entity = mapper.apiToEntity(body);
		Mono<Reaction> newEntity = repository.save(entity).log()
				.onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException(
						"Duplicate key, Post Id: " + body.getPostId() + ", Reaction Id:" + body.getReactionId()))
				.map(e -> mapper.entityToApi(e));

		return newEntity.block();
	}

	@Override
	public Flux<Reaction> getReactions(int postId) {

		if (postId < 1)
			throw new InvalidInputException("Invalid postId: " + postId);

		return repository.findByPostId(postId).log().map(e -> mapper.entityToApi(e)).map(e -> {
			e.setServiceAddress(serviceUtil.getServiceAddress());
			return e;
		});
	}

	@Override
	public void deleteReactions(int postId) {

		if (postId < 1)
			throw new InvalidInputException("Invalid postId: " + postId);

		LOG.debug("deleteReactions: tries to delete reactions for the post with postId: {}", postId);
		repository.deleteAll(repository.findByPostId(postId)).block();
	}
}
