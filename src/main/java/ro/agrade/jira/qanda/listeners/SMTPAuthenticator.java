/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created at: Oct 12, 2010 8:43:06 AM
 * 
 * File: SMTPAuthenticator.java
 */
package ro.agrade.jira.qanda.listeners;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Authenticator class for the SMTP server
 *
 * @author Florin Manaila (flo.manaila@gmail.com)
 */
public class SMTPAuthenticator extends Authenticator {

    private String user;
    private String password;

    public SMTPAuthenticator(String username, String password) {
        this.user = username;
        this.password = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

}
