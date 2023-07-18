package se.magnus.microservices.core.comment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.comment.persistence.CommentEntity;
import se.magnus.microservices.core.comment.persistence.CommentRepository;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

    @Autowired
    private CommentRepository repository;

    private CommentEntity savedEntity;

    @Before
   	public void setupDb() {
   		repository.deleteAll();

        CommentEntity entity = new CommentEntity(1, 2, "Comment text", LocalDate.now());
        savedEntity = repository.save(entity);

        assertEqualsComment(entity, savedEntity);
    }


    @Test
   	public void create() {

        CommentEntity newEntity = new CommentEntity(1, 3, "Comment text", LocalDate.now());
        repository.save(newEntity);

        CommentEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsComment(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
   	public void update() {
        savedEntity.setCommentText("Updated comment text");
        repository.save(savedEntity);

        CommentEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("Updated comment text", foundEntity.getCommentText());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByPostId() {
        List<CommentEntity> entityList = repository.findByPostId(savedEntity.getPostId());

        assertThat(entityList, hasSize(1));
        assertEqualsComment(savedEntity, entityList.get(0));
    }

    @Test
   	public void duplicateError() throws DataIntegrityViolationException {
        try {
			CommentEntity entity = new CommentEntity(1, 2, "Comment text", LocalDate.now());
			repository.save(entity);
		} catch (DataIntegrityViolationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        CommentEntity entity1 = repository.findById(savedEntity.getId()).get();
        CommentEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setCommentText("Updated comment text for the first time");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            entity2.setCommentText("Updated comment text for the second time");
            repository.save(entity2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        CommentEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("Updated comment text for the first time", updatedEntity.getCommentText());
    }

    private void assertEqualsComment(CommentEntity expectedEntity, CommentEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getPostId(), actualEntity.getPostId());
        assertEquals(expectedEntity.getCommentId(),  actualEntity.getCommentId());
        assertEquals(expectedEntity.getCommentText(),    actualEntity.getCommentText());
        assertEquals(expectedEntity.getCommentText(),   actualEntity.getCommentText());
    }
}
