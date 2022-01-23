package com.example.mailread.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
public class MailController {

    private MailboxServiceImpl mailboxServiceImpl;

    @Autowired
    public MailController(MailboxServiceImpl mailboxServiceImpl){
        this.mailboxServiceImpl = mailboxServiceImpl;
    }

    @GetMapping(value = "/ls")
    public void startListener() {
        try {
            mailboxServiceImpl.start();
        } catch (MessagingException  e) {
            e.printStackTrace();
        }
    }
}
