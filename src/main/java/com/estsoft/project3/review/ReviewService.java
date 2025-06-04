package com.estsoft.project3.review;

import com.estsoft.project3.Image.Image;
import com.estsoft.project3.Image.ImageDto;
import com.estsoft.project3.Image.ImageRepository;
import com.estsoft.project3.Image.ImageStorageService;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ImageStorageService imageStorageService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDto saveReview(User user,
        ReviewRequestDto requestDto) {
        try {
            Review review = requestDto.toEntity(user);

            reviewRepository.save(review);

            if (requestDto.getImages() != null) {
                for (ImageDto dto : requestDto.getImages()) {
                    Image image = dto.toEntity(review);
                    imageRepository.save(image);
                }
            }

            return new ReviewResponseDto(review);
        } catch (Exception ex) {
            throw new IllegalArgumentException("리뷰 저장 중 문제가 발생했습니다.");
        }
    }

    public List<Review> getAllReviewsSortedByDate(boolean newestFirst) {
        Sort sort = newestFirst
            ? Sort.by(Sort.Direction.DESC, "createDate")
            : Sort.by(Sort.Direction.ASC, "createDate");
        return reviewRepository.findAll(sort);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public Review updateReview(Long id,
        ReviewRequestDto requestDto, User currentUser) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id=" + id));

        if (!currentUser.isOwner(review)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        review.update(requestDto.getTitle(), requestDto.getContent());
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, User currentUser) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!(currentUser.isOwner(review) || currentUser.isAdmin())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        List<Image> images = review.getImages();

        if (images != null && !images.isEmpty()) {
            List<String> imageKeys = images.stream()
                .map(Image::getFilename)
                .collect(Collectors.toList());

            imageStorageService.deleteImagesByKeys(imageKeys);
        }

        reviewRepository.delete(review);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
