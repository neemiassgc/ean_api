package com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableAutoConfiguration(exclude = HypermediaAutoConfiguration.class)
@EnableCaching
public class SavegLocalMarketApplication {

	public static void main(String[] args) {

		SpringApplication.run(SavegLocalMarketApplication.class, args);
	}
}
