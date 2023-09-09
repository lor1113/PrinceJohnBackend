package com.CodeClan.PrinceJohn.components;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@Component
@Order(0)
public class ReplayFilter extends OncePerRequestFilter {

    private static final int messageLife = 2;
    private final List<Long> list1 = new ArrayList<>();
    private final List<Long> list2 = new ArrayList<>();
    private Boolean toggle = Boolean.TRUE;

    @Scheduled(fixedDelay = 30000)
    public void iterator() {
        if (toggle) {
            list2.clear();
            toggle = Boolean.FALSE;
        } else {
            list1.clear();
            toggle = Boolean.TRUE;
        }
    }


    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        Long sendID;
        long sendDate;
        System.out.println("Replay filter starting");
        try {
            sendID = Long.parseLong(request.getHeader("X-Operation-Id"));
            sendDate = Long.parseLong(request.getHeader("X-Operation-Timestamp"));
        } catch (Exception e) {
            System.out.println("Missing/malformed headers");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long unixTime = System.currentTimeMillis() / 1000L;
        if (sendDate > unixTime) {
            System.out.println("what");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if ((unixTime - sendDate) > messageLife) {
            System.out.println("Message timed out");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (list1.contains(sendID)) {
            System.out.println("Message duplicated");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (list2.contains(sendID)) {
            System.out.println("Message duplicated");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (toggle) {
            list1.add(sendID);
        } else {
            list2.add(sendID);
        }
        System.out.println("Replay filter success");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean shouldFilter = matcher.match("/appEndpoint/s*/**", path);
        return !shouldFilter;
    }
}
