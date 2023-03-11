package com.api.configuration;

import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CacheControlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final CacheControl cacheControl = CacheControl.noStore();

        httpServletResponse.addHeader("Cache-Control", cacheControl.getHeaderValue());
        chain.doFilter(request, httpServletResponse);
    }
}
