package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.ProspectiveUser;
import com.CodeClan.PrinceJohn.repositories.ProspectiveUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@EnableScheduling
public class CleanupService {
    @Autowired
    ProspectiveUserRepository prospectiveUserRepository;

    @Scheduled(cron = "0 0 */2 * * *")
    public void cleanProspectiveUsers() {
        System.out.println("Cleaning prospective users");
        LocalDateTime now = LocalDateTime.now();
        List<ProspectiveUser> usersToClean = prospectiveUserRepository.findAll();
        for (ProspectiveUser user : usersToClean) {
            long timePassed = user.hashDate.until(now, ChronoUnit.SECONDS);
            if (timePassed > 7200) {
                prospectiveUserRepository.delete(user);
            }
        }
        System.out.println("Cleaned prospective users");
    }
}
