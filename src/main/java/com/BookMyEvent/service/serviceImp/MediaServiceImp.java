package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.service.MediaService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class MediaServiceImp implements MediaService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of
            ("image/jpeg", "image/webp","image/svg","image/png");
    @Override
    public List<byte[]> getImageBytes(MultipartFile[] multipartFiles) throws IOException{
        final long MAX_SIZE_MB = 1 * 1024 * 1024;
        List<byte[]> imgByBytes = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            if ("image/svg+xml".equals(file.getContentType())) {
                imgByBytes.add(file.getBytes());
                continue;
            }
            if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
                throw new IOException("Unsupported file type: " + file.getContentType());
            }
            if (file.getSize() > MAX_SIZE_MB) {
                throw new IOException("File size exceeds the 1MB limit: " + file.getOriginalFilename());
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(1024, 768)
                    .outputQuality(0.7)
                    .toOutputStream(outputStream);
            byte[] optimizedImage = outputStream.toByteArray();
            if (optimizedImage.length > MAX_SIZE_MB) {
                throw new IOException("Optimized image still exceeds the 1MB limit: " + file.getOriginalFilename());
            }
            imgByBytes.add(optimizedImage);
        }
        return imgByBytes;
    }
    @Override
    public String getImageBytesAsBase64(MultipartFile file) throws IOException {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IOException("Unsupported file type: " + file.getContentType());
        }

        return Base64.getEncoder().encodeToString(file.getBytes());
    }
    @Override
    public byte[] getSingleImageBytes(MultipartFile file) throws IOException {
        return getImageBytes(new MultipartFile[]{file}).get(0);
    }
}