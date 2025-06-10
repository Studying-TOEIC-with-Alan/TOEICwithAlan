package com.estsoft.project3.review;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.file.FileDto;
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
    private List<FileDto> images;

    public Review toEntity(User user) {
        return Review.builder()
            .user(user)
            .content(content)
            .title(title)
            .build();
    }
}
