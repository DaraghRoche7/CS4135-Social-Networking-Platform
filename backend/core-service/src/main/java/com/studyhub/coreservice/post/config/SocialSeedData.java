package com.studyhub.coreservice.post.config;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.domain.UserFollow;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import com.studyhub.coreservice.auth.persistence.UserFollowRepository;
import com.studyhub.coreservice.post.application.PostStorageService;
import com.studyhub.coreservice.post.domain.Post;
import com.studyhub.coreservice.post.domain.PostLike;
import com.studyhub.coreservice.post.persistence.PostLikeRepository;
import com.studyhub.coreservice.post.persistence.PostRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class SocialSeedData {

    @Bean
    @DependsOn("seedUsers")
    ApplicationRunner seedSocialData(
        StudyHubUserRepository studyHubUserRepository,
        UserFollowRepository userFollowRepository,
        PostRepository postRepository,
        PostLikeRepository postLikeRepository,
        PostStorageService postStorageService
    ) {
        return args -> {
            StudyHubUser demoUser = studyHubUserRepository.findByPublicId("demo-user").orElseThrow();
            StudyHubUser peerUser = studyHubUserRepository.findByPublicId("peer-user").orElseThrow();

            if (!userFollowRepository.existsByFollowerAndFollowed(demoUser, peerUser)) {
                userFollowRepository.save(new UserFollow(
                    demoUser,
                    peerUser,
                    Instant.parse("2026-04-15T10:30:00Z")
                ));
            }

            if (postRepository.count() > 0) {
                return;
            }

            byte[] peerPdf = samplePdfBytes("CS4135 Sprint Planning");
            String peerStoragePath = postStorageService.storeSeedPdf("peer-cs4135", peerPdf);
            Post peerPost = postRepository.save(new Post(
                peerUser,
                "CS4135 Sprint Planning Notes",
                "Shared summaries from the sprint planning meeting and task breakdown.",
                "CS4135",
                "cs4135-sprint-planning.pdf",
                "application/pdf",
                peerPdf.length,
                peerStoragePath,
                Instant.parse("2026-04-15T11:00:00Z"),
                Instant.parse("2026-04-15T11:00:00Z")
            ));

            byte[] demoPdf = samplePdfBytes("CS4001 Revision Pack");
            String demoStoragePath = postStorageService.storeSeedPdf("demo-cs4001", demoPdf);
            Post demoPost = postRepository.save(new Post(
                demoUser,
                "CS4001 Revision Pack",
                "A focused revision pack with key definitions, examples, and exam reminders.",
                "CS4001",
                "cs4001-revision-pack.pdf",
                "application/pdf",
                demoPdf.length,
                demoStoragePath,
                Instant.parse("2026-04-15T11:45:00Z"),
                Instant.parse("2026-04-15T11:45:00Z")
            ));

            postLikeRepository.save(new PostLike(
                peerPost,
                demoUser,
                Instant.parse("2026-04-15T12:05:00Z")
            ));
            postLikeRepository.save(new PostLike(
                demoPost,
                peerUser,
                Instant.parse("2026-04-15T12:10:00Z")
            ));
        };
    }

    private byte[] samplePdfBytes(String title) {
        String sanitizedTitle = title.replace("\\", "-").replace("(", "").replace(")", "");
        String contents = """
            BT
            /F1 18 Tf
            36 90 Td
            (%s) Tj
            ET
            """.formatted(sanitizedTitle);

        String object1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        String object2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        String object3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 300 144] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n";
        String object4 = "4 0 obj\n<< /Length %s >>\nstream\n%s\nendstream\nendobj\n".formatted(
            contents.getBytes(StandardCharsets.US_ASCII).length,
            contents
        );
        String object5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n";

        List<String> objects = List.of(object1, object2, object3, object4, object5);
        List<Integer> offsets = new ArrayList<>();

        StringBuilder builder = new StringBuilder("%PDF-1.4\n");
        for (String object : objects) {
            offsets.add(builder.toString().getBytes(StandardCharsets.US_ASCII).length);
            builder.append(object);
        }

        int xrefOffset = builder.toString().getBytes(StandardCharsets.US_ASCII).length;
        builder.append("xref\n0 6\n");
        builder.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            builder.append("%010d 00000 n \n".formatted(offset));
        }
        builder.append("trailer\n<< /Root 1 0 R /Size 6 >>\n");
        builder.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");
        return builder.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
