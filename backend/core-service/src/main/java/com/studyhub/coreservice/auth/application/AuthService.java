package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.api.dto.AuthResponse;
import com.studyhub.coreservice.auth.api.dto.LoginRequest;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import com.studyhub.coreservice.config.AppProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StudyHubUserRepository studyHubUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationEventPublisher authenticationEventPublisher;
    private final AppProperties appProperties;
    private final Clock clock;
    private final JwtEncoder jwtEncoder;

    public AuthService(
        StudyHubUserRepository studyHubUserRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationEventPublisher authenticationEventPublisher,
        AppProperties appProperties,
        Clock clock
    ) {
        this.studyHubUserRepository = studyHubUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationEventPublisher = authenticationEventPublisher;
        this.appProperties = appProperties;
        this.clock = clock;

        var secretKey = new SecretKeySpec(
            appProperties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        StudyHubUser user = authenticate(request);
        Instant issuedAt = clock.instant();
        long tokenLifetimeSeconds = appProperties.getSecurity().getTokenLifetimeSeconds();
        Instant expiresAt = issuedAt.plusSeconds(tokenLifetimeSeconds);
        List<String> roles = user.getRoles().stream()
            .map(role -> role.getCode())
            .sorted(Comparator.naturalOrder())
            .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getPublicId())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("role", roles.stream().findFirst().orElse("USER"))
            .claim("roles", roles)
            .claim("email", user.getEmail())
            .claim("displayName", user.getDisplayName())
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(),
            claims
        )).getTokenValue();

        authenticationEventPublisher.publishLoginSuccess(user);

        return new AuthResponse(
            token,
            "Bearer",
            tokenLifetimeSeconds,
            roles.stream().findFirst().orElse("USER")
        );
    }

    private StudyHubUser authenticate(LoginRequest request) {
        StudyHubUser user = studyHubUserRepository.findByEmailIgnoreCase(request.email())
            .filter(StudyHubUser::isActive)
            .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
