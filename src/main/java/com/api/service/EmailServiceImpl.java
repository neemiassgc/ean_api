package com.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EmailServiceImpl implements EmailService {

    private static final String OWNER_EMAIL = System.getenv("OWNER_EMAIL");

    private final JavaMailSender javaMailSender;

    @Override
    public void sendAuditEmail(String message) {

    }
}