package com.studyhub.supportservice.security;

import com.studyhub.supportservice.config.AppProperties;
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
public class DevTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;

    public DevTokenAuthenticationFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null
            && appProperties.getSecurity().isAllowDemoToken()) {
            String authorization = request.getHeader("Authorization");
            if ("Bearer demo-token".equals(authorization)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                    "demo-user",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if ("Bearer admin-demo-token".equals(authorization)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                    "admin-user",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
