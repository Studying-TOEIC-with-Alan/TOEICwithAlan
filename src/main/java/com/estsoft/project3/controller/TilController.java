package com.estsoft.project3.controller;

import com.estsoft.project3.dto.TilRequest;
import com.estsoft.project3.service.TilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TilController {
    private final TilService tilService;

    public TilController(TilService tilService) {
        this.tilService = tilService;
    }

    @PostMapping("/api/til")
    public ResponseEntity<Void> insertTIL (@RequestBody TilRequest request) {
        tilService.insertTIL(request);
        return ResponseEntity.ok().build();
    }
}
