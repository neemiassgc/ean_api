package com.api.component;

import com.api.entity.Product;
import com.api.service.CacheManager;
import com.api.utility.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class CachingFilter implements Filter {

    @Autowired
    private CacheManager<Product, UUID> productCacheManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);

        final HttpServletResponse servletResponse = (HttpServletResponse)  response;
        if (servletResponse.getStatus() == HttpStatus.BAD_REQUEST.value()
            || servletResponse.getStatus() == HttpStatus.NOT_FOUND.value()) return;

        final HttpServletRequest servletRequest = (HttpServletRequest) request;
        if (servletRequest.getRequestURI().contains("/api/prices")) {
            servletResponse.addHeader("Cache-Control", "max-age=" + calculateCacheControl());
            return;
        }

        final String freshEtag = productCacheManager.getRef().toString().replace("-", "");
        servletResponse.addHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
        servletResponse.addHeader("ETag", freshEtag);
    }

    private long calculateCacheControl() {
        final ZoneId timezone = ZoneId.of(Constants.TIMEZONE);
        final LocalDate tomorrow = LocalDate.now(timezone).plusDays(1);
        final LocalTime fiveAm = LocalTime.of(5, 0);
        final ZonedDateTime tomorrowAtFiveAm = ZonedDateTime.of( LocalDateTime.of(tomorrow, fiveAm), timezone);
        return ChronoUnit.SECONDS.between(ZonedDateTime.now(timezone), tomorrowAtFiveAm);
    }
}
