package com.ds.bearer.token.cache.service;

import org.springframework.stereotype.Service;

@Service
public interface CacheRemovalService {

	public void removeAllCacheWithTimer();
}