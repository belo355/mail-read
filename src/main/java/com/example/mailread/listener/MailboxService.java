package com.example.mailread.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;

@Service
public class MailboxService {

    private MailboxServiceImpl mailboxService;

    @Autowired
    public MailboxService(MailboxServiceImpl mailboxService) {
        this.mailboxService = mailboxService;
    }

    public void startGetEmailsListener() throws MessagingException {
        mailboxService.start();
    }
}
