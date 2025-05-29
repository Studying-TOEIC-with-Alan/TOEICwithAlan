package com.estsoft.project3.review;

import com.estsoft.project3.Image.Image;
import com.estsoft.project3.Image.ImageDto;
import com.estsoft.project3.Image.ImageRepository;
import com.estsoft.project3.Image.ImageStorageService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;

    @Transactional
    public ReviewResponseDto saveReview(ReviewRequestDto requestDto) {
        Review review = requestDto.toEntity();

        reviewRepository.save(review);

        if (requestDto.getImages() != null) {
            for (ImageDto dto : requestDto.getImages()) {
                Image image = dto.toEntity(review);
                imageRepository.save(image);
            }
        }

        return new ReviewResponseDto(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));
    }

    public Review updateReview(Long id, ReviewRequestDto requestDto) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        review.update(requestDto.getTitle(), requestDto.getContent());
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        List<Image> images = review.getImages();

        if (images != null && !images.isEmpty()) {
            List<String> imageKeys = images.stream()
                .map(Image::getFilename)
                .collect(Collectors.toList());

            imageStorageService.deleteImagesByKeys(imageKeys);
        }

        reviewRepository.delete(review);
    }

}
