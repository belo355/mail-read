package com.example.mailread.service.listener;

import com.sun.mail.imap.IMAPStore;

import com.sun.mail.imap.* ;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * MailboxService opens a mailbox and process emails if necessary.
 *
 * MailboxService connects an IMAP server and opens the mailbox from the
 * server. Every configuration to be needed to do that is defined by imap.* in
 * conf/application.conf.
 *
 * Then MailboxService fetches and processes emails in the mailbox as follows:
 *
 * 1. Only emails whose recipients contain the address of Yobi defined by
 *    imap.address configuration are accepted.
 * 2. Emails must have one or more recipients which are Yobi projects; If not
 *    Yobi replies with an error.
 * 3. Emails which reference or reply to a resource assumed to comment the
 *    resource; otherwise assumed to post an issue in the projects.
 * 4. Yobi does the assumed job only if the sender has proper permission to do
 *    that; else Yobi replies with a permission denied error.
 *
 * Note: It is possible to create multiple resources if the recipients contain
 * multiple projects.
 */

@Service
public class MailboxService {
    private static final String FOLDER_INBOX = "INBOX";
    private static final String IMAP_PROTOCOL = "imaps";
    private static final String HOST = "imap.gmail.com";
    private static final int PORT = 993;
    private static final String USER = "anyteste123@gmail.com";
    private static final String PASS = "!@#Mudar";

    private IMAPStore store;
    private static IMAPFolder imapFolder;
    private Thread idleThread;
    private final boolean isStopping = false;

    private IMAPStore imapStore;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void startEmailListener() throws MessagingException, UnsupportedOperationException {
        Folder folder = null;
        try {
            imapStore = connectEmail();
            logger.info("Success conected!");
            folder = getFolder(FOLDER_INBOX);
//            Message[] messages = folder.getMessages();
//            List<Message> messagesList = Arrays.asList(messages);

            if (!imapStore.hasCapability("IDLE")) {
                throw new UnsupportedOperationException("The imap server does not support IDLE command");
            }

            imapFolder = (IMAPFolder) imapStore.getFolder(FOLDER_INBOX);
            MessageCountListener messageCountListener = new MessageCountListener() {
                @Override
                public void messagesAdded(MessageCountEvent messageCountEvent) {
                    try {
                        //action for received message
                        EmailHandler.handleMessages(imapFolder, messageCountEvent.getMessages());
                    } catch (Exception e1) {
                        logger.error("Unexpected error occurs while handling messages", e1);
                    }
                }
                @Override
                public void messagesRemoved(MessageCountEvent e) {
                    //action for remove message
                }
            };
            // Add the handler for messages to be added in the future.
            imapFolder.addMessageCountListener(messageCountListener);

            idleThread = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    logger.info("Start the Email Receiving Thread");
                    while (true) {
                        if (isStopping) break;
                        try {
                            // Notify the message count listener if the value of EXISTS response is larger than realTotal.
                            imapFolder.idle();
                        } catch (FolderClosedException e) {
                            if (isStopping) break;
                            // reconnect
                            logger.info("Reopen the imap folder");
                            imapFolder = reopenFolder();
                        } catch (Exception e) {
                            logger.warn("Failed to run IDLE command; abort", e);
                            Thread.sleep(1000);
                            //break;
                        }
                    }
                    logger.info("Stop the Email Receiving Thread");
                }
            };
            idleThread.start();

        } catch (MessagingException e) {
            logger.error("Error on verifying new messages: " + e.getMessage());
            throw e;
        } finally {
            closeFolder(folder);
            closeImapStore();
        }

    }

    private IMAPFolder reopenFolder() {
        return null;
    }

    private Folder getFolder(String folderName) throws MessagingException {
        Folder folder = imapStore.getFolder(folderName);
        if (folder != null) {
            folder.open(Folder.READ_WRITE);
            logger.info("folder open!");
            return folder;
        }
        logger.info("folder not open!");
        return folder;
    }

    private IMAPStore connectEmail() throws MessagingException {
        Session emailSession = Session.getDefaultInstance(new Properties());
        imapStore = (IMAPStore) emailSession.getStore(IMAP_PROTOCOL);
        imapStore.connect(HOST, PORT, USER, PASS);
        return imapStore;
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
