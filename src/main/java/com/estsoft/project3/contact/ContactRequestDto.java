package com.estsoft.project3.contact;

import com.estsoft.project3.domain.User;
import com.estsoft.project3.file.FileDto;
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
    private List<FileDto> images;

    public Contact toEntity(User user) {
        return Contact.builder()
            .user(user)
            .content(content)
            .title(title)
            .build();
    }
}