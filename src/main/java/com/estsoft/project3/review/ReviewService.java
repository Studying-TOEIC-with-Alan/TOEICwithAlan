package com.estsoft.project3.review;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.file.FileDto;
import com.estsoft.project3.file.FileStorageService;
import com.estsoft.project3.file.ReviewFile;
import com.estsoft.project3.file.ReviewFileRepository;
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
    private final FileStorageService fileStorageService;
    private final ReviewFileRepository reviewFileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDto saveReview(User user,
        ReviewRequestDto requestDto) {
        try {
            Review review = requestDto.toEntity(user);

            reviewRepository.save(review);

            if (requestDto.getImages() != null) {
                for (FileDto dto : requestDto.getImages()) {
                    ReviewFile image = dto.toEntity(review);
                    reviewFileRepository.save(image);
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
    public Review updateReview(Long reviewId, ReviewRequestDto dto, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("리뷰 작성자만 수정할 수 있습니다.");
        }

        review.update(dto.getTitle(), dto.getContent());

        List<ReviewFile> existingImages = reviewFileRepository.findByReview_ReviewId(reviewId);
        List<String> newFilePaths = dto.getImages().stream()
            .map(com.estsoft.project3.file.FileDto::getFilePath)
            .collect(Collectors.toList());

        List<String> s3KeysToDelete = existingImages.stream()
            .filter(img -> !newFilePaths.contains(img.getFilePath()))
            .map(ReviewFile::getFilePath)
            .collect(Collectors.toList());

        fileStorageService.deleteImagesByKeys(s3KeysToDelete);

        for (ReviewFile image : existingImages) {
            if (!newFilePaths.contains(image.getFilePath())) {
                reviewFileRepository.delete(image);
            }
        }

        for (FileDto fileDto : dto.getImages()) {
            boolean alreadyExists = existingImages.stream()
                .anyMatch(img -> img.getFilePath().equals(fileDto.getFilePath()));

            if (!alreadyExists) {
                ReviewFile newImage = new ReviewFile();
                newImage.setFilename(fileDto.getFilename());
                newImage.setFilePath(fileDto.getFilePath());
                newImage.setReview(review);
                reviewFileRepository.save(newImage);
            }
        }

        return review;
    }

    @Transactional
    public void deleteReview(Long reviewId, User currentUser) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        if (!(currentUser.isOwner(review) || currentUser.isAdmin())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        List<ReviewFile> images = review.getImages();

        if (images != null && !images.isEmpty()) {
            List<String> imageKeys = images.stream()
                .map(ReviewFile::getFilename)
                .collect(Collectors.toList());

            fileStorageService.deleteImagesByKeys(imageKeys);
        }

        reviewRepository.delete(review);
    }

    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndIsActive(email,"Y")
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
