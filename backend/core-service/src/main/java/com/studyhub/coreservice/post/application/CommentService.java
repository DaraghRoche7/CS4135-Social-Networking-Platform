package com.studyhub.coreservice.post.application;

import com.studyhub.coreservice.auth.application.UserProvisioningService;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.post.api.dto.CommentResponse;
import com.studyhub.coreservice.post.api.dto.CreateCommentRequest;
import com.studyhub.coreservice.post.domain.Post;
import com.studyhub.coreservice.post.domain.PostComment;
import com.studyhub.coreservice.post.persistence.PostCommentRepository;
import com.studyhub.coreservice.post.persistence.PostRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserProvisioningService userProvisioningService;
    private final Clock clock;

    public CommentService(
            PostRepository postRepository,
            PostCommentRepository postCommentRepository,
            UserProvisioningService userProvisioningService,
            Clock clock
    ) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.userProvisioningService = userProvisioningService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(Long postId) {
        Post post = getRequiredPost(postId);
        return postCommentRepository.findByPostOrderByCreatedAtAsc(post).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long postId, CreateCommentRequest request, String currentUserId) {
        Post post = getRequiredPost(postId);
        StudyHubUser user = userProvisioningService.getOrProvision(currentUserId);
        PostComment comment = postCommentRepository.save(
                new PostComment(post, user, request.body().trim(), clock.instant())
        );
        return CommentResponse.from(comment);
    }

    private Post getRequiredPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}