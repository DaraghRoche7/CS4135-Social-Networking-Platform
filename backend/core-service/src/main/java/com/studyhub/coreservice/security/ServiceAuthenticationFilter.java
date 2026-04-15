package com.studyhub.coreservice.security;

import com.studyhub.coreservice.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final AppProperties appProperties;

    public ServiceAuthenticationFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String providedApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);
        if (appProperties.getInternal().getApiKey().equals(providedApiKey)) {
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                "support-service",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
