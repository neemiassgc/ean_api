package com.xyz.ean.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Vector;

@Getter
@Setter
public class ErrorTemplate {

    private List<String> reasons;
    private HttpStatus status;

    public ErrorTemplate(final HttpStatus status) {
        this.status = status;
        this.reasons = new Vector<>();
    }

    public ErrorTemplate() {
        this(null);
    }

    public void addReasons(final String... reasons) {
        this.reasons.addAll(List.of(reasons));
    }

    @Override
    public String toString() {
        final String template = "ErrorTemplate{status=%s, reasons=%s}";
        return String.format(
            template,
            status.value()+" "+status.getReasonPhrase(),
            String.join(";", this.reasons)
        );
    }
}
