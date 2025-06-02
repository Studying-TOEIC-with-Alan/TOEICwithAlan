package com.estsoft.project3.review;

import com.estsoft.project3.Image.ImageDto;
import com.estsoft.project3.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

    private String content;
    private String title;
    private List<ImageDto> images;

    public com.estsoft.project3.review.Review toEntity(User user) {
        return com.estsoft.project3.review.Review.builder()
            .user(user)
            .content(content)
            .title(title)
            .build();
    }
}
