package com.ds.bearer.token.cache.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class CacheRemovalServiceImpl implements CacheRemovalService {

	private final CacheManager cacheManager;
	
	@Override
	public void removeAllCacheWithTimer() {
        cacheManager.getCacheNames().parallelStream().forEach(name -> cacheManager.getCache(name).clear());
	}
}