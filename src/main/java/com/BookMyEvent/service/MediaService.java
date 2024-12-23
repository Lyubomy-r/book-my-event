package com.BookMyEvent.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaService {
    List<byte[]> getImageBytes(MultipartFile[] multipartFiles) throws IOException;
    String getImageBytesAsBase64(MultipartFile file)throws IOException;
    byte[] getSingleImageBytes(MultipartFile file) throws IOException;
}