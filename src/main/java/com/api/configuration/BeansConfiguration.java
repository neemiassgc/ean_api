package com.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Configuration
public class BeansConfiguration {

    @Bean
    public JavaMailSender javaMailSender() {
        final Properties props = new Properties();
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.transport.protocol", "smtp");

        final Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(System.getenv("MAIL_USERNAME"), System.getenv("MAIL_PASSWORD"));
            }
        });

        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setUsername(System.getenv("MAIL_USERNAME"));
        javaMailSender.setPassword(System.getenv("MAIL_PASSWORD"));
        javaMailSender.setSession(session);

        return javaMailSender;
    }
}