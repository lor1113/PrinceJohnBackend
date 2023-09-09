package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component
@Order(20)
public class UserFilter extends OncePerRequestFilter {

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("User filter starting");
        String token;
        JWTClaimsSet claims;
        String issuer;
        Long jwt_user_id;
        Long jwt_security_id;
        String jwt_user_email;
        Boolean jwt_refresh;
        Date expiration;
        Date issued_on;
        Date not_before;
        try {
            token = request.getHeader("bearer");
            claims = jwtTokenService.decryptToken(token).orElseThrow();
            issuer = claims.getIssuer();
            issued_on = claims.getIssueTime();
            expiration = claims.getExpirationTime();
            not_before = claims.getNotBeforeTime();
            jwt_user_id = (Long) claims.getClaim("user_id");
            jwt_user_email = (String) claims.getClaim("user_email");
            jwt_security_id = (Long) claims.getClaim("security_id");
            jwt_refresh = (Boolean) claims.getClaim("refresh");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Optional<UserSecrets> optSecrets = userSecretsRepository.findById(jwt_user_id);
        if (optSecrets.isEmpty()) {
            System.out.println("fails user id");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        UserSecrets secrets = optSecrets.get();
        Date now = new Date();
        if (!Objects.equals(issuer, "PrinceJohn")) {
            System.out.println("fails issuer");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (expiration.before(now)) {
            System.out.println("expired");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (issued_on.after(now)) {
            System.out.println("what");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (not_before.after(now)) {
            System.out.println("fails not before");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!secrets.getId().equals(jwt_user_id)) {
            System.out.println("fails user_id");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!secrets.securityId.equals(jwt_security_id)) {
            System.out.println("fails security_id");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!Objects.equals(secrets.email, jwt_user_email)) {
            System.out.println("fails email");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        Boolean refresh_needed = matcher.match("/appEndpoint/s2/**", path);
        if (jwt_refresh != refresh_needed) {
            System.out.println("fails refresh");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(secrets.Id, "");
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        System.out.println("User filter success");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        Boolean shouldFilter1 = matcher.match("/appEndpoint/s1/**", path);
        Boolean shouldFilter2 = matcher.match("/appEndpoint/s2/**", path);
        Boolean shouldFilter3 = matcher.match("/appEndpoint/s3/**", path);
        return !(shouldFilter1 || shouldFilter2 || shouldFilter3);
    }
}
