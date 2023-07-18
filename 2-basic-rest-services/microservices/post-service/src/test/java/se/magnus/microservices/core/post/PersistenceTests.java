package se.magnus.microservices.core.post;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.post.persistence.PostEntity;
import se.magnus.microservices.core.post.persistence.PostRepository;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PersistenceTests {

	@Autowired
	private PostRepository repository;

	private PostEntity savedEntity;

	@Before
	public void setupDb() {
		StepVerifier.create(repository.deleteAll()).verifyComplete();

		PostEntity entity = new PostEntity(1, "Type of post", "Post caption", LocalDate.now());
		StepVerifier.create(repository.save(entity)).expectNextMatches(createdEntity -> {
			savedEntity = createdEntity;
			return arePostEqual(entity, savedEntity);
		}).verifyComplete();
	}

	@Test
	public void create() {
		PostEntity newEntity = new PostEntity(2, "Type of post", "Post caption", LocalDate.now());

		StepVerifier.create(repository.save(newEntity))
				.expectNextMatches(createdEntity -> newEntity.getPostId() == createdEntity.getPostId())
				.verifyComplete();

		StepVerifier.create(repository.findById(newEntity.getId()))
				.expectNextMatches(foundEntity -> arePostEqual(newEntity, foundEntity)).verifyComplete();

		StepVerifier.create(repository.count()).expectNext(2l).verifyComplete();
	}

	@Test
	public void update() {
		savedEntity.setPostCaption("Updated post caption");
		StepVerifier.create(repository.save(savedEntity))
				.expectNextMatches(updatedEntity -> updatedEntity.getPostCaption().equals("Updated post caption"))
				.verifyComplete();

		StepVerifier.create(repository.findById(savedEntity.getId()))
				.expectNextMatches(foundEntity -> foundEntity.getVersion() == 1
						&& foundEntity.getPostCaption().equals("Updated post caption"))
				.verifyComplete();
	}

	@Test
	public void delete() {
		StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
		StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
	}

	@Test
	public void getByPostId() {

		StepVerifier.create(repository.findByPostId(savedEntity.getPostId()))
				.expectNextMatches(foundEntity -> arePostEqual(savedEntity, foundEntity)).verifyComplete();
	}

	@Test
	public void duplicateError() {
		PostEntity entity = new PostEntity(savedEntity.getPostId(), "Type of post", "Post caption", LocalDate.now());
		StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
	}

	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		PostEntity entity1 = repository.findById(savedEntity.getId()).block();
		PostEntity entity2 = repository.findById(savedEntity.getId()).block();

		// Update the entity using the first entity object
		entity1.setPostCaption("Updated post caption");
		repository.save(entity1).block();

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		entity2.setTypeOfReaction("Updated post caption again...");
		StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

		// Get the updated entity from the database and verify its new sate
		StepVerifier.create(repository.findById(savedEntity.getId()))
				.expectNextMatches(foundEntity -> foundEntity.getVersion() == 1
						&& foundEntity.getPostCaption().equals("Updated post caption"))
				.verifyComplete();
	}

	private boolean arePostEqual(PostEntity expectedEntity, PostEntity actualEntity) {
		return (expectedEntity.getId().equals(actualEntity.getId()))
				&& (expectedEntity.getVersion() == actualEntity.getVersion())
				&& (expectedEntity.getPostId() == actualEntity.getPostId())
				&& (expectedEntity.getTypeOfPost().equals(actualEntity.getTypeOfPost()))
				&& (expectedEntity.getPostCaption() == actualEntity.getPostCaption())
				&& (expectedEntity.getPostedOn().equals(actualEntity.getPostedOn()));
	}
}