package com.estsoft.project3.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactStatusUpdateDto {

    private String status;

    public Contact.Status toStatusEnum() {
        try {
            return Contact.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("올바르지 않은 상태 값입니다: " + status);
        }
    }
}
