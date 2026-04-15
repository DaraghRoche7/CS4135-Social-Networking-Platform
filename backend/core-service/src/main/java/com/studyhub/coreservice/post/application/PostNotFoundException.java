package com.studyhub.coreservice.post.application;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long postId) {
        super("Post %s was not found".formatted(postId));
    }
}
