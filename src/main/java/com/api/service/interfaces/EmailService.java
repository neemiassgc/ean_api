package com.api.service.interfaces;

import com.api.service.minimal.Info;

public interface EmailService {

    void sendFailureMessage(String message);

    void sendSuccessMessage(Info info);
}