package com.api.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.headers().cacheControl().disable();

        http.authorizeRequests().mvcMatchers("/hit").permitAll();
        http.authorizeRequests().mvcMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        http.authorizeRequests().anyRequest().hasAnyAuthority("SCOPE_admin");
        http.httpBasic().disable();
        http.oauth2ResourceServer().jwt();
    }
}