package se.magnus.api.core.reaction;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface ReactionService {

    Reaction createReaction(@RequestBody Reaction body);

    /**
     * Sample usage:
     *
     * curl $HOST:$PORT/reaction?postId=1
     *
     * @param postId
     * @return
     */
    @GetMapping(
        value    = "/reaction",
        produces = "application/json")
    Flux<Reaction> getReactions(@RequestParam(value = "postId", required = true) int postId);

    void deleteReactions(@RequestParam(value = "postId", required = true)  int postId);
}
