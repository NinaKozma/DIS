package se.magnus.microservices.core.image;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.image.persistence.ImageEntity;
import se.magnus.microservices.core.image.persistence.ImageRepository;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@RunWith(SpringRunner.class)
@DataJpaTest(properties = { "spring.cloud.config.enabled=false" })
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

	@Autowired
	private ImageRepository repository;

	private ImageEntity savedEntity;

	@Before
	public void setupDb() {
		repository.deleteAll();

		ImageEntity entity = new ImageEntity(1, 2, "Image URL", LocalDate.now());
		savedEntity = repository.save(entity);

		assertEqualsImage(entity, savedEntity);
	}

	@Test
	public void create() {

		ImageEntity newEntity = new ImageEntity(1, 3, "Image URL", LocalDate.now());
		repository.save(newEntity);

		ImageEntity foundEntity = repository.findById(newEntity.getId()).get();
		assertEqualsImage(newEntity, foundEntity);

		assertEquals(2, repository.count());
	}

	@Test
	public void update() {
		savedEntity.setImageUrl("Updated image URL");
		repository.save(savedEntity);

		ImageEntity foundEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (long) foundEntity.getVersion());
		assertEquals("Updated image URL", foundEntity.getImageUrl());
	}

	@Test
	public void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()));
	}

	@Test
	public void getByPostId() {
		List<ImageEntity> entityList = repository.findByPostId(savedEntity.getPostId());

		assertThat(entityList, hasSize(1));
		assertEqualsImage(savedEntity, entityList.get(0));
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void duplicateError() {

		ImageEntity entity = new ImageEntity(1, 2, "Image URL", LocalDate.now());
		repository.save(entity);

	}

	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		ImageEntity entity1 = repository.findById(savedEntity.getId()).get();
		ImageEntity entity2 = repository.findById(savedEntity.getId()).get();

		// Update the entity using the first entity object
		entity1.setImageUrl("Updated image URL for the first time");
		repository.save(entity1);

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		try {
			entity2.setImageUrl("Updated image URL for the second time");
			repository.save(entity2);

			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException e) {
		}

		// Get the updated entity from the database and verify its new sate
		ImageEntity updatedEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (int) updatedEntity.getVersion());
		assertEquals("Updated image URL for the first time", updatedEntity.getImageUrl());
	}

	private void assertEqualsImage(ImageEntity expectedEntity, ImageEntity actualEntity) {
		assertEquals(expectedEntity.getId(), actualEntity.getId());
		assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
		assertEquals(expectedEntity.getPostId(), actualEntity.getPostId());
		assertEquals(expectedEntity.getImageId(), actualEntity.getImageId());
		assertEquals(expectedEntity.getImageUrl(), actualEntity.getImageUrl());
		assertEquals(expectedEntity.getUploadDate(), actualEntity.getUploadDate());
	}
}
