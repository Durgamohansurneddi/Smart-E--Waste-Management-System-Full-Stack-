package com.ewaste.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String email, String token){

//        String resetLink = "http://localhost:4200/reset-password?token=" + token;
    	String resetLink = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Account Approved - Create Password");

        message.setText(
                "Your account has been approved.\n\n" +
                "Click the link below to create your password:\n\n" +
                resetLink
        );

        mailSender.send(message);
    }
}