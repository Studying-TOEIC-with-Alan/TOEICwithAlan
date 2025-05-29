package com.estsoft.project3.contact;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactResponseDto {

    private Long contactId;
    private Long userId;
    private String title;
    private String content;
    private Contact.Status status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public ContactResponseDto(Contact contact) {
        this.contactId = contact.getContactId();
        this.userId = contact.getUserId();
        this.title = contact.getTitle();
        this.content = contact.getContent();
        this.status = contact.getStatus();
        this.createDate = contact.getCreateDate();
        this.updateDate = contact.getUpdateDate();
    }
}
