package com.example.mailread.listener;

import com.sun.mail.imap.IMAPStore;

import com.sun.mail.imap.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            logger.info("listener add in ==> {}", imapFolder.getFullName());
            imapFolder.addMessageCountListener(messageCountListener);
            imapFolder.idle(true);
            //imapFolder.idle(); //analisar tempo de inatividade de regra para novo idle  -- https://stackovergo.com/pt/q/963390/javamail-keeping-imapfolderidle-alive

    }


    private void handleMessages(IMAPFolder folder, Message[] messages) throws MessagingException {
        logger.info("listener: qtd msg chegou ==> {} , momento ==> {}" ,messages.length ,LocalTime.now());
        Message [] fullMessages = getNewMessages(folder);
        logger.info("quantidade de mensagem capturadas apos aviso ==> {}", fullMessages.length);
        logger.info("envio para MQ ... ");
        setSeenisTrue(fullMessages);

        //start multithead para envio para fila
//        idleThread = new Thread() {
//            @Override
//            public void run() {
//                logger.info("Start the Email Receiving Thread");
//                while (true) {
//                    try {
//                        imapFolder.idle();
//                    } catch (FolderClosedException e) {
//                        logger.info("Reopen the imap folder");
//                        try {
//                            imapFolder = reopenFolder();
//                        } catch (MessagingException e1) {
//                            logger.warn("Failed to reopen the imap folder abort", e1);
//                            break;
//                        }
//                    } catch (Exception e) {
//                        logger.warn("Failed to run IDLE command; abort", e);
//                        break;
//                    }
//                }
//                logger.info("Stop the Email Receiving Thread");
//            }
//        };
//        idleThread.start();
    }

    private void setSeenisTrue(Message [] msgs) {
        List<Message> messages = Arrays.asList(msgs);
        messages.forEach(message -> {
            try {
                message.setFlag(Flags.Flag.SEEN, true);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
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
            logger.info("folder closed!");
            imapFolder.close(false);
        }
    }

    private void closeImapStore() throws MessagingException {
        if (imapStore != null) {
            logger.info("store closed!");
            imapStore.close();
        }
    }

    private Message[] getNewMessages(Folder folder) throws MessagingException {
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
