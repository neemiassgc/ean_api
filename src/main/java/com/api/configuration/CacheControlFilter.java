package com.api.configuration;

import com.api.component.Constants;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.*;

@Component
public class CacheControlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final ZoneId zoneId = ZoneId.of(Constants.TIMEZONE);
        final LocalDate nextDayDate = LocalDate.now(zoneId).plusDays(1);
        final LocalTime fiveAM = LocalTime.of(5, 0);
        final ZonedDateTime cacheControlDate = ZonedDateTime.of(nextDayDate, fiveAM, zoneId);
        final long futureMillis = cacheControlDate.toInstant().toEpochMilli();
        final long nowMillis = Instant.now().toEpochMilli();
        final long usefulMillis = futureMillis - nowMillis;

        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        CacheControl cacheControl = CacheControl.maxAge(Duration.ofMillis(usefulMillis));

        httpServletResponse.addHeader("Cache-Control", cacheControl.getHeaderValue());
        chain.doFilter(request, httpServletResponse);
    }
}
