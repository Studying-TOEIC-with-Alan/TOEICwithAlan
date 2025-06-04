package com.estsoft.project3.contact;

import com.estsoft.project3.Image.ImageDto;
import com.estsoft.project3.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    private String content;
    private String title;
    private List<ImageDto> images;

    public Contact toEntity(User user) {
        return Contact.builder()
            .user(user)
            .content(content)
            .title(title)
            .build();
    }
}