package com.estsoft.project3.review;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MockS3ClientConfig.class)
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

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
    void saveReview() throws Exception {
        createUser("test@example.com");

        ReviewRequestDto requestDto = new ReviewRequestDto("문의 내용", "문의 제목",
            Collections.emptyList());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        ResultActions resultActions = mockMvc.perform(post("/api/reviews")
            .with(oauth2Login().oauth2User(mockOAuth2User))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto))
        );

        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("문의 제목"))
            .andExpect(jsonPath("$.content").value("문의 내용"));
    }

    @Test
    @DisplayName("리뷰글 전체 조회")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAllReviews() throws Exception {
        // given
        User testUser = createUser("test@example.com");
        Review review = Review.builder()
            .title("title1")
            .content("content1")
            .user(testUser)
            .build();
        reviewRepository.save(review);

        // when
        ResultActions resultActions = mockMvc.perform(get("/api/reviews"));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].title").value("title1"))
            .andExpect(jsonPath("$[0].content").value("content1"));
    }

    @Test
    @DisplayName("ID로 리뷰글 단건 조회")
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReviewById() throws Exception {
        // given
        User testUser = createUser("test@example.com");
        Review review = Review.builder()
            .title("title1")
            .content("content1")
            .user(testUser)
            .build();
        review = reviewRepository.save(review);

        // when
        ResultActions resultActions = mockMvc.perform(
            get("/api/reviews/{id}", review.getReviewId()));

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("title1"))
            .andExpect(jsonPath("$.content").value("content1"));
    }

    @Test
    @DisplayName("리뷰글 수정")
    void updateReview() throws Exception {
        // given
        User user = createUser("test@example.com");
        Review review = Review.builder()
            .title("old title")
            .content("old content")
            .user(user)
            .build();
        review = reviewRepository.save(review);

        ReviewRequestDto updateDto = new ReviewRequestDto("new content", "new title",
            Collections.emptyList());

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        // when
        ResultActions resultActions = mockMvc.perform(
            put("/api/reviews/{id}", review.getReviewId())
                .with(oauth2Login().oauth2User(mockOAuth2User))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        );

        // then
        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("new title"))
            .andExpect(jsonPath("$.content").value("new content"));
    }

    @Test
    @DisplayName("리뷰글 삭제")
    void deleteReview() throws Exception {
        // given
        User user = createUser("test@example.com");
        Review review = Review.builder()
            .title("to be deleted")
            .content("delete this")
            .user(user)
            .build();
        review = reviewRepository.save(review);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "tester");

        OAuth2User mockOAuth2User = new DefaultOAuth2User(
            AuthorityUtils.createAuthorityList("ROLE_USER"),
            attributes,
            "email"
        );

        // when
        ResultActions resultActions = mockMvc.perform(
            delete("/api/reviews/{id}", review.getReviewId())
                .with(oauth2Login().oauth2User(mockOAuth2User))
        );

        // then
        resultActions.andExpect(status().isNoContent());
    }
}