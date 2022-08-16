package com.api.service;

import com.api.pojo.Constants;
import com.api.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EmailServiceImpl implements EmailService {

    private static final String OWNER_EMAIL = System.getenv("OWNER_EMAIL");

    private final JavaMailSender javaMailSender;

    @Override
    public void sendAuditEmail(final String message) {
        final SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(OWNER_EMAIL);
        simpleMailMessage.setTo(OWNER_EMAIL);
        simpleMailMessage.setSubject(
            "Audit email sent with JavaMail - "+LocalDate.now(ZoneId.of(Constants.TIMEZONE))
        );
        simpleMailMessage.setText(message);

        javaMailSender.send(simpleMailMessage);
    }
}