package com.estsoft.project3.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import com.estsoft.project3.review.Review;
import com.estsoft.project3.review.ReviewRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@Import(MockS3ClientConfig.class)
@ActiveProfiles("test")
class ReviewFileRepositoryTest {

    @Autowired
    private ReviewFileRepository reviewFileRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("조회")
    void findByReviewId() {
        // given
        User user = User.builder()
            .provider("test-provider")
            .email("tester@example.com")
            .nickname("tester")
            .role(Role.ROLE_USER)
            .isActive("Y")
            .build();

        user = userRepository.save(user);

        Review review = new Review();
        review.setUser(user);
        Review savedReview = reviewRepository.save(review);

        ReviewFile file1 = new ReviewFile();
        file1.setReview(savedReview);
        file1.setFilename("file1.jpg");
        file1.setFilePath("http://example.com/file1.jpg");

        ReviewFile file2 = new ReviewFile();
        file2.setReview(savedReview);
        file2.setFilename("file2.jpg");
        file2.setFilePath("http://example.com/file2.jpg");

        reviewFileRepository.save(file1);
        reviewFileRepository.save(file2);

        // when
        List<ReviewFile> files = reviewFileRepository.findByReview_ReviewId(
            savedReview.getReviewId());

        // then
        assertThat(files).isNotNull();
        assertThat(files).hasSize(2);
        assertThat(files).extracting("Filename")
            .containsExactlyInAnyOrder("file1.jpg", "file2.jpg");
    }
}
