package com.example.mailread.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.io.IOException;

@RestController
public class EmailController {

    private EmailServiceImpl emailService;

    @Autowired
    public EmailController(EmailServiceImpl emailService){
        this.emailService = emailService;
    }

    @GetMapping(value = "/run")
    public void sendEmail() {
        try {
            emailService.getNewMessages();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}