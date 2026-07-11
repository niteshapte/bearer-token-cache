package com.ds.bearer.token.cache.controller;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ds.bearer.token.cache.dto.BearerTokenDTO;
import com.ds.bearer.token.cache.service.BearerTokenService;
import com.ds.bearer.token.cache.service.CacheRemovalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("token")
public class BearerTokenCacheController {
	
	private final BearerTokenService bearerTokenService;
	
	private final CacheRemovalService tokenCacheRemovalService;
	
	Timer timerOneTime;
	
	@Cacheable(value = "token", key = "#requestId")
    @GetMapping(value = "/cache/", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<BearerTokenDTO> tokenForPartner(String requestId) {
		BearerTokenDTO tokenOutboundResponseDTO = bearerTokenService.bearerToken();
		timerOneTime = new Timer();
		
		Integer expiryTime = Integer.parseInt(tokenOutboundResponseDTO.getExpires_in());
		
		log.info("Request ID {}. Cache eviction after: {}", requestId, expiryTime);
        timerOneTime.schedule(new OneTimeEvictionTask(), expiryTime * 1000);
		
		return Mono.just(tokenOutboundResponseDTO);
	}
	
	class OneTimeEvictionTask extends TimerTask {
		
		public void run() {
			log.info("Time's up!");
			tokenCacheRemovalService.removeAllCacheWithTimer();
			timerOneTime.cancel();
		}
	}
}