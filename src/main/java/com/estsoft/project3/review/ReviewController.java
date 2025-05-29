package com.estsoft.project3.review;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewResponseDto> saveReview(
        @RequestBody ReviewRequestDto reviewRequestDto) {

        ReviewResponseDto responseDto = reviewService.saveReview(reviewRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
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
        @RequestBody ReviewRequestDto requestDto) {

        Review updatedReview = reviewService.updateReview(id, requestDto);
        ReviewResponseDto responseDto = new ReviewResponseDto(updatedReview);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
