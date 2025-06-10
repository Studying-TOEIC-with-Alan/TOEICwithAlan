package com.estsoft.project3.file;

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
public class FileDto {

    private String filePath;
    private String filename;

    public ReviewFile toEntity(Review review) {
        return ReviewFile.builder()
            .filePath(this.filePath)
            .filename(this.filename)
            .review(review)
            .build();
    }

    public ContactFile toEntity(Contact contact) {
        return com.estsoft.project3.file.ContactFile.builder()
            .filename(this.filename)
            .filePath(this.filePath)
            .contact(contact)
            .build();
    }
}
