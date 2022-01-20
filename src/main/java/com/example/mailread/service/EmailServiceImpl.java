package com.example.mailread.service;

import com.example.mailread.config.EmailCredentials;
import com.example.mailread.config.EmailVO;
import com.sun.mail.imap.IMAPStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class EmailServiceImpl {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SUBJECT_MAIL = "Test Subject";
    private static final String FOLDER_INBOX = "INBOX";
    private static final String HOST = "imap.gmail.com";
    private static final int PORT = 993;
    private static final String USER = "anyteste123@gmail.com";
    private static final String PASS = "!@#Mudar";
    private static final String IMAP_PROTOCOL = "imaps";

    private EmailCredentials emailCredentials;
    private IMAPStore imapStore;

    @Autowired
    public EmailServiceImpl(EmailCredentials emailCredentials) {
        this.emailCredentials = emailCredentials;
    }

    public List<EmailVO> getNewMessages() throws MessagingException, IOException {
        logger.info("Verifying new messages inbox...");
        Folder inbox = null;
        try {
            connectEmail();
            inbox = getFolder(FOLDER_INBOX);
            List<EmailVO> emailVOS = readMessagesFromFolder(inbox);
            logger.info("Success on verifying new messages inbox!");
            return emailVOS;
        } catch (MessagingException | IOException exception) {
            logger.error("Error on verifying new messages: " + exception.getMessage());
            throw exception;
        } finally {
            closeFolder(inbox);
            closeImapStore();
        }
    }

    private void connectEmail() throws MessagingException {
        Session emailSession = Session.getDefaultInstance(new Properties());
        imapStore = (IMAPStore) emailSession.getStore(IMAP_PROTOCOL);
        imapStore.connect(HOST, PORT, USER, PASS);
        logger.info("connected email!");
    }

    private Folder getFolder(String folderName) throws MessagingException {
        Folder inbox = imapStore.getFolder(folderName);
        if (inbox != null) inbox.open(Folder.READ_WRITE);
        return inbox;
    }

    private List<EmailVO> readMessagesFromFolder(Folder folder) throws MessagingException, IOException {
        if (folder == null) {
            logger.info("No folder found.");
            return null;
        }

        Message[] messages = getUnseenMessages(folder);

        if (messages.length == 0) logger.info("No messages found.");

        List<EmailVO> emails = new ArrayList<>();
        for (Message message : messages) {
            Address[] from = message.getFrom();

            emails.add(EmailVO.builder()
                    .from(((InternetAddress) from[0]).getAddress())
                    .subject(message.getSubject())
                    .content(message.getContent().toString())
                    .build());

            message.setFlag(Flags.Flag.SEEN, true);
        }

        return emails;
    }

    private Message[] getUnseenMessages(Folder folder) throws MessagingException {
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

        SearchTerm subjectSearchTerm = new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    logger.info("subject : " + message.getSubject());
                    return message.getSubject().toLowerCase().contains("teste");
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        };

        final SearchTerm[] filters = {unseenFlagTerm, subjectSearchTerm};
        final SearchTerm searchTerm = new AndTerm(filters);
        Message[] messages = folder.search(searchTerm);
        List<Message> messagesList = Arrays.asList(messages);
        return messages;
    }


    private void closeFolder(Folder inbox) throws MessagingException {
        if (inbox != null) {
            inbox.close(false);
        }
    }

    private void closeImapStore() throws MessagingException {
        if (imapStore != null) {
            imapStore.close();
        }
    }
}
