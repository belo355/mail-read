package com.example.mailread.controller;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailServiceImpl extends Thread {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SUBJECT_MAIL = "teste";
    private static final String FOLDER_INBOX = "INBOX";
    private static final String HOST = "imap.gmail.com";
    private static final int PORT = 993;
    private static final String USER = "your-mail@teste.com";
    private static final String PASS = "your-pass";
    private static final String IMAP_PROTOCOL = "imaps";

    private IMAPStore imapStore;
    private IMAPFolder imapFolder;

    public List<EmailDTO> getNewMessages() throws MessagingException, IOException {
        logger.info("Verifying new messages inbox...");
        try {
            connectEmail();
            getFolder();
            List<EmailDTO> emailDTOS = readMessagesFromFolderAndSetFlagSeen(imapFolder);
            logger.info("Success on verifying new messages inbox! .. total emails: {}", emailDTOS.size());
            return emailDTOS;
        } catch (MessagingException | IOException e) {
            logger.error("Error on verifying new messages: {}", e.getMessage());
            throw e;
        } finally {
            closeFolder(imapFolder);
            closeImapStore();
        }
    }

    private void connectEmail() throws MessagingException {
        Session emailSession = Session.getDefaultInstance(new Properties());
        imapStore = (IMAPStore) emailSession.getStore(IMAP_PROTOCOL);
        imapStore.connect(HOST, PORT, USER, PASS);
        logger.info("connected email!");
    }

    private void getFolder() throws MessagingException {
        this.imapFolder = (IMAPFolder) imapStore.getFolder(FOLDER_INBOX);
        if (this.imapFolder != null) {
            this.imapFolder.open(Folder.READ_WRITE);
            logger.info("folder open!");
        }else {
            logger.info("folder not open!");
        }
    }

    private List<EmailDTO> readMessagesFromFolderAndSetFlagSeen(Folder folder) throws MessagingException, IOException {
        if (folder == null) {
            logger.info("No folder found.");
            return new ArrayList<>();
        }
        Message[] messages = getNewMessages(folder);
        if (messages.length == 0) logger.info("No messages found.");

        List<EmailDTO> emails = new ArrayList<>();
        for (Message message : messages) {
            Address[] from = message.getFrom();

            emails.add(EmailDTO.builder()
                    .from(((InternetAddress) from[0]).getAddress())
                    .subject(message.getSubject())
                    .content(message.getContent().toString())
                    .build());
            message.setFlag(Flags.Flag.SEEN, true);
        }
        return emails;
    }

    private Message[] getNewMessages(Folder folder) throws MessagingException {
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, true);

        SearchTerm subjectSearchTerm = new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    return message.getSubject().toLowerCase().contains(SUBJECT_MAIL);
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        };

        final SearchTerm[] filters = {unseenFlagTerm, subjectSearchTerm};
        final SearchTerm searchTerm = new AndTerm(filters);
        return folder.search(searchTerm);
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
