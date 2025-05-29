package com.estsoft.project3.Image;

import com.estsoft.project3.contact.Contact;
import com.estsoft.project3.review.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDto {

    private String filePath;
    private String filename;

    public Image toEntity(Review review) {
        return Image.builder()
            .filePath(this.filePath)
            .filename(this.filename)
            .review(review)
            .build();
    }

    public Image toEntity(Contact contact) {
        return Image.builder()
            .filename(this.filename)
            .filePath(this.filePath)
            .contact(contact)
            .build();
    }
}
