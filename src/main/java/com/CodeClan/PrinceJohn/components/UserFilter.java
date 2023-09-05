package com.CodeClan.PrinceJohn.components;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

public class UserFilter extends OncePerRequestFilter {

    @Autowired
    JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token;
        JWTClaimsSet claims;
        String issuer;
        Long user_id;
        String user_email;
        Long jwt_user_id;
        String jwt_user_email;
        Boolean jwt_refresh;
        try {
            token = request.getHeader("bearer");
            claims = jwtTokenService.decryptToken(token).orElseThrow();
            issuer = (String) claims.getClaim("issuer");
            user_id = Long.parseLong(request.getHeader("user_id"));
            user_email = request.getHeader("user_email");
            jwt_user_id = Long.parseLong((String) claims.getClaim("user_id"));
            jwt_user_email = (String) claims.getClaim("user_email");
            jwt_refresh = (Boolean) claims.getClaim("refresh");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!Objects.equals(issuer, "PrinceJohn")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!user_id.equals(jwt_user_id)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!Objects.equals(user_email, jwt_user_email)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user_id,jwt_refresh);
        authentication.setAuthenticated(Boolean.TRUE);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        filterChain.doFilter(request, response);
    }
}
