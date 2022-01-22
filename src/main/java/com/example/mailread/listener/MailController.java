package com.example.mailread.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
public class MailController {

    @Autowired
    private MailboxServiceImpl mailboxServiceImpl;

    @RequestMapping(value = "/cron", method = RequestMethod.GET)
    public void startListener() {
        try {
            mailboxServiceImpl.start();
        } catch (MessagingException  e) {
            e.printStackTrace();
        }
    }
}
