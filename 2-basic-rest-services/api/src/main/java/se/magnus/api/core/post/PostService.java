package se.magnus.api.core.post;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface PostService {

	Post createPost(@RequestBody Post body);

	/**
	 * Sample usage: curl $HOST:$PORT/post/1
	 *
	 * @param postId
	 * @return the post, if found, else null
	 */
	@GetMapping(value = "/post/{postId}", produces = "application/json")
	Mono<Post> getPost(@PathVariable int postId,
			@RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
			@RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent);

	void deletePost(@PathVariable int postId);
}
