package com.example.MilkSystemApplication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CloudStorageServiceImpl implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudStorageServiceImpl.class);

    @Value("${app.storage.base-dir:./cloud-storage}")
    private String baseDir;

    @Value("${app.storage.public-base-url:/api/storage}")
    private String publicBaseUrl;

    @Override
    public String uploadFile(String directory, String fileName, byte[] content, String contentType) {
        try {
            Path targetDirPath = Paths.get(baseDir, directory);
            if (!Files.exists(targetDirPath)) {
                Files.createDirectories(targetDirPath);
            }

            Path filePath = targetDirPath.resolve(fileName);
            Files.write(filePath, content);

            // Construct accessible URL
            String normalizedDir = directory.replace("\\", "/");
            if (normalizedDir.startsWith("/")) normalizedDir = normalizedDir.substring(1);
            
            logger.info("File successfully stored to cloud storage: {}/{}", normalizedDir, fileName);
            return publicBaseUrl + "/" + normalizedDir + "/" + fileName;
        } catch (IOException e) {
            logger.error("Failed to store file in cloud storage", e);
            throw new RuntimeException("Could not store file in cloud storage: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getFile(String relativePath) {
        try {
            Path filePath = Paths.get(baseDir, relativePath);
            if (!Files.exists(filePath)) {
                return null;
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("Error reading file from cloud storage: {}", relativePath, e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(baseDir, relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.error("Error deleting file from cloud storage: {}", relativePath, e);
            return false;
        }
    }
}
