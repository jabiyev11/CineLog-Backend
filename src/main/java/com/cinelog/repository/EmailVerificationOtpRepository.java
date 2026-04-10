package com.cinelog.repository;

import com.cinelog.entity.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {

    Optional<EmailVerificationOtp> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);
}
