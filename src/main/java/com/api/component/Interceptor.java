package com.api.component;

import com.api.entity.Product;
import com.api.service.CacheManager;
import com.api.utility.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
public class Interceptor implements HandlerInterceptor {

    @Autowired
    private CacheManager<Product, UUID> productCacheManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (checkIfETagsMatch(request, response)) return false;

        if (!addCacheControlIfURIContainsPrices(request, response)) {
            final String freshEtag = productCacheManager.getRef().toString().replace("-", "");
            response.addHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
            response.addHeader("ETag", freshEtag);
        }
        return true;
    }

    private boolean addCacheControlIfURIContainsPrices(final HttpServletRequest request, final HttpServletResponse response) {
        if (request.getRequestURI().equals("/api/prices")) {
            final ZoneId timezone = ZoneId.of(Constants.TIMEZONE);
            final LocalDate tomorrow = LocalDate.now(timezone).plusDays(1);
            final LocalTime fiveAm = LocalTime.of(5, 0);
            final ZonedDateTime tomorrowAtFiveAm = ZonedDateTime.of( LocalDateTime.of(tomorrow, fiveAm), timezone);
            final long differenceInSeconds = ChronoUnit.SECONDS.between(ZonedDateTime.now(timezone), tomorrowAtFiveAm);
            response.addHeader("Cache-Control", "max-age="+differenceInSeconds);
            return true;
        }
        return false;
    }

    private boolean checkIfETagsMatch(final HttpServletRequest request, final HttpServletResponse response) {
        final Optional<String> staleEtagOptional = Optional.ofNullable(request.getHeader("If-None-Match"));
        final String freshEtag = productCacheManager.getRef().toString().replace("-", "");
        if (staleEtagOptional.isPresent()) {
            final String staleEtag = staleEtagOptional.get();
            if (staleEtag.equals(freshEtag)) {
                response.addHeader("ETag", staleEtag);
                response.setStatus(304);
                return true;
            }
        }
        return false;
    }
}