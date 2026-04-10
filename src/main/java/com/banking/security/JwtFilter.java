package com.banking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtFilter(JwtService jwtService,
                     UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // GUARD 1 — no header at all
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // GUARD 2 — extract token
        final String token = authHeader.substring(7);
        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // GUARD 3 — extract email
        final String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            logger.warn("Could not extract email from token: {}",
                    e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // GUARD 4 — email is null
        if (email == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // GUARD 5 — already authenticated
        if (SecurityContextHolder.getContext()
                                 .getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // GUARD 6 — user not found
        final UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            logger.warn("User not found for email: {}", email);
            filterChain.doFilter(request, response);
            return;
        }

        // GUARD 7 — token invalid
        if (!jwtService.isTokenValid(token, userDetails)) {
            logger.warn("Invalid token for user: {}", email);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ ALL GUARDS PASSED — set authentication
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authToken.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // continue the chain
        filterChain.doFilter(request, response);
    }
}