package com.example.mailread.listener;

import com.sun.mail.imap.IMAPStore;

import com.sun.mail.imap.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.util.*;

@Service
public class MailboxServiceImpl {
    private static final String FOLDER_INBOX = "INBOX";
    private static final String IMAP_PROTOCOL = "imaps";
    private static final String HOST = "imap.gmail.com";
    private static final int PORT = 993;
    private static final String USER = "anyteste123@gmail.com";
    private static final String PASS = "!@#Mudar";

    private IMAPStore store;
    private IMAPFolder imapFolder;
    private Thread idleThread;

    private IMAPStore imapStore;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void start() throws MessagingException, UnsupportedOperationException {
        try {
            connectEmail();
            openFolder(FOLDER_INBOX);
            startEmailListener();
        } catch (MessagingException e) {
            logger.error("Error on verifying new messages: {} ", e.getMessage());
        } finally {
//            closeFolder();
//            closeImapStore();
        }
    }

    private void startEmailListener() throws MessagingException {
            MessageCountListener messageCountListener = new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    try {
                        handleMessages(imapFolder, e.getMessages());
                    } catch (Exception e1) {
                        logger.error("Unexpected error occurs while handling messages", e1);
                    }
                }
                @Override
                public void messagesRemoved(MessageCountEvent e) { }
            };
            imapFolder.addMessageCountListener(messageCountListener);
            imapFolder.idle();
            logger.info("listener add in ==> " + imapFolder.getFullName());
    }

    private IMAPFolder reopenFolder() throws MessagingException {
        if (store == null || !store.isConnected()) {
            connectEmail();
        }
        IMAPFolder folder = (IMAPFolder) store.getFolder(FOLDER_INBOX);
        folder.open(Folder.READ_ONLY);
        return folder;
    }

    private void openFolder(String folderName) throws MessagingException {
        this.imapFolder = (IMAPFolder) imapStore.getFolder(folderName);
        if (this.imapFolder != null) {
            this.imapFolder.open(Folder.READ_WRITE);
            logger.info("folder open!");
        }else {
            logger.info("folder not open!");
        }
    }

    private void connectEmail() throws MessagingException {
        Session emailSession = Session.getDefaultInstance(new Properties());
        this.imapStore = (IMAPStore) emailSession.getStore(IMAP_PROTOCOL);
        this.imapStore.connect(HOST, PORT, USER, PASS);
        logger.info("connected email!");
    }

    private void closeFolder() throws MessagingException {
        if (imapFolder != null) {
            imapFolder.close(false);
        }
    }

    private void closeImapStore() throws MessagingException {
        if (imapStore != null) {
            imapStore.close();
        }
    }

    private void handleMessages(IMAPFolder folder, Message[] messages) throws MessagingException {
        logger.info("metodo chamado pelo listener -- qtd msg chegou" + messages.length);

        Message [] fullMessages = folder.getMessages();
        List<Message> messagesList = Arrays.asList(fullMessages);
        logger.info("quantidade de mensagem capturadas apos aviso ==> " + messagesList.size());
    }
}
