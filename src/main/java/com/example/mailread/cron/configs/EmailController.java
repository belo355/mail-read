package com.example.mailread.cron.configs;

import com.example.mailread.cron.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void sendEmail() {
        try {
            emailService.getEmailsNeverSeen();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}