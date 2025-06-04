package com.estsoft.project3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllenRequest {
    private Long userId;
    private String category;
    private String inputText;
    private String summary;
}
