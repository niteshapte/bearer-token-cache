package com.ds.bearer.token.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BearerTokenDTO {

	private String token_type;
	
	private String expires_in;
	
	private String access_token;
}