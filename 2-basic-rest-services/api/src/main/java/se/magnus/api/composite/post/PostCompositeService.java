package se.magnus.api.composite.post;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Api(description = "REST API for composite post information.")
public interface PostCompositeService {

	/**
	 * Sample usage:
	 *
	 * curl -X POST $HOST:$PORT/post-composite \ -H "Content-Type: application/json"
	 * --data \ '{"postId":123,"typeOfPost":"Ig Reels","postCaption":"Having fun!",
	 * "postedOn":"2023-07-17"}'
	 *
	 * @param body
	 */
	@ApiOperation(value = "${api.post-composite.create-composite-post.description}", notes = "${api.post-composite.create-composite-post.notes}")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
			@ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.") })
	@PostMapping(value = "/post-composite", consumes = "application/json")
	Mono<Void> createCompositePost(@RequestBody PostAggregate body);

	/**
	 * Sample usage: curl $HOST:$PORT/post-composite/1
	 *
	 * @param postId
	 * @return the composite post info, if found, else null
	 */
	@ApiOperation(value = "${api.post-composite.get-composite-post.description}", notes = "${api.post-composite.get-composite-post.notes}")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
			@ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
			@ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.") })
	@GetMapping(value = "/post-composite/{postId}", produces = "application/json")
	Mono<PostAggregate> getCompositePost(@PathVariable int postId);

	/**
	 * Sample usage:
	 *
	 * curl -X DELETE $HOST:$PORT/post-composite/1
	 *
	 * @param postId
	 */
	@ApiOperation(value = "${api.post-composite.delete-composite-post.description}", notes = "${api.post-composite.delete-composite-post.notes}")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
			@ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.") })
	@DeleteMapping(value = "/post-composite/{postId}")
	Mono<Void> deleteCompositePost(@PathVariable int postId);
}
