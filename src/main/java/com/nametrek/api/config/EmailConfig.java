package com.nametrek.api.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {


	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private int port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${spring.mail.password}")
	private String password;
	// @Value("${spring.security.oauth2.client.registration.google.client-id}")
	// private String clientId;
	//
	// @Value("${spring.security.oauth2.client.registration.google.client-secret}")
	// private String clientSecret;
	//
	// @Value("${spring.security.oauth2.client.registration.google.refresh-token}")
	// private String refreshToken;
	//
	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(password);

		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");  // Corrected property
		props.put("mail.smtp.ssl.enable", "true");  // Corrected property
		props.put("mail.debug", "true");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.oauth.client.id", clientId);
		// props.put("mail.smtp.oauth.client.secret", clientSecret);
		// props.put("mail.smtp.oauth.refresh-token", refreshToken);

		return mailSender;
	}
}
