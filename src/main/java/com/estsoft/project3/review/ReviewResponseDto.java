package com.estsoft.project3.review;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewResponseDto {

    private Long reviewId;
    private Long userId;
    private String nickname;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public ReviewResponseDto(Review review) {
        this.reviewId = review.getReviewId();
        this.userId = review.getUserId();
        this.content = review.getContent();
        this.title = review.getTitle();
        this.createDate = review.getCreateDate();
        this.updateDate = review.getUpdateDate();

        this.nickname = review.getNickname();

    }
}
