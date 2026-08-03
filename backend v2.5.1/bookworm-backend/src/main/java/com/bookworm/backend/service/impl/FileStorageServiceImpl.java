package com.bookworm.backend.service.impl;

import com.bookworm.backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Local-disk storage under bookworm.upload.dir, servable at /uploads/**
 * (see config.WebConfig's resource handler). Good enough for a single-node
 * deployment; swapping this for S3/GCS later is a one-file change since
 * every caller only depends on the FileStorageService interface.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootDir;

    public FileStorageServiceImpl(@Value("${bookworm.upload.dir:uploads}") String uploadDir) {
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not initialize upload directory: " + rootDir, ex);
        }
    }

    @Override
    public String store(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + extension;

        Path targetSubdir = rootDir.resolve(subfolder).normalize();
        if (!targetSubdir.startsWith(rootDir)) {
            throw new IllegalArgumentException("Invalid subfolder");
        }

        try {
            Files.createDirectories(targetSubdir);
            Path target = targetSubdir.resolve(filename);
            file.transferTo(target);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store uploaded file", ex);
        }

        return "/uploads/" + subfolder + "/" + filename;
    }
}
