package com.marketx.marketx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@CrossOrigin
public class FileStorageController {

    private final String uploadDirectory = "uploads";

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestParam("image") MultipartFile image) {

        try {

            // =========================
            // CHECK EMPTY FILE
            // =========================

            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select an image.");
            }

            // =========================
            // CHECK FILE TYPE
            // =========================

            String contentType = image.getContentType();

            if (contentType == null ||
                    !ALLOWED_TYPES.contains(contentType.toLowerCase())) {

                return ResponseEntity.badRequest()
                        .body("Only JPG, PNG and WEBP images are allowed.");
            }

            // =========================
            // CHECK FILE SIZE
            // =========================

            long maxFileSize = 10L * 1024L * 1024L;

            if (image.getSize() > maxFileSize) {
                return ResponseEntity.badRequest()
                        .body("Image size must be less than 10 MB.");
            }

            // =========================
            // CREATE UPLOAD DIRECTORY
            // =========================

            Path uploadPath = Paths.get(uploadDirectory);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // =========================
            // GET FILE EXTENSION
            // =========================

            String originalFileName = image.getOriginalFilename();

            String extension = ".jpg";

            if (originalFileName != null) {

                String lowerName =
                        originalFileName.toLowerCase();

                if (lowerName.endsWith(".png")) {
                    extension = ".png";
                } else if (lowerName.endsWith(".webp")) {
                    extension = ".webp";
                } else if (lowerName.endsWith(".jpeg")) {
                    extension = ".jpeg";
                } else if (lowerName.endsWith(".jpg")) {
                    extension = ".jpg";
                }
            }

            // =========================
            // CREATE UNIQUE FILE NAME
            // =========================

            String fileName =
                    UUID.randomUUID().toString()
                    + extension;

            // =========================
            // CREATE FILE PATH
            // =========================

            Path filePath =
                    uploadPath
                            .resolve(fileName)
                            .normalize();

            // =========================
            // SAVE IMAGE
            // =========================

            Files.copy(
                    image.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // =========================
            // RETURN IMAGE URL
            // =========================

            String imageUrl =
                    "/api/images/" + fileName;

            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body("Image upload failed.");
        }
    }

    // ==================================================
    // GET IMAGE
    // ==================================================

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String fileName) {

        try {

            Path uploadPath =
                    Paths.get(uploadDirectory)
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    uploadPath
                            .resolve(fileName)
                            .normalize();

            // =========================
            // SECURITY CHECK
            // =========================

            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            // =========================
            // CHECK FILE EXISTS
            // =========================

            if (!Files.exists(filePath) ||
                    !Files.isRegularFile(filePath)) {

                return ResponseEntity.notFound().build();
            }

            // =========================
            // CREATE RESOURCE
            // =========================

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists() ||
                    !resource.isReadable()) {

                return ResponseEntity.notFound().build();
            }

            // =========================
            // DETECT CONTENT TYPE
            // =========================

            String contentType =
                    Files.probeContentType(filePath);

            if (contentType == null) {

                contentType =
                        MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // =========================
            // RETURN IMAGE
            // =========================

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .body(resource);

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}