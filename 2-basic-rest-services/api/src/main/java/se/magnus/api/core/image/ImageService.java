package se.magnus.api.core.image;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

public interface ImageService {

    Image createImage(@RequestBody Image body);

    /**
     * Sample usage: curl $HOST:$PORT/image?postId=1
     *
     * @param postId
     * @return
     */
    @GetMapping(
        value    = "/image",
        produces = "application/json")
    Flux<Image> getImages(@RequestParam(value = "postId", required = true) int postId);

    void deleteImages(@RequestParam(value = "postId", required = true)  int postId);
}
