package com.CodeClan.PrinceJohn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class PrinceJohnApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrinceJohnApplication.class, args);
    }

}
