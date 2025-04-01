package com.BookMyEvent.service;

import com.BookMyEvent.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaService {

    List<Image> savedEventImg(List<MultipartFile> images, String title);
    Image savedImg(MultipartFile image);

    String updateImageById(MultipartFile image, String imageId);

    void deleteAll(List<Image> imagesId);
}