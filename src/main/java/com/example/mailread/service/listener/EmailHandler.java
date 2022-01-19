package com.example.mailread.service.listener;

import com.sun.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EmailHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static void handleMessages(IMAPFolder folder, Message[] messages) {
        handleMessages(folder, Arrays.asList(messages));
    }

    private static void handleMessages(final IMAPFolder folder, List<Message> messages) {
     
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                try {
                    return Long.compare(folder.getUID(m1), folder.getUID(m2));
                } catch (MessagingException e) {
//                    logger.info(
//                            "Failed to compare uids of " + m1 + " and " + m2 +
//                                    " while sorting messages by the uid; " +
//                                    "There is some remote chance of loss of " +
//                                    "mail requests.");
                    return 0;
                }
            }
        });

        /**
        for (Message msg : messages) {
            handleMessage((IMAPMessage) msg);
        }
         **/
    }
}
