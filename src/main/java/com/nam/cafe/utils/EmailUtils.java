package com.nam.cafe.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {
    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String text, List<String> listCc) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ltnam43202.dev@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if (listCc != null && !listCc.isEmpty()) {
            message.setCc(getCcList(listCc));
        }
        emailSender.send(message);
    }

    public String[] getCcList(List<String> listCc) {
        String[] cc = new String[listCc.size()];
        for (int i = 0; i < listCc.size(); i++) {
            cc[i] = listCc.get(i);
        }
        return cc;
    }

    public void forgotPassword(String to, String subject, String password) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("ltnam43202.dev@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg = "<p><b>Your Login details for Cafe Management System</b><br><b>Email: </b> " + to + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlMsg, "text/html");
        emailSender.send(message);
    }
}
