package com.estsoft.project3.controller;

import com.estsoft.project3.domain.Role;
import com.estsoft.project3.domain.User;
import com.estsoft.project3.review.Review;
import com.estsoft.project3.review.ReviewResponseDto;
import com.estsoft.project3.review.ReviewService;
import com.estsoft.project3.service.UserService;
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
    private final UserService userService;


    @Autowired
    public ReviewViewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
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
        String email = principal.getAttribute("email");
        User user = reviewService.getUserByEmail(email);
        List<Review> reviews = reviewService.getReviewsByUser(user);

        if (!review.getUser().getEmail().equals(currentUserEmail)) {
            return "redirect:/error";
        }
        model.addAttribute("review", new ReviewResponseDto(review));
        model.addAttribute("reviewId", id);
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("role", String.valueOf(user.getRole()));
        model.addAttribute("nickname", user.getNickname());
        return "review";
    }

    @GetMapping("/reviews")
    public String showAllReviews(@RequestParam(name = "sort", defaultValue = "newest") String sort,
        Model model, @AuthenticationPrincipal OAuth2User principal) {
        boolean newestFirst = sort.equalsIgnoreCase("newest");

        String email = principal.getAttribute("email");
        User user = reviewService.getUserByEmail(email);
        List<Review> review = reviewService.getReviewsByUser(user);

        List<Review> reviews = reviewService.getAllReviewsSortedByDate(newestFirst);

        List<ReviewResponseDto> responseDto = reviews.stream()
            .map(ReviewResponseDto::new)
            .collect(Collectors.toList());

        model.addAttribute("reviews", responseDto);
        model.addAttribute("sort", sort);
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("role", String.valueOf(user.getRole()));
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("user", user);

        return "review-main";
    }

    @GetMapping("/reviews/{id}")
    public String showReviewDetail(@PathVariable Long id, Model model,
        @AuthenticationPrincipal OAuth2User principal) {
        Review review = reviewService.getReviewById(id);
        ReviewResponseDto responseDto = new ReviewResponseDto(review);

        String email = principal.getAttribute("email");
        User user = reviewService.getUserByEmail(email);
        List<Review> reviews = reviewService.getReviewsByUser(user);

        model.addAttribute("review", responseDto);

        String currentUserEmail = principal.getName();
        boolean isOwner = review.getUser().getEmail().equals(currentUserEmail);
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        model.addAttribute("hasAccess", isOwner);
        model.addAttribute("hasDeleteAccess", isOwner || isAdmin);
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("role", String.valueOf(user.getRole()));
        model.addAttribute("nickname", user.getNickname());
        return "review-detail";
    }
}