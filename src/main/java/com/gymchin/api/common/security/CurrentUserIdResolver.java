package com.gymchin.api.common.security;

import com.gymchin.api.common.error.AppException;
import com.gymchin.api.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserIdResolver {

    public Long resolve(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw AppException.of(ErrorCode.AUTH_UNAUTHORIZED, "Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        return ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
    }
}
