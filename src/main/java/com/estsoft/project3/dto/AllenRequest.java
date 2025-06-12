package com.estsoft.project3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AllenRequest {
    private Long userId;
    private String category;
    private String inputText;
    private String summary;
}
