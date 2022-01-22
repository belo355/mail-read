package com.example.mailread.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
public class EmailController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private EmailServiceImpl emailService;

    public EmailController(EmailServiceImpl emailService){
        this.emailService = emailService;
    }

    @RequestMapping(value = "/ls", method = RequestMethod.GET)
    public void sendEmail() {
        try {
            emailService.getNewMessages();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}