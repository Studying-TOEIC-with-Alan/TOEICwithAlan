package com.estsoft.project3.review;

import com.estsoft.project3.Image.ImageDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

    private Long userId;
    private String nickname;
    private String content;
    private String title;
    private List<ImageDto> images;

    public Review toEntity() {
        return Review.builder()
            .userId(userId)
            .nickname(nickname)
            .content(content)
            .title(title)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }
}
