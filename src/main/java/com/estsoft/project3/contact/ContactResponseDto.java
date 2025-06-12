package com.estsoft.project3.contact;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactResponseDto {

    private Long contactId;
    private Long userId;
    private String nickname;
    private Long grade;
    private String title;
    private String content;
    private String status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public ContactResponseDto(Contact contact) {
        this.contactId = contact.getContactId();
        this.userId = contact.getUser().getUserId();
        this.title = contact.getTitle();
        this.content = contact.getContent();
        this.status = contact.getStatus() != null ? contact.getStatus().name() : "OPEN";
        this.createDate = contact.getCreateDate();
        this.updateDate = contact.getUpdateDate();
        this.nickname = contact.getUser().getNickname();
        this.grade = contact.getUser().getGrade();
    }
}
