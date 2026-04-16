package com.studyhub.coreservice.post.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.domain.UserFollow;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import com.studyhub.coreservice.auth.persistence.UserFollowRepository;
import com.studyhub.coreservice.post.application.PostStorageService;
import com.studyhub.coreservice.post.domain.Post;
import com.studyhub.coreservice.post.persistence.PostLikeRepository;
import com.studyhub.coreservice.post.persistence.PostRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyHubUserRepository studyHubUserRepository;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostStorageService postStorageService;

    private StudyHubUser demoUser;
    private StudyHubUser peerUser;
    private Long peerPostId;

    @BeforeEach
    void setUp() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        userFollowRepository.deleteAll();

        demoUser = studyHubUserRepository.findByPublicId("demo-user").orElseThrow();
        peerUser = studyHubUserRepository.findByPublicId("peer-user").orElseThrow();

        userFollowRepository.save(new UserFollow(
            demoUser,
            peerUser,
            Instant.parse("2026-04-15T13:00:00Z")
        ));

        peerPostId = postRepository.save(new Post(
            peerUser,
            "Peer CS4135 Notes",
            "A concise set of sprint-planning notes.",
            "CS4135",
            "peer-cs4135.pdf",
            "application/pdf",
            12,
            postStorageService.storeSeedPdf("peer-test", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII)),
            Instant.parse("2026-04-15T13:05:00Z"),
            Instant.parse("2026-04-15T13:05:00Z")
        )).getId();

        postRepository.save(new Post(
            demoUser,
            "Own CS4001 Notes",
            "My own revision notes.",
            "CS4001",
            "own-cs4001.pdf",
            "application/pdf",
            12,
            postStorageService.storeSeedPdf("demo-test", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII)),
            Instant.parse("2026-04-15T12:55:00Z"),
            Instant.parse("2026-04-15T12:55:00Z")
        ));
    }

    @Test
    void returnsPersonalizedFeedFilteredByModule() throws Exception {
        mockMvc.perform(get("/api/feed")
                .param("module", "cs4135")
                .with(jwtFor("demo-user", "USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].title").value("Peer CS4135 Notes"))
            .andExpect(jsonPath("$.items[0].authorUserId").value("peer-user"));
    }

    @Test
    void uploadsPdfPostsForAuthenticatedUsers() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "new-note.pdf",
            "application/pdf",
            "%PDF-1.4".getBytes(StandardCharsets.US_ASCII)
        );

        mockMvc.perform(multipart("/api/posts")
                .file(file)
                .param("title", "Graph Theory Cheatsheet")
                .param("description", "Short definitions and worked examples.")
                .param("module", "cs3002")
                .with(jwtFor("demo-user", "USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Graph Theory Cheatsheet"))
            .andExpect(jsonPath("$.module").value("CS3002"))
            .andExpect(jsonPath("$.authorUserId").value("demo-user"));
    }

    @Test
    void likesAndUnlikesPosts() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/like", peerPostId)
                .with(jwtFor("demo-user", "USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.likedByUser").value(true))
            .andExpect(jsonPath("$.likesCount").value(1));

        mockMvc.perform(delete("/api/posts/{postId}/like", peerPostId)
                .with(jwtFor("demo-user", "USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.likedByUser").value(false))
            .andExpect(jsonPath("$.likesCount").value(0));
    }

    private JwtRequestPostProcessor jwtFor(String subject, String... roles) {
        List<String> roleList = Arrays.asList(roles);
        return jwt()
            .jwt(token -> token.subject(subject).claim("roles", roleList))
            .authorities(roleList.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList()));
    }
}
