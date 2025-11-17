package com.lostfound.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class FilesController {

    private static final String UPLOAD_BASE_DIR = "C:/Users/SRI SAI JAYANTH/OneDrive/Desktop/Images";

    @GetMapping("/files")
    public ResponseEntity<?> getFile(@RequestParam("path") String filePath) {
        try {
            Path p = Paths.get(filePath);
            if (!p.isAbsolute()) {
                p = Paths.get(UPLOAD_BASE_DIR).resolve(filePath).normalize();
            }
            if (!Files.exists(p) || !Files.isRegularFile(p)) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = Files.readAllBytes(p);
            String probe = Files.probeContentType(p);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (probe != null) {
                try { mediaType = MediaType.parseMediaType(probe); } catch (Exception ignored) {}
            } else {
                // fallback for common image extensions
                String name = p.getFileName().toString().toLowerCase();
                if (name.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
                else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
                else if (name.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=60, public")
                    .contentType(mediaType)
                    .body(new ByteArrayResource(bytes));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Unable to read file");
        }
    }
}
