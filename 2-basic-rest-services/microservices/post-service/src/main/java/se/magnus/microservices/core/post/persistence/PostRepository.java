package se.magnus.microservices.core.post.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PostRepository extends ReactiveCrudRepository<PostEntity, String> {
    Mono<PostEntity> findByPostId(int postId); //optional zato sto mozda nema objave sa tim ID-em
}
