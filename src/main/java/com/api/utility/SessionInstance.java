package com.api.utility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
