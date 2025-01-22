package com.nametrek.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nametrek.api.dto.EmailRequest;
import com.nametrek.api.service.EmailService;

import jakarta.validation.Valid;

@RestController
public class EmailController {

    private EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }


    @PostMapping("/contact")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequest request) {
        emailService.sendMailAsync(request.getTo(), request.getEmail(), request.getSubject(), request.getText())
            .exceptionally(throwable -> {
                // Log the error but don't block the response
                System.err.println("Error sending email: " + throwable.getMessage());
                return null;
            });
        return ResponseEntity.accepted().body("Message submitted successfully!");
    }
}
