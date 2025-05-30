package com.estsoft.project3.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TilRequest {
    private Long userId;
    private String title;
    private String summary;
}
