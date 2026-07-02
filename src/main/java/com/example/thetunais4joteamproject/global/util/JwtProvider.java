package com.example.thetunais4joteamproject.global.util;

import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String ROLE_CLAIM = "role";

	private final SecretKey secretKey;
	private final long accessTokenExpiration;

	public JwtProvider(
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.access-token-expiration}") long accessTokenExpiration
	) {
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
		this.accessTokenExpiration = accessTokenExpiration;
	}

	public String createAccessToken(Long memberId, MemberRole role) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + accessTokenExpiration);

		return Jwts.builder()
			.subject(String.valueOf(memberId))
			.claim(ROLE_CLAIM, role.name())
			.issuedAt(now)
			.expiration(expiration)
			.signWith(secretKey)
			.compact();
	}

	public String extractToken(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			throw BusinessException.from(ErrorCode.UNAUTHORIZED);
		}
		if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
			throw BusinessException.from(ErrorCode.UNAUTHORIZED);
		}

		return authorizationHeader.substring(BEARER_PREFIX.length());
	}

	public void validateToken(String token) {
		parseClaims(token);
	}

	public Long getMemberId(String token) {
		Claims claims = parseClaims(token);

		return Long.valueOf(claims.getSubject());
	}

	public MemberRole getRole(String token) {
		Claims claims = parseClaims(token);

		return MemberRole.valueOf(claims.get(ROLE_CLAIM, String.class));
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (JwtException | IllegalArgumentException exception) {
			throw BusinessException.from(ErrorCode.UNAUTHORIZED);
		}
	}

	public Authentication getAuthentication(String token) {

		validateToken(token);

		Long memberId = getMemberId(token);
		MemberRole role = getRole(token);

		return new UsernamePasswordAuthenticationToken(
			memberId,
			null,
			List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
		);
	}
}
