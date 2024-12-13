package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.service.MediaService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class MediaServiceImp implements MediaService {
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of
            ("image/jpeg","image/ico","image/tiff", "image/webp",
                    "image/eps","image/svg","image/png","image/jfif","image/gif");
    @Override
    public List<byte[]> getImageBytes(MultipartFile[] multipartFiles) throws IOException {
        List<byte[]> imgByBytes = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
                throw new IOException("Unsupported file type: " + file.getContentType());
            }
            imgByBytes.add(file.getBytes());
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