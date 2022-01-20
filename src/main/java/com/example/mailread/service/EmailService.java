package com.example.mailread.service;

import com.example.mailread.config.EmailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@Service
public class EmailService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private EmailServiceImpl emailServiceImpl;

    @Autowired
    public EmailService(EmailServiceImpl emailServiceImpl) {
        this.emailServiceImpl = emailServiceImpl;
    }

//        @Scheduled(cron = "*/30 * * * * *") // default: every 30 seconds
    public void getEmailsNeverSeen() throws MessagingException, IOException {
        List<EmailVO> newMessages = emailServiceImpl.getNewMessages();
        logger.info("Found {} new message(s)!", newMessages.size() );
        try{
            //adicionar na fila MQ
            logger.info("Adicionando mensagem na fila ");
        }catch (Exception e ){}
    }
}