package com.projetoDados.HUB.Backend.Redis.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projetoDados.HUB.Backend.Redis.Model.Upload;
import com.projetoDados.HUB.Backend.Redis.Service.UploadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping
    public ResponseEntity<Void> createUpload(@RequestBody Upload upload) {
        uploadService.createUpload(upload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Upload> getUpload(@PathVariable String gameId) {

        Upload upload = uploadService.getUpload(gameId);

        if (upload == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(upload);
    }

    @PutMapping("/{gameId}")
    public ResponseEntity<Void> updateUpload(
            @PathVariable String gameId,
            @RequestParam String status) {

        uploadService.updateUpload(gameId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteUpload(@PathVariable String gameId) {

        uploadService.deleteUpload(gameId);
        return ResponseEntity.ok().build();
    }
}