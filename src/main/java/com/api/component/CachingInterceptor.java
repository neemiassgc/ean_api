package com.api.component;

import com.api.entity.Product;
import com.api.service.CacheManager;
import com.api.utility.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CachingInterceptor implements HandlerInterceptor {

    private final CacheManager<Product, UUID> productCacheManager;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final Optional<String> staleEtagOptional = Optional.ofNullable(request.getHeader("If-None-Match"));
        if (staleEtagOptional.isEmpty()) return true;

        final String freshEtag = productCacheManager.getRef().toString().replace("-", "");
        final String staleEtag = staleEtagOptional.get();
        if (!staleEtag.equals(freshEtag)) return true;

        response.addHeader("ETag", staleEtag);
        response.setStatus(304);
        return false;
    }
}