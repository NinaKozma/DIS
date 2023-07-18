package se.magnus.api.core.comment;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface CommentService {

    Comment createComment(@RequestBody Comment body);

    /**
     * Sample usage: curl $HOST:$PORT/comment?postId=1
     *
     * @param postId
     * @return
     */
    @GetMapping(
        value    = "/comment",
        produces = "application/json")
    Flux<Comment> getComments(@RequestParam(value = "postId", required = true) int postId);

    void deleteComments(@RequestParam(value = "postId", required = true)  int postId);
}
