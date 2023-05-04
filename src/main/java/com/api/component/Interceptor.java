package com.api.component;

import com.api.entity.Product;
import com.api.service.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Component
public class Interceptor implements HandlerInterceptor {

    @Autowired
    private CacheManager<Product, UUID> productCacheManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final Optional<String> staleEtagOptional = Optional.ofNullable(request.getHeader("If-None-Match"));
        final String freshEtag = productCacheManager.getRef().toString().replace("-", "");
        if (staleEtagOptional.isPresent()) {
            final String staleEtag = staleEtagOptional.get();
            if (staleEtag.equals(freshEtag)) {
                response.addHeader("ETag", staleEtag);
                response.setStatus(304);
                return false;
            }
        }

        response.addHeader("Cache-Control", "no-cache, max-age=0, must-revalidate");
        response.addHeader("ETag", freshEtag);
        return true;
    }
}