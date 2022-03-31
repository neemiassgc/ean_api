package com.xyz.ean.pojo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
public final class SessionInstance {

    private final String sessionId;
    private final String ajaxIdentifier;

    public static SessionInstance EMPTY_SESSION = new SessionInstance();

    private SessionInstance() {
        this("", "");
    }

    public String getAjaxIdentifier() {
        return "PLUGIN="+ ajaxIdentifier;
    }

    @Override
    public String toString() {
        return "SessionInstance [sessionId="+sessionId+", "+this.getAjaxIdentifier()+"]";
    }
}
