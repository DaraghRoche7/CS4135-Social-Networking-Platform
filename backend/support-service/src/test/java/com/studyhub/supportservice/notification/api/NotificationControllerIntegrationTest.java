package com.studyhub.supportservice.notification.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studyhub.supportservice.notification.application.CoreUserClient;
import com.studyhub.supportservice.notification.application.CoreUserSummaryResponse;
import com.studyhub.supportservice.notification.domain.Notification;
import com.studyhub.supportservice.notification.domain.NotificationCategory;
import com.studyhub.supportservice.notification.persistence.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private CoreUserClient coreUserClient;

    private Long demoNotificationId;
    private Long adminNotificationId;

    @BeforeEach
    void resetData() {
        notificationRepository.deleteAll();
        notificationRepository.saveAll(List.of(
            new Notification(
                "demo-user",
                "Assignment deadline moved",
                "CS4135 project deadline has moved to Friday 5 PM.",
                NotificationCategory.COURSE,
                false,
                Instant.parse("2026-04-15T12:15:00Z"),
                null
            ),
            new Notification(
                "demo-user",
                "New follower request",
                "A classmate wants to connect with you.",
                NotificationCategory.SOCIAL,
                false,
                Instant.parse("2026-04-15T11:00:00Z"),
                null
            ),
            new Notification(
                "demo-user",
                "Weekly digest",
                "Your study group posted 4 new notes this week.",
                NotificationCategory.DIGEST,
                true,
                Instant.parse("2026-04-14T18:30:00Z"),
                Instant.parse("2026-04-14T19:00:00Z")
            ),
            new Notification(
                "admin-user",
                "Moderation queue updated",
                "Two new reports were added to the moderation queue.",
                NotificationCategory.MODERATION,
                false,
                Instant.parse("2026-04-15T09:30:00Z"),
                null
                )
        ));

        demoNotificationId = notificationRepository.findByUserIdOrderByCreatedAtDesc("demo-user").stream()
            .filter(notification -> "Assignment deadline moved".equals(notification.getTitle()))
            .findFirst()
            .orElseThrow(() -> new UsernameNotFoundException("Missing demo notification"))
            .getId();

        adminNotificationId = notificationRepository.findByUserIdOrderByCreatedAtDesc("admin-user").stream()
            .findFirst()
            .orElseThrow(() -> new UsernameNotFoundException("Missing admin notification"))
            .getId();
    }

    @Test
    void returnsOnlyAuthenticatedUsersNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications").with(user("demo-user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(3))
            .andExpect(jsonPath("$.unreadCount").value(2))
            .andExpect(jsonPath("$.items[0].title").value("Assignment deadline moved"));
    }

    @Test
    void supportsFilteringUnreadNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .param("read", "false")
                .with(user("demo-user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].read").value(false));
    }

    @Test
    void marksNotificationAsReadForOwner() throws Exception {
        mockMvc.perform(put("/api/notifications/{notificationId}/read", demoNotificationId)
                .with(user("demo-user").roles("USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(demoNotificationId))
            .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void returnsNotFoundWhenNotificationBelongsToAnotherUser() throws Exception {
        mockMvc.perform(put("/api/notifications/{notificationId}/read", adminNotificationId)
                .with(user("demo-user").roles("USER")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Notification %d was not found".formatted(adminNotificationId)));
    }

    @Test
    void adminCanCreateNotificationForKnownRecipient() throws Exception {
        when(coreUserClient.getUserById("demo-user")).thenReturn(new CoreUserSummaryResponse(
            "demo-user",
            "student@studyhub.local",
            "Demo Student",
            true,
            java.util.Set.of("USER")
        ));

        mockMvc.perform(post("/api/notifications")
                .with(user("admin-user").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": "demo-user",
                      "title": "System maintenance",
                      "message": "A maintenance window starts tonight at 10 PM.",
                      "category": "SYSTEM"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("demo-user"))
            .andExpect(jsonPath("$.title").value("System maintenance"))
            .andExpect(jsonPath("$.read").value(false));
    }
}
