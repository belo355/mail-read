package com.example.mailread.service.listener.v2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.mailread.service.listener.MailboxService;

import javax.mail.MessagingException;

@RestController
public class MailController2 {

    @Autowired
    private MailboxService mailboxService;

    @RequestMapping(value = "/teste", method = RequestMethod.GET)
    public void startListener() {
        try {
            mailboxService.startEmailListener();
        } catch (MessagingException  e) {
            e.printStackTrace();
        }
    }
}
