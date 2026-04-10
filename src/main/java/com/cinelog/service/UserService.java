package com.cinelog.service;

import com.cinelog.dto.MessageResponse;
import com.cinelog.dto.UserProfileResponse;
import com.cinelog.dto.UserStatsResponse;
import com.cinelog.entity.User;
import com.cinelog.entity.WatchLog;
import com.cinelog.exception.ResourceNotFoundException;
import com.cinelog.repository.RatingRepository;
import com.cinelog.repository.EmailVerificationOtpRepository;
import com.cinelog.repository.ReviewLikeRepository;
import com.cinelog.repository.ReviewRepository;
import com.cinelog.repository.UserRepository;
import com.cinelog.repository.WatchLogRepository;
import com.cinelog.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WatchLogRepository watchLogRepository;
    private final RatingRepository ratingRepository;
    private final EmailVerificationOtpRepository emailVerificationOtpRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final WatchlistRepository watchlistRepository;
    private final CurrentUserService currentUserService;

    public UserProfileResponse getProfile(String username) {
        User user = findByUsername(username);
        return new UserProfileResponse(
                user.getUsername(),
                user.getProfilePictureUrl(),
                user.getBio(),
                watchLogRepository.countByUserId(user.getId())
        );
    }

    public UserStatsResponse getStats(String username) {
        User user = findByUsername(username);
        List<WatchLog> watchLogs = watchLogRepository.findByUserId(user.getId());

        String mostWatchedGenre = mostFrequentValue(watchLogs.stream()
                .flatMap(watchLog -> watchLog.getMovie().getGenres().stream())
                .toList());
        String mostWatchedDirector = mostFrequentValue(watchLogs.stream()
                .flatMap(watchLog -> watchLog.getMovie().getDirectors().stream())
                .toList());
        Map<Integer, Long> moviesWatchedPerYear = watchLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getWatchedDate().getYear(), Collectors.counting()));

        return new UserStatsResponse(mostWatchedGenre, mostWatchedDirector, moviesWatchedPerYear);
    }

    @Transactional
    public MessageResponse deleteMyAccount() {
        User user = currentUserService.getCurrentUser();
        reviewLikeRepository.deleteByUserId(user.getId());
        reviewLikeRepository.deleteByReviewUserId(user.getId());
        emailVerificationOtpRepository.deleteByUserId(user.getId());
        ratingRepository.deleteByUserId(user.getId());
        reviewRepository.deleteByUserId(user.getId());
        watchlistRepository.deleteByUserId(user.getId());
        watchLogRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
        return new MessageResponse("Account deleted permanently.");
    }

    private User findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private String mostFrequentValue(List<String> values) {
        return values.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry.comparingByKey(Comparator.naturalOrder())))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
