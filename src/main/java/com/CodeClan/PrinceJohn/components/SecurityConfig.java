package com.CodeClan.PrinceJohn.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    ReplayFilter replayFilter;

    @Autowired
    UserFilter userFilter;

    @Autowired
    RefreshFilter refreshFilter;

    @Autowired
    LoginFilter loginFilter;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(PathRequest.toH2Console())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    SecurityFilterChain s0SecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/appEndpoint/s0")
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(replayFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    SecurityFilterChain s1SecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/appEndpoint/s1")
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(replayFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(userFilter, ReplayFilter.class);
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 30)
    SecurityFilterChain s2SecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/appEndpoint/s2")
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(replayFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(refreshFilter, ReplayFilter.class);
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 40)
    SecurityFilterChain s3SecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/appEndpoint/s3")
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(replayFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(userFilter, ReplayFilter.class);
        return http.build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 50)
    SecurityFilterChain s4SecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/appEndpoint/s4")
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAfter(replayFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(loginFilter, ReplayFilter.class);
        return http.build();
    }

    @Bean
    @Order()
    SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
        return http
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requiresChannel(channel -> channel.anyRequest().requiresSecure())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll())
                .build();
    }

}