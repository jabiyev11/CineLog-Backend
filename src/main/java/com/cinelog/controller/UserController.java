package com.cinelog.controller;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.UserProfileResponse;
import com.cinelog.dto.UserStatsResponse;
import com.cinelog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{username}")
    public UserProfileResponse getUserProfile(@PathVariable String username) {
        return userService.getProfile(username);
    }

    @GetMapping("/users/{username}/stats")
    public UserStatsResponse getUserStats(@PathVariable String username) {
        return userService.getStats(username);
    }

    @DeleteMapping("/users/me")
    public MessageResponse deleteMyAccount() {
        return userService.deleteMyAccount();
    }
}
