package com.example.GymManagement.access.service;

import com.example.GymManagement.config.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L; // 5 MB
    private static final Path STORAGE_DIR = Paths.get("uploads", "clearances");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    public String storeClearanceFile(MultipartFile file) {
        // 1. Validate that the uploaded file exists and is not empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Medical clearance file must not be empty.");
        }
        // 2. Enforce the 5 MB maximum file size limit
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Medical clearance file size must not exceed 5 MB.");
        }
        // 3. Verify the content type and extension are PDF, JPG, or PNG
        String contentType = file.getContentType();
        String extension = resolveAllowedExtension(file.getOriginalFilename(), contentType);
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT)) || extension == null) {
            throw new IllegalArgumentException("Only PDF, JPG, and PNG files are allowed.");
        }
        // 4. Generate a random file name so the user's original filename is never used on disk
        String storedFileName = UUID.randomUUID() + extension;
        try {
            // 5. Ensure the uploads/clearances/ directory exists and save the file bytes
            Files.createDirectories(STORAGE_DIR);
            Path targetPath = STORAGE_DIR.resolve(storedFileName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded medical clearance document.", ex);
        }
    }

    public byte[] loadDocumentBytes(String storedFileName) {
        // 1. Resolve the path inside uploads/clearances/ and prevent directory traversal
        Path filePath = STORAGE_DIR.resolve(storedFileName).normalize();
        if (!filePath.startsWith(STORAGE_DIR) || !Files.exists(filePath)) {
            throw new ResourceNotFoundException("Medical clearance document file not found.");
        }
        // 2. Read and return the raw file bytes
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read medical clearance document.", ex);
        }
    }

    public MediaType resolveMediaType(String storedFileName) {
        // 1. Determine the HTTP content type from the stored file extension
        String lower = storedFileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        }
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        return MediaType.IMAGE_JPEG;
    }

    private String resolveAllowedExtension(String originalName, String contentType) {
        // 1. Check original filename extension if available
        if (originalName != null) {
            String lower = originalName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".pdf")) {
                return ".pdf";
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return ".jpg";
            }
            if (lower.endsWith(".png")) {
                return ".png";
            }
        }
        // 2. Fall back to MIME type mapping
        if (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)) {
            return ".pdf";
        }
        if (MediaType.IMAGE_JPEG_VALUE.equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return null;
    }
}