package com.estsoft.project3.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.file.FileDto;
import com.estsoft.project3.file.FileStorageService;
import com.estsoft.project3.file.ReviewFileRepository;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
@RequiredArgsConstructor
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewFileRepository reviewFileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public User createUser(String email) {
        return userRepository.findByEmailAndIsActive(email, "Y").orElseGet(() ->
            userRepository.save(User.builder()
                .email(email)
                .nickname("tester")
                .role(Role.ROLE_USER)
                .isActive("Y")
                .provider("google")
                .build()));
    }

    @Test
    @DisplayName("리뷰 저장 및 이미지 저장 테스트")
    void saveReviewTest() {
        User user = createUser("test@example.com");

        List<FileDto> images = List.of(
            new FileDto("file1.jpg", "path/to/file1.jpg"),
            new FileDto("file2.jpg", "path/to/file2.jpg")
        );
        ReviewRequestDto requestDto = new ReviewRequestDto("리뷰 내용", "리뷰 제목", images);

        ReviewResponseDto responseDto = reviewService.saveReview(user, requestDto);

        assertThat(responseDto.getTitle()).isEqualTo("리뷰 제목");
        assertThat(responseDto.getContent()).isEqualTo("리뷰 내용");

        Review saved = reviewRepository.findById(responseDto.getReviewId()).orElseThrow();
        if (saved.getImages() != null) {
            assertThat(saved.getImages()).hasSize(2);
        }
    }

    @Test
    @DisplayName("전체 리뷰 조회 정렬 테스트")
    void getAllReviewsSortedByDateTest() throws InterruptedException {
        reviewRepository.deleteAll();
        User user = createUser("test@example.com");

        Review r1 = reviewRepository.save(Review.builder()
            .title("first")
            .content("first content")
            .user(user)
            .build());

        Thread.sleep(10);

        Review r2 = reviewRepository.save(Review.builder()
            .title("second")
            .content("second content")
            .user(user)
            .build());

        List<Review> newestFirst = reviewService.getAllReviewsSortedByDate(true);
        assertThat(newestFirst.get(0).getReviewId()).isEqualTo(r2.getReviewId());

        List<Review> oldestFirst = reviewService.getAllReviewsSortedByDate(false);
        assertThat(oldestFirst.get(0).getReviewId()).isEqualTo(r1.getReviewId());
    }

    @Test
    @DisplayName("ID로 리뷰 단건 조회 테스트")
    void getReviewByIdTest() {
        User user = createUser("test@example.com");

        Review review = reviewRepository.save(Review.builder()
            .title("title")
            .content("content")
            .user(user)
            .build());

        Review found = reviewService.getReviewById(review.getReviewId());

        assertThat(found.getTitle()).isEqualTo("title");
        assertThat(found.getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("리뷰 수정 테스트")
    void updateReviewTest() {
        User user = createUser("test@example.com");
        Review review = reviewRepository.save(Review.builder()
            .title("old title")
            .content("old content")
            .user(user)
            .build());

        List<FileDto> images = List.of(new FileDto("newfile.jpg", "new/path/file.jpg"));
        ReviewRequestDto updateDto = new ReviewRequestDto("new content", "new title", images);

        Review updated = reviewService.updateReview(review.getReviewId(), updateDto, user);

        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("new title");
        assertThat(updated.getContent()).isEqualTo("new content");

        if (updated.getImages() != null) {
            assertThat(updated.getImages())
                .anyMatch(img -> "newfile.jpg".equals(img.getFilename()));
        }
    }

    @Test
    @DisplayName("리뷰 삭제 권한 및 삭제 테스트")
    void deleteReviewTest() {
        User user = createUser("test@example.com");
        Review review = reviewRepository.save(Review.builder()
            .title("to be deleted")
            .content("content")
            .user(user)
            .build());

        reviewService.deleteReview(review.getReviewId(), user);

        assertThat(reviewRepository.findById(review.getReviewId())).isEmpty();
    }

    @Test
    @DisplayName("사용자별 리뷰글 조회 테스트")
    void getReviewsByUserTest() {
        User user = createUser("test@example.com");

        reviewRepository.save(Review.builder()
            .title("user's review")
            .content("content")
            .user(user)
            .build());

        List<Review> reviews = reviewService.getReviewsByUser(user);

        assertThat(reviews).isNotEmpty();
        assertThat(reviews.get(0).getUser().getEmail()).isEqualTo("test@example.com");
    }
}
