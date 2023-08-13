package se.magnus.microservices.core.image;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.image.Image;
import se.magnus.microservices.core.image.persistence.ImageEntity;
import se.magnus.microservices.core.image.services.ImageMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ImageMapperTests {

	private ImageMapper mapper = Mappers.getMapper(ImageMapper.class);

	@Test
	public void imageMapperTests() {

		assertNotNull(mapper);

		Image api = new Image(1, 2, "image url", LocalDate.now(), "adr");

		ImageEntity entity = mapper.apiToEntity(api);

		assertEquals(api.getPostId(), entity.getPostId());
		assertEquals(api.getImageId(), entity.getImageId());
		assertEquals(api.getImageUrl(), entity.getImageUrl());
		assertEquals(api.getUploadDate(), entity.getUploadDate());

		Image api2 = mapper.entityToApi(entity);

		assertEquals(api.getPostId(), api2.getPostId());
		assertEquals(api.getImageId(), api2.getImageId());
		assertEquals(api.getImageUrl(), api2.getImageUrl());
		assertEquals(api.getUploadDate(), api2.getUploadDate());
		assertNull(api2.getServiceAddress());
	}

	@Test
	public void mapperListTests() {

		assertNotNull(mapper);

		Image api = new Image(1, 2, "image url", LocalDate.now(), "adr");
		List<Image> apiList = Collections.singletonList(api);

		List<ImageEntity> entityList = mapper.apiListToEntityList(apiList);
		assertEquals(apiList.size(), entityList.size());

		ImageEntity entity = entityList.get(0);

		assertEquals(api.getPostId(), entity.getPostId());
		assertEquals(api.getImageId(), entity.getImageId());
		assertEquals(api.getImageUrl(), entity.getImageUrl());
		assertEquals(api.getUploadDate(), entity.getUploadDate());

		List<Image> api2List = mapper.entityListToApiList(entityList);
		assertEquals(apiList.size(), api2List.size());

		Image api2 = api2List.get(0);

		assertEquals(api.getPostId(), api2.getPostId());
		assertEquals(api.getImageId(), api2.getImageId());
		assertEquals(api.getImageUrl(), api2.getImageUrl());
		assertEquals(api.getUploadDate(), api2.getUploadDate());
		assertNull(api2.getServiceAddress());
	}
}
