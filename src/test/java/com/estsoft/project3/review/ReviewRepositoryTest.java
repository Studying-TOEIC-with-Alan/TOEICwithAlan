package com.estsoft.project3.review;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
@Transactional
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .email(email)
                    .nickname("tester")
                    .role(Role.ROLE_USER)
                    .isActive("Y")
                    .provider("google")
                    .build()
            ));
    }

    @Test
    void findByUser_returnsReviewsOfUser() {
        User user = createUser("user@example.com");

        Review review1 = Review.builder()
            .title("Title 1")
            .content("Content 1")
            .user(user)
            .build();

        Review review2 = Review.builder()
            .title("Title 2")
            .content("Content 2")
            .user(user)
            .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        List<Review> reviews = reviewRepository.findByUser(user);

        assertThat(reviews).isNotNull();
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting("title").containsExactlyInAnyOrder("Title 1", "Title 2");
    }

}