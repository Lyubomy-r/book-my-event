package com.BookMyEvent.service;

import com.BookMyEvent.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {

  List<Image> savedEventImages(List<MultipartFile> images, String title);
  Image saveEventImage(MultipartFile image, String title);
  Image savedUserImage(MultipartFile image, String userId);

  String updateImageById(MultipartFile image, String imageId, String folderName);

  void deleteAllEventImg(List<Image> imagesId);

  void deleteUserImg(Image image);

  byte[] downloadFile(String fileName);
}
