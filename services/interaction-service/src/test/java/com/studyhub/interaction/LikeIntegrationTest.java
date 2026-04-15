package com.studyhub.interaction;

import com.studyhub.interaction.client.NotesServiceClient;
import com.studyhub.interaction.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LikeRepository likeRepository;

    @MockBean
    private NotesServiceClient notesServiceClient;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private UUID testPostId;
    private UUID testUserId;

    @BeforeEach
    void setup() {
        likeRepository.deleteAll();
        testPostId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        when(notesServiceClient.postExists(any(UUID.class))).thenReturn(true);
    }

    private UsernamePasswordAuthenticationToken userAuth(UUID userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void likePost_shouldReturn201() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(testPostId.toString()))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()));
    }

    @Test
    void likePost_duplicate_shouldReturn409() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void unlikePost_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isNoContent());

        assertFalse(likeRepository.existsByPostIdAndUserId(testPostId, testUserId));
    }

    @Test
    void unlikePost_notLiked_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLikes_shouldReturnCountAndLikedFlag() throws Exception {
        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/posts/{postId}/likes", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.likedByCurrentUser").value(true));
    }

    @Test
    void getLikes_differentUser_likedByCurrentUserShouldBeFalse() throws Exception {
        UUID anotherUser = UUID.randomUUID();

        mockMvc.perform(post("/api/posts/{postId}/like", testPostId)
                .with(authentication(userAuth(anotherUser))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/posts/{postId}/likes", testPostId)
                .with(authentication(userAuth(testUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.likedByCurrentUser").value(false));
    }
}
