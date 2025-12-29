package com.gymchin.api.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymchin.api.common.dto.ApiResponse;
import com.gymchin.api.common.error.AppError;
import com.gymchin.api.common.error.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        AppError error = new AppError(ErrorCode.AUTH_UNAUTHORIZED.name(), "Forbidden", null);
        objectMapper.writeValue(response.getWriter(), ApiResponse.failure(error));
    }
}
