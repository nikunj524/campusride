package com.campusride.authservice.service;

import com.campusride.authservice.exception.MailSendingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final String PASSWORD_RESET_SUBJECT = "CampusRide Password Reset OTP";

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public MailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendPasswordResetOtp(String recipientEmail, String otp) {
        String body = "Your CampusRide password reset OTP is: " + otp
                + "\n\nThis OTP expires in 10 minutes. Do not share it with anyone.";
        sendEmail(recipientEmail, PASSWORD_RESET_SUBJECT, body, "Unable to send password reset OTP");
    }

    private void sendEmail(String recipientEmail, String subject, String body, String failureMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new MailSendingException(failureMessage, exception);
        }
    }
}
