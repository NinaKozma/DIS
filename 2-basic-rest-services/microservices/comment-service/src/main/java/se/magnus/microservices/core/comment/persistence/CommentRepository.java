package se.magnus.microservices.core.comment.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends CrudRepository<CommentEntity, Integer> {

    @Transactional(readOnly = true)
    List<CommentEntity> findByPostId(int postId);
}