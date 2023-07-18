package se.magnus.microservices.core.reaction.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ReactionRepository extends ReactiveCrudRepository<ReactionEntity, String> {
    Flux<ReactionEntity> findByPostId(int postId);
}
