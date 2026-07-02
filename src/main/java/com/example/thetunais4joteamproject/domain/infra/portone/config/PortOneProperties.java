package com.example.thetunais4joteamproject.domain.infra.portone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "portone")
@Getter
@Setter
public class PortOneProperties {
	private String baseUrl;
	private String apiSecret;
	private String storeId;
	private String channelKey;
	private String webhookSecret;
}
