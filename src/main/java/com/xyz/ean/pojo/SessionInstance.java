package com.xyz.ean.pojo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public final class SessionInstance {

    private final String sessionId;
    private final String ajaxIdentifier;

    private SessionInstance() {
        this("", "");
    }

    public String getAjaxIdentifier() {
        return "PLUGIN="+ ajaxIdentifier;
    }
}
