package se.magnus.microservices.core.post;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import se.magnus.microservices.core.post.persistence.*;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

	@Autowired
	private PostRepository repository;

	private PostEntity savedEntity;

	@Before
   	public void setupDb() {
   		repository.deleteAll().block();

        PostEntity entity = new PostEntity(1, "Type of post", "Post Caption", LocalDate.now());
        savedEntity = repository.save(entity).block();

        assertEqualsPost(entity, savedEntity);
    }


    @Test
   	public void create() {

        PostEntity newEntity = new PostEntity(2, "Type of post", "Post Caption", LocalDate.now());
        repository.save(newEntity).block();

        PostEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsPost(newEntity, foundEntity);

        assertEquals(2, (long)repository.count().block());
    }

    @Test
   	public void update() {
        savedEntity.setTypeOfPost("Type of post updated");
        repository.save(savedEntity).block();

        PostEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("Type of post updated", foundEntity.getTypeOfPost());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
   	public void getByPostId() {
        PostEntity foundEntity = repository.findByPostId(savedEntity.getPostId()).block();
        
        assertEqualsPost(savedEntity, foundEntity);
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
        PostEntity entity = new PostEntity(1, "Type of post", "Post Caption", LocalDate.now());
        repository.save(entity).block();
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        PostEntity entity1 = repository.findById(savedEntity.getId()).block();
        PostEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setTypeOfPost("Type of post updated");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
        	entity2.setTypeOfPost("Type of post updated again...");
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        PostEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("Type of post updated", updatedEntity.getTypeOfPost());
    }

    private void assertEqualsPost(PostEntity expectedEntity, PostEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getPostId(),        actualEntity.getPostId());
        assertEquals(expectedEntity.getTypeOfPost(), actualEntity.getTypeOfPost());
        assertEquals(expectedEntity.getPostCaption(),           actualEntity.getPostCaption());
        assertEquals(expectedEntity.getPostedOn(),           actualEntity.getPostedOn());
    }
}