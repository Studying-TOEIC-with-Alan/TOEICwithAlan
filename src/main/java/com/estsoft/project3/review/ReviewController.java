package com.estsoft.project3.review;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> saveReview(
        @RequestBody ReviewRequestDto reviewRequestDto,
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = reviewService.getUserByEmail(email);

        ReviewResponseDto responseDto = reviewService.saveReview(user, reviewRequestDto);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviewsSortedByDate(
            true);
        List<ReviewResponseDto> responseList = reviews.stream()
            .map(ReviewResponseDto::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id);
        ReviewResponseDto responseDto = new ReviewResponseDto(review);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
        @PathVariable Long id,
        @RequestBody ReviewRequestDto requestDto,
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = reviewService.getUserByEmail(email);

        Review updatedReview = reviewService.updateReview(id,
            requestDto, user);
        ReviewResponseDto responseDto = new ReviewResponseDto(updatedReview);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id,
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = reviewService.getUserByEmail(email);

        reviewService.deleteReview(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(
        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");

        User user = reviewService.getUserByEmail(email);

        List<Review> reviews = reviewService.getReviewsByUser(user);

        List<ReviewResponseDto> responseList = reviews.stream()
            .map(ReviewResponseDto::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseList);
    }

}
