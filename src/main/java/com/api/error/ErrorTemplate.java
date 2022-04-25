package com.api.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Vector;

@Getter
public final class ErrorTemplate {

    private final List<String> reasons;
    private final HttpStatus status;

    public ErrorTemplate(final HttpStatus status, final List<String> reasons) {
        this.status = status;
        this.reasons = reasons;
    }

    @Override
    public String toString() {
        final String template = "ErrorTemplate{status=%s, reasons=[%s]}";
        return String.format(
            template,
            status.value()+" "+status.getReasonPhrase(),
            String.join(", ", this.reasons)
        );
    }
}
