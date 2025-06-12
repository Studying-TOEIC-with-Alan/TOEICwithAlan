package com.estsoft.project3.controller;

import com.estsoft.project3.dto.TilRequest;
import com.estsoft.project3.service.TilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/api/til/{id}")
    public ResponseEntity<Void> updateTIL (@PathVariable("id") long id, @RequestBody TilRequest request) {
        tilService.updateTIL(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/til/{id}")
    public ResponseEntity<Void> deleteTIL (@PathVariable("id") long id) {
        tilService.deleteTIL(id);
        return ResponseEntity.ok().build();
    }
}
