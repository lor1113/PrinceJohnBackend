package com.CodeClan.PrinceJohn.components;

import org.springframework.stereotype.Component;

@Component
public class EmailService {

    public void sendEmail(String email, String message) {
        System.out.println("Sending email to: " + email);
        System.out.println("Message Content: " + message);
    }
}
