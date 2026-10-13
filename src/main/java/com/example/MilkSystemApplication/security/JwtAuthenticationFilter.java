package com.example.MilkSystemApplication.security;

import com.example.MilkSystemApplication.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Claims claims = tokenProvider.getAllClaims(jwt);
                String username = claims.getSubject();
                String tenantId = (String) claims.get("tenantId");
                String role = (String) claims.get("role");
                String fullName = (String) claims.get("fullName");
                String email = (String) claims.get("email");
                Long userId = claims.get("userId") != null ? Long.parseLong(claims.get("userId").toString()) : null;

                if (tenantId != null && !tenantId.isEmpty()) {
                    TenantContext.setTenantId(tenantId);
                }

                UserPrincipal principal = new UserPrincipal(
                        userId,
                        username,
                        email,
                        "",
                        tenantId != null ? tenantId : "default-tenant",
                        fullName != null ? fullName : username,
                        role != null ? role : "ROLE_ADMIN"
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Check if X-Tenant-ID is supplied for unauthenticated or public requests
                String headerTenantId = request.getHeader("X-Tenant-ID");
                if (StringUtils.hasText(headerTenantId)) {
                    TenantContext.setTenantId(headerTenantId.trim());
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            filterChain.doFilter(request, response);
        } finally {
            // Clean up ThreadLocal tenant context at end of HTTP request to prevent thread leaks in pool
            TenantContext.clear();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
