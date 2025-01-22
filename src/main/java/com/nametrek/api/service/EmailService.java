package com.nametrek.api.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

	@Autowired
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}


    @Async("asyncExecutor")
	public CompletableFuture<Void> sendMailAsync(String to, String senderEmail, String subject, String text) {
        try {
            String formattedMessage = String.format("Contact Email: %s%nMessage: %s", senderEmail, text);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(formattedMessage);

            mailSender.send(message);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
	}
}
