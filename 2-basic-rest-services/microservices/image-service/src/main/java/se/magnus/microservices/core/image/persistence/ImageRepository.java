package se.magnus.microservices.core.image.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ImageRepository extends CrudRepository<ImageEntity, Integer> {

    @Transactional(readOnly = true)
    List<ImageEntity> findByPostId(int postId);
}
