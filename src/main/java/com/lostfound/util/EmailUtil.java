package com.lostfound.util;

public class EmailUtil {

    /**
     * Stub method for sending emails.
     * Right now it just prints to console so your project compiles.
     * Later you can replace this with Jakarta Mail code.
     */
    public boolean sendEmail(String to, String subject, String body) {
        System.out.println("Pretend sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        return true;
    }
}