package com.cinelog.service;

import com.cinelog.entity.User;
import com.cinelog.exception.UnauthorizedException;
import com.cinelog.repository.UserRepository;
import com.cinelog.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            throw new UnauthorizedException("Missing or invalid JWT.");
        }
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("Missing or invalid JWT."));
    }
}
