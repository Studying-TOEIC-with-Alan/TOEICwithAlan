package com.estsoft.project3.contact;

import com.estsoft.project3.Image.ImageDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequestDto {

    private Long userId;
    private String title;
    private String content;
    private Contact.Status status;
    private List<ImageDto> images;

    public Contact toEntity() {
        return Contact.builder()
            .userId(userId)
            .title(title)
            .content(content)
            .status(status != null ? status : Contact.Status.PENDING)
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .build();
    }
}