package com.example.thetunais4joteamproject.global.config;

import com.example.thetunais4joteamproject.global.error.ErrorCode;
import com.example.thetunais4joteamproject.global.error.ErrorResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException, ServletException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication == null ? null : authentication.getPrincipal();
		Object authorities = authentication == null ? null : authentication.getAuthorities();

		log.warn(
			"Access denied. method={}, uri={}, principal={}, authorities={}",
			request.getMethod(),
			request.getRequestURI(),
			principal,
			authorities
		);

		writeForbiddenResponse(response);
	}

	private void writeForbiddenResponse(HttpServletResponse response) throws IOException {
		ErrorCode errorCode = ErrorCode.FORBIDDEN;
		ErrorResponse errorResponse = ErrorResponse.from(errorCode);
		String responseBody = "{\"status\":"
			+ errorResponse.status()
			+ ",\"message\":\""
			+ errorResponse.message()
			+ "\"}";

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseBody);
	}
}
