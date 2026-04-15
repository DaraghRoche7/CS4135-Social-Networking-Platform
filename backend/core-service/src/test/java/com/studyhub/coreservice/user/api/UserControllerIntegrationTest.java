package com.studyhub.coreservice.user.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studyhub.coreservice.auth.persistence.UserFollowRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFollowRepository userFollowRepository;

    @BeforeEach
    void setUp() {
        userFollowRepository.deleteAll();
    }

    @Test
    void followsAndUnfollowsUsers() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/follow", "peer-user")
                .with(jwtFor("demo-user", "USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("peer-user"))
            .andExpect(jsonPath("$.followersCount").value(1));

        mockMvc.perform(delete("/api/users/{userId}/follow", "peer-user")
                .with(jwtFor("demo-user", "USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("peer-user"))
            .andExpect(jsonPath("$.followersCount").value(0));
    }

    @Test
    void updatesOwnProfile() throws Exception {
        mockMvc.perform(put("/api/users/{userId}", "demo-user")
                .with(jwtFor("demo-user", "USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"updated.student@studyhub.local","displayName":"Updated Demo Student"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("demo-user"))
            .andExpect(jsonPath("$.email").value("updated.student@studyhub.local"))
            .andExpect(jsonPath("$.displayName").value("Updated Demo Student"));
    }

    private JwtRequestPostProcessor jwtFor(String subject, String... roles) {
        List<String> roleList = List.of(roles);
        return jwt()
            .jwt(token -> token.subject(subject).claim("roles", roleList))
            .authorities(roleList.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toArray(SimpleGrantedAuthority[]::new));
    }
}
