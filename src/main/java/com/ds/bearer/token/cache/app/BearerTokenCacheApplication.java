package com.ds.bearer.token.cache.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.reactive.config.EnableWebFlux;

@ComponentScan("com.ds.bearer.token.cache*")
@EnableWebFlux
@EnableCaching
@EnableAutoConfiguration
@SpringBootApplication
public class BearerTokenCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(BearerTokenCacheApplication.class, args);
	}
}