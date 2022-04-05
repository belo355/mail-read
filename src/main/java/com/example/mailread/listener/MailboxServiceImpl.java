package com.example.mailread.listener;

import com.sun.mail.imap.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.time.LocalTime;
import java.util.*;

@Service
public class MailboxServiceImpl {

    @Value("${email.read.folder}")
    private String folder;

    @Value("${email.read.protocol}")
    private String protocol;

    @Value("${email.read.host}")
    private String host;

    @Value("${email.read.user}")
    private String user;

    @Value("${email.read.pass}")
    private String pass;

    @Value("${email.read.port}")
    private int port;

    private IMAPStore imapStore;
    private IMAPFolder imapFolder;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void start() throws MessagingException, UnsupportedOperationException {
        try {
            connectMail();
            openFolder();
            startMailListener();
        } catch (MessagingException e) {
            logger.error("Error on verifying new messages: {} ", e.getMessage());
        }
    }

    private void startMailListener() throws MessagingException {
        if (!((IMAPStore)imapFolder.getStore()).hasCapability("IDLE")) {
            throw new UnsupportedOperationException("The imap server does not support IDLE command");
        }
        if(imapFolder.isOpen()){
            createMessageCountListener();
            logger.info("listener add in ==> {}", imapFolder.getFullName());
        }else {
            logger.error("Folder is closed ==> {}", imapFolder.getFullName());
        }
    }

    private void createMessageCountListener() {
        MessageCountListener messageCountListener = new MessageCountListener() {
            @Override
            public void messagesAdded(MessageCountEvent e) {
                try {
                    handleMessage(imapFolder);
                } catch (Exception e1) {
                    logger.error("Unexpected error occurs while handling messages", e1);
                }
            }
            @Override
            public void messagesRemoved(MessageCountEvent e) { }
        };
        imapFolder.addMessageCountListener(messageCountListener);

        Thread idleThread = new Thread() {
            @Override
            public void run() {
                logger.info("Start the Email Receiving");
                while (true) {
                    try {
                        imapFolder.idle();
                    } catch (FolderClosedException e) {
                        logger.info("Reopen the imap folder");
                        try {
                            reopenFolder();
                        } catch (MessagingException e1) {
                            logger.warn("Failed to reopen the imap folder abort", e1);
                            break;
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to run IDLE command; abort", e);
                        break;
                    }
                }
                logger.info("Stop the Email Receiving Thread");
            }
        };
        idleThread.start();
    }


    private void handleMessage(IMAPFolder folder) throws MessagingException {
        logger.info("new mail received! ==> total mails ==> {}, momento ==> {}", folder.getMessageCount(), LocalTime.now());
        Message [] messages = searchForNewMessages(folder);
        setSeenMessage(messages);
    }

    private void setSeenMessage(Message [] msgs) {
        List<Message> messages = Arrays.asList(msgs);
        messages.forEach(message -> {
            try {
                String messageId = getID(message);
                logger.info("messageId recuparado: {}", messageId);
                message.setFlag(Flags.Flag.SEEN, true);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    private String getID(Message message) throws MessagingException {
        return ((IMAPMessage) message).getMessageID();
    }

    private void reopenFolder() throws MessagingException {
        if (imapStore == null || !imapStore.isConnected()) {
            connectMail();
        }
        this.imapFolder = (IMAPFolder) imapStore.getFolder(this.folder);
        imapFolder.open(Folder.READ_ONLY);
    }

    private void openFolder() throws MessagingException {
        this.imapFolder = (IMAPFolder) imapStore.getFolder(this.folder);
        if (this.imapFolder != null) {
            this.imapFolder.open(Folder.READ_WRITE);
            logger.info("folder open!");
        }else {
            logger.info("folder not open!");
        }
    }

    private void connectMail() throws MessagingException {
        Session emailSession = Session.getDefaultInstance(new Properties());
        this.imapStore = (IMAPStore) emailSession.getStore(this.protocol);
        this.imapStore.connect(this.host, this.port, this.user, this.pass);
        logger.info("connected email!");
    }

    private Message[] searchForNewMessages(Folder folder) throws MessagingException {
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

        SearchTerm subjectSearchTerm = new SearchTerm() {
            @Override
            public boolean match(Message message) {
                try {
                    return !message.getSubject().isEmpty();
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
}
