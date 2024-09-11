package com.huflit.studentmanagement.utilities;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class SMTP {

    public static Session sendMail() {
        String host = "smtp.gmail.com";
        String senderEmail = "topchannel102@gmail.com";
        String passwordSenderEmail = "icvc sksu obdu hosk";

        Properties props = System.getProperties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, passwordSenderEmail);
                    }
                });

        return session;
    }
}
