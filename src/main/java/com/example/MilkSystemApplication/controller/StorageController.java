package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.service.CloudStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    @Autowired
    private CloudStorageService cloudStorageService;

    @GetMapping("/**")
    public ResponseEntity<byte[]> serveFile(HttpServletRequest request) {
        String fullPath = request.getRequestURI();
        // Remove "/api/storage/" prefix
        String relativePath = fullPath.replaceFirst("^/api/storage/", "");

        byte[] content = cloudStorageService.getFile(relativePath);
        if (content == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        if (relativePath.endsWith(".pdf")) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", relativePath.substring(relativePath.lastIndexOf('/') + 1));
        } else if (relativePath.endsWith(".jpg") || relativePath.endsWith(".jpeg")) {
            headers.setContentType(MediaType.IMAGE_JPEG);
        } else if (relativePath.endsWith(".png")) {
            headers.setContentType(MediaType.IMAGE_PNG);
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
