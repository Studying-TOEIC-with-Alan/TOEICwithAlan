package com.estsoft.project3.controller;

import com.estsoft.project3.review.Review;
import com.estsoft.project3.review.ReviewResponseDto;
import com.estsoft.project3.review.ReviewService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewViewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewViewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews/new")
    public String showCreateReviewForm() {

        return "review";
    }

    @GetMapping("/reviews/{id}/edit")
    public String showEditReviewForm(@PathVariable Long id, Model model,
        @AuthenticationPrincipal OAuth2User principal) {

        String currentUserEmail = principal.getAttribute("email");
        Review review = reviewService.getReviewById(id);

        if (!review.getUser().getEmail().equals(currentUserEmail)) {
            return "redirect:/error";
        }
        model.addAttribute("review", new ReviewResponseDto(review));
        model.addAttribute("reviewId", id);
        return "review";
    }

    @GetMapping("/reviews")
    public String showAllReviews(@RequestParam(name = "sort", defaultValue = "newest") String sort,
        Model model) {
        boolean newestFirst = sort.equalsIgnoreCase("newest");

        List<Review> reviews = reviewService.getAllReviewsSortedByDate(newestFirst);

        List<ReviewResponseDto> responseDto = reviews.stream()
            .map(ReviewResponseDto::new)
            .collect(Collectors.toList());

        model.addAttribute("reviews", responseDto);
        model.addAttribute("sort", sort);

        return "review-main";
    }

    @GetMapping("/reviews/{id}")
    public String showReviewDetail(@PathVariable Long id, Model model) {
        Review review = reviewService.getReviewById(id);
        model.addAttribute("review", new ReviewResponseDto(review));
        return "review-detail";
    }
}