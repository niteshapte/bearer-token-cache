package com.ds.bearer.token.cache.service;

import org.springframework.stereotype.Service;

import com.ds.bearer.token.cache.dto.BearerTokenDTO;

@Service
public interface BearerTokenService {
	
	public BearerTokenDTO bearerToken();
}