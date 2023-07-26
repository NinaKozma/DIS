package se.magnus.microservices.core.reaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import se.magnus.microservices.core.reaction.persistence.*;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
public class PersistenceTests {

	@Autowired
	private ReactionRepository repository;

	private ReactionEntity savedEntity;

	@Before
   	public void setupDb() {
   		repository.deleteAll().block();

        ReactionEntity entity = new ReactionEntity(1, 2, "Type of reaction");
        savedEntity = repository.save(entity).block();

        assertEqualsReaction(entity, savedEntity);
    }


    @Test
   	public void create() {

        ReactionEntity newEntity = new ReactionEntity(1, 3, "Type of reaction");
        repository.save(newEntity).block();

        ReactionEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsReaction(newEntity, foundEntity);

        assertEquals(2, (long)repository.count().block());
    }

    @Test
   	public void update() {
        savedEntity.setTypeOfReaction("Type of reaction updated");
        repository.save(savedEntity).block();

        ReactionEntity foundEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("Type of reaction updated", foundEntity.getTypeOfReaction());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity).block();
        assertFalse(repository.existsById(savedEntity.getId()).block());
    }

    @Test
   	public void getByPostId() {
        List<ReactionEntity> entityList = repository.findByPostId(savedEntity.getPostId()).collectList().block();

        assertThat(entityList, hasSize(1));
        assertEqualsReaction(savedEntity, entityList.get(0));
    }

    @Test(expected = DuplicateKeyException.class)
   	public void duplicateError() {
        ReactionEntity entity = new ReactionEntity(1, 2, "Type of reaction");
        repository.save(entity).block();
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ReactionEntity entity1 = repository.findById(savedEntity.getId()).block();
        ReactionEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setTypeOfReaction("Type of reaction updated");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
        	entity2.setTypeOfReaction("Type of reaction updated again...");
            repository.save(entity2).block();

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {}

        // Get the updated entity from the database and verify its new sate
        ReactionEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("Type of reaction updated", updatedEntity.getTypeOfReaction());
    }

    private void assertEqualsReaction(ReactionEntity expectedEntity, ReactionEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getPostId(),        actualEntity.getPostId());
        assertEquals(expectedEntity.getReactionId(), actualEntity.getReactionId());
        assertEquals(expectedEntity.getTypeOfReaction(),           actualEntity.getTypeOfReaction());
    }
}