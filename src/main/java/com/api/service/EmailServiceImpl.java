package com.api.service;

import com.api.service.minimal.Info;
import com.api.service.minimal.ProductDetails;
import com.api.utility.Constants;
import com.api.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EmailServiceImpl implements EmailService {

    private static final String OWNER_EMAIL = System.getenv("OWNER_EMAIL");

    private final JavaMailSender javaMailSender;

    @Override
    public void sendSuccessMessage(final Info info) {
        final Function<ProductDetails, String> mapperFunction = productDetails ->
            String.format(
                "%s | R$%s -> R$%s %s", productDetails.getDescription(),
                productDetails.getOldPrice(), productDetails.getNewPrice(),
                calculateVariablePercentage(productDetails.getOldPrice(), productDetails.getNewPrice())
            );

        final String emailMessage =
            "Running job successfully\n\n"+
            String.format("%d/%d products changed\n\n", info.getProductDetailsList().size(), info.getTotalOfProducts())+
            String.format("Elapsed time: %d seconds\n\n", info.getElapsedTimeInSeconds()) +
            info.getProductDetailsList().stream().map(mapperFunction).collect(Collectors.joining("\n"));

        sendAuditEmail(emailMessage);
    }

    private String calculateVariablePercentage(final BigDecimal originalPrice, final BigDecimal newPrice) {
        final BigDecimal fraction = originalPrice.subtract(newPrice).divide(originalPrice, 2, RoundingMode.HALF_EVEN);
        final BigDecimal oneHundred = new BigDecimal("100");
        final BigDecimal percentage = fraction.abs().multiply(oneHundred);
        return (isNegative(fraction) ? ">" : "<")+" ~"+percentage.toBigInteger()+"%";
    }

    private boolean isNegative(final BigDecimal value) {
        return value.signum() == -1;
    }

    @Override
    public void sendFailureMessage(final String message) {
        final String emailMessage =
            "Running job failed\n\n"+
            "With exception: "+message;

        sendAuditEmail(emailMessage);
    }

    private void sendAuditEmail(final String message) {
        final SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(OWNER_EMAIL);
        simpleMailMessage.setTo(OWNER_EMAIL);
        simpleMailMessage.setSubject(
            "Audit email sent with JavaMail - "+LocalDate.now(ZoneId.of(Constants.TIMEZONE))
        );
        simpleMailMessage.setText(message);

        javaMailSender.send(simpleMailMessage);
        log.info("An email with information about updated products has benn sent");
    }
}