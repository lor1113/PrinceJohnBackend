package com.CodeClan.PrinceJohn.components;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@EnableScheduling
@Component
public class ReplayFilter extends OncePerRequestFilter {

    private List<String> list1 = new ArrayList<>();
    private List<String> list2 = new ArrayList<>();
    private Boolean toggle = Boolean.TRUE;
    private static final int messageLife = 1500;

    @Scheduled(fixedDelay = 5000)
    public void iterator () {
        if (toggle) {
            list2.clear();
            toggle = Boolean.FALSE;
        } else {
            list1.clear();
            toggle = Boolean.TRUE;
        }
    }


    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String sendID;
        long sendDate;
        try {
            sendID = request.getHeader("X-Operation-Id");
            sendDate = Long.parseLong(request.getHeader("X-Operation-Timestamp"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long unixTime = System.currentTimeMillis();
        if (sendDate > unixTime) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if ((unixTime - sendDate) > messageLife) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (list1.contains(sendID)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (list2.contains(sendID)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (toggle) {
            list1.add(sendID);
        } else {
            list2.add(sendID);
        }
        filterChain.doFilter(request, response);
    }
}
