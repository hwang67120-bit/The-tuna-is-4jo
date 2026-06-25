package com.example.thetunais4joteamproject.domain.infra.portone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class PortOneConfig {

	private final PortOneProperties properties;

	@Bean
	public RestClient portOneRestClient() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(3000);  // PortOne 서버와 연결 자체가 안되면 3초 후 실패
		requestFactory.setReadTimeout(5000);     // 연결 성공 후 응답이 안 오면 5초 후 실패

		// RestClient가 제공하는 생성 방식
		return RestClient.builder()
			.requestFactory(requestFactory)
			.baseUrl(properties.getBaseUrl())
			.defaultHeader("Authorization", "PortOne " + properties.getApiSecret())
			.build();
	}
}
