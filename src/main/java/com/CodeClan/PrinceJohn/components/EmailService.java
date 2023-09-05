package com.CodeClan.PrinceJohn.components;

import org.springframework.stereotype.Component;

@Component
public class EmailService {

    public void sendEmail (String email, String message) {
        System.out.println(email);
        System.out.println(message);
    }
}
