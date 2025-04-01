package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.ImageRepository;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service("cloudinaryServiceImp")
@RequiredArgsConstructor
@Slf4j
@Primary
public class CloudinaryServiceImp implements CloudinaryService {
  private final ImageRepository imageRepository;
  private final String clasName = this.getClass().getSimpleName();
  public static final String Event_FOLDER_NAME = "bookMyEventApp";
  public static final String USERS_FOLDER_NAME = "users";

  private final Cloudinary cloudinary;

  final static long MAX_SIZE_MB =1024 * 1024;

  @Override
  public List<Image> savedEventImages(List<MultipartFile> images, String title) {
    List<Image> existingImages = new ArrayList<>();
    try {
      title = getString(title);
      boolean main=true;
      int counter=1;
      for (MultipartFile imageFile : images) {
        String nameImg= title+"-"+counter;
        while (imageRepository.existsByName(nameImg)){
          nameImg= UUID.randomUUID()+"-"+nameImg;
        }
        String stringUrl = uploadFile(imageFile,nameImg,Event_FOLDER_NAME);
        Image newImage = Image.builder()
            .url(stringUrl)
            .creationDate(LocalDateTime.now())
            .isMain(main)
            .name(nameImg)
            .build();
        Image saveImage = imageRepository.save(newImage);
        main=false;
        counter++;
        log.info("{}::savedEventImg. Return all existing Event Images. bytes - {} ", clasName, saveImage.getId());
        existingImages.add(saveImage);
      }
      return existingImages;
    } catch (Exception e) {
      throw new GeneralException("Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Image saveEventImage(MultipartFile image, String title) {
    try {
      title = getString(title);
      String nameImg = title+"/"+image.getOriginalFilename();
      while (imageRepository.existsByName(nameImg)){
        nameImg= UUID.randomUUID()+"-"+nameImg;
      }
      String stringUrl = uploadFile(image, nameImg, Event_FOLDER_NAME);
      Image newImage = Image.builder()
          .url(stringUrl)
          .creationDate(LocalDateTime.now())
          .isMain(false)
          .name(nameImg)
          .build();
      Image saveImage = imageRepository.save(newImage);
      log.info("{}::savedEventImg. Return all existing Event Images. Id - {} ", clasName, saveImage.getId());

      return saveImage;
    } catch (Exception e) {
      throw new GeneralException("Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  private static String getString(String title) {
    if(title.length()>20){
      String[] titleSplit = title.split(" ");
      if(titleSplit.length>=2){
      title = titleSplit[0]+" "+titleSplit[1];
      return title;
      }else {
        title =  title.substring(0, 20);
        return title;
      }
    }
    return title;
  }

  @Override
  public Image savedUserImage(MultipartFile image, String userId) {
    try {
      String nameImg = userId+"/"+image.getOriginalFilename();
      while (imageRepository.existsByName(nameImg)){
        nameImg= UUID.randomUUID()+"-"+nameImg;
      }
      String stringUrl = uploadFile(image, nameImg, USERS_FOLDER_NAME);
      Image newImage = Image.builder()
          .url(stringUrl)
          .creationDate(LocalDateTime.now())
          .isMain(true)
          .name(nameImg)
          .build();
      Image saveImage = imageRepository.save(newImage);
      log.info("{}::savedEventImg. Return all existing Event Images. Id - {} ", clasName, saveImage.getId());

      return saveImage;
    } catch (Exception e) {
      throw new GeneralException("Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public String updateImageById(MultipartFile image, String imageId, String folderName) {
    var imageOptional = imageRepository.findById(new ObjectId(imageId)).orElseThrow(
        () -> new GeneralException("Image not found with ID: " + imageId, HttpStatus.NOT_FOUND));
    try {
        String nameImg= image.getName();
        while (imageRepository.existsByName(nameImg)){
          nameImg= UUID.randomUUID()+"-"+nameImg;
        }
        String stringUrl = uploadFile(image,nameImg,Event_FOLDER_NAME);

      imageOptional.setUrl(stringUrl);
      imageRepository.save(imageOptional);
    } catch (Exception e) {
      throw new GeneralException("Failed to process image file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return "Image updated successfully";
  }

  @Override
  public void deleteAllEventImg(List<Image> images) {
    if(!images.isEmpty()){
      for (Image imageFile : images) {
        String imgName = Event_FOLDER_NAME+"/"+imageFile.getName();
       boolean deletionResult = deleteFile(imgName);
        log.info("{}::deleteAll. cloudinary deletion imageFile.getName() {} ", clasName, imageFile.getName());
        log.info("{}::deleteAll. cloudinary deletion result {} ", clasName, deletionResult);
        }
      imageRepository.deleteAll(images);
      log.info("{}::deleteAll. delete all existing Event Images by id. ", clasName);
    }
  }

  @Override
  public void deleteUserImg(Image image) {
    if(image!=null){
        String imgName = USERS_FOLDER_NAME+"/"+image.getName();
        boolean deletionResult = deleteFile(imgName);
        log.info("{}::deleteUserImg. cloudinary deletion imageFile.getName() {} ", clasName, image.getName());
        log.info("{}::deleteUserImg. cloudinary deletion result {} ", clasName, deletionResult);

      imageRepository.delete(image);
      log.info("{}::deleteUserImg. delete  existing User Images  {}. ", clasName, image.getName());
    }
  }

  public String uploadFile(MultipartFile file, String fileName, String folderName) {
    if (!checkFileType(file)) {
      throw new GeneralException("File type unsupported "+file.getContentType(), HttpStatus.BAD_REQUEST);
    }
    try {
        byte[] fileData = (file.getSize() > MAX_SIZE_MB) ? optimizeImage(file) : file.getBytes();
        String stringUrl = cloudinary.uploader()
            .upload(
                fileData,
                ObjectUtils.asMap("public_id", fileName, "overwrite", true, "folder", folderName))
            .get("url")
            .toString();
        log.info("CloudinaryUtils::uploadFile. Upload File, new image and get image url.");
        return stringUrl;

    } catch (IOException e) {
      log.error("Failed to upload file: {}", file.getName(), e);
      throw new GeneralException("Failed to upload file - "+e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public boolean deleteFile(String fileName) {
    try {
      var result = cloudinary.uploader().destroy(fileName, ObjectUtils.emptyMap());
      log.info("{}::deleteFile. delete img in cloudinary result. {} ", clasName, result) ;
      if (result.containsKey("result") && result.get("result").equals("ok")) {
        log.info("{}::deleteFile. delete img in cloudinary id. {} ", clasName, fileName) ;
        return true;
      }
    } catch (IOException e) {
      log.error("Failed to delete a file: {}", fileName, e);
      throw new GeneralException("Failed to delete file - "+e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return false;
  }

  private boolean checkFileType(MultipartFile file) {
    Map<String, List<Byte>> signatures =
        Map.of(
            "*.jpeg, *.jpg", List.of((byte) 0xFF, (byte) 0xD8),
            "*.png",
            List.of(
                (byte) 0x89,
                (byte) 0x50,
                (byte) 0x4E,
                (byte) 0x47,
                (byte) 0x0D,
                (byte) 0x0A,
                (byte) 0x1A,
                (byte) 0x0A),
            "*.webp", List.of((byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46),
            "*.svg+xml", List.of((byte) 0x3C, (byte) 0x73, (byte) 0x76, (byte) 0x67) );
    try {
      byte[] bytes = Arrays.copyOfRange(file.getBytes(), 0, 8);
      if (signatures.values().stream()
          .anyMatch(
              signature ->
                  IntStream.range(0, signature.size())
                      .allMatch(i -> signature.get(i).equals(bytes[i])))) {
        return true;
      }
    } catch (IOException e) {
      log.error("Failed to read a file {}", file.getName(), e);
    }
    return false;
  }

  private byte[] optimizeImage(MultipartFile file) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Thumbnails.of(file.getInputStream())
        .size(1024, 768)
        .outputQuality(0.7)
        .toOutputStream(outputStream);
    byte[] optimizedImage = outputStream.toByteArray();
    if (optimizedImage.length > MAX_SIZE_MB) {
      throw new GeneralException("Optimized image still exceeds the 1MB limit: " + file.getOriginalFilename(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    return optimizedImage;
  }
}
