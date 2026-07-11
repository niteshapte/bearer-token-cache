package com.ds.bearer.token.cache.service;

import org.springframework.stereotype.Service;

import com.ds.bearer.token.cache.dto.BearerTokenDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class BearerTokenServiceImpl implements BearerTokenService {

	public BearerTokenDTO bearerToken() {
		BearerTokenDTO response = new BearerTokenDTO();
		response.setAccess_token("ACCESS_TOKEN");
		response.setExpires_in("3600");
		response.setToken_type("Bearer");
		return response;
	}
}