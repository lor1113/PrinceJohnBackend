package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginFilter extends OncePerRequestFilter {

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    SecurityBeans securityBeans;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String user_email;
        String user_password;
        try {
            user_email = request.getHeader("X-User-Email");
            user_password = request.getHeader("X-User-Password");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Optional<UserSecrets> userSecretsOptional = userSecretsRepository.findByEmail(user_email);
        if (userSecretsOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        UserSecrets userSecrets = userSecretsOptional.get();
        if(! securityBeans.encoder().matches(user_password,userSecrets.passwordHash)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (userSecrets.enabled2FA) {
            String user_2FA;
            try {
                user_2FA = request.getHeader("X-User-2FA");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            TimeProvider timeProvider = new SystemTimeProvider();
            CodeGenerator codeGenerator = new DefaultCodeGenerator();
            CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            Boolean match2FA = verifier.isValidCode(userSecrets.secret2FA,user_2FA);
            if (!match2FA) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userSecrets.Id,"");
        authentication.setAuthenticated(Boolean.TRUE);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        filterChain.doFilter(request, response);

    }
}
