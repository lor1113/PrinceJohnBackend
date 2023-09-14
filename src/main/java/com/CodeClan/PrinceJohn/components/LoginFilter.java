package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
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
import java.util.Optional;

@Component
@Order(20)
public class LoginFilter extends OncePerRequestFilter {

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    SecurityBeans securityBeans;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("Login Filter starting");
        String user_email;
        String user_password;
        try {
            user_email = request.getHeader("X-User-Email");
            user_password = request.getHeader("X-User-Password");
        } catch (Exception e) {
            System.out.println("Missing/malformed headers");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (user_email == null) {
            System.out.println("Missing/malformed email");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (user_password == null) {
            System.out.println("Missing/malformed password");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        user_email = user_email.toLowerCase();
        Optional<UserSecrets> userSecretsOptional = userSecretsRepository.findByEmail(user_email);
        if (userSecretsOptional.isEmpty()) {
            System.out.println("Bad user email");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        UserSecrets userSecrets = userSecretsOptional.get();
        if (userSecrets.loginDisabled) {
            System.out.println("User login disabled");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!securityBeans.encoder().matches(user_password, userSecrets.passwordHash)) {
            System.out.println("Bad password");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean check2FA = userSecrets.enabled2FA;
        boolean newLogin = matcher.match("/appEndpoint/s4/newLogin", path);
        if (newLogin) {
            System.out.println("Skipping 2FA check for /newLogin endpoint");
            check2FA = false;
        }
        if (check2FA) {
            String totpCode;
            totpCode = request.getHeader("X-User-TOTP");
            if (totpCode == null) {
                System.out.println("Missing TOTP header");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (totpCode.equals(userSecrets.getLastTOTP())) {
                System.out.println("TOTP matches previous request");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            TimeProvider timeProvider = new SystemTimeProvider();
            CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA512, 8);
            CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            boolean totpMatch = codeVerifier.isValidCode(userSecrets.secret2FA, totpCode);
            if (!totpMatch) {
                System.out.println("TOTP code mismatch");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            userSecrets.lastTOTP = totpCode;
            userSecretsRepository.save(userSecrets);
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userSecrets.Id, "");
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        System.out.println("Login filter success");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean shouldFilter = matcher.match("/appEndpoint/s4/**", path);
        return !shouldFilter;
    }

}
