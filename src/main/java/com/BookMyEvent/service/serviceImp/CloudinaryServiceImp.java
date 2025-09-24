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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

  private static final Map<Character, String> CYRILLIC_TO_LATIN =
      Map.ofEntries(
          Map.entry('А', "A"),
          Map.entry('а', "a"),
          Map.entry('Б', "B"),
          Map.entry('б', "b"),
          Map.entry('В', "V"),
          Map.entry('в', "v"),
          Map.entry('Г', "H"),
          Map.entry('г', "h"),
          Map.entry('Ґ', "G"),
          Map.entry('ґ', "g"),
          Map.entry('Д', "D"),
          Map.entry('д', "d"),
          Map.entry('Е', "E"),
          Map.entry('е', "e"),
          Map.entry('Є', "Ye"),
          Map.entry('є', "ie"),
          Map.entry('Ж', "Zh"),
          Map.entry('ж', "zh"),
          Map.entry('З', "Z"),
          Map.entry('з', "z"),
          Map.entry('И', "Y"),
          Map.entry('и', "y"),
          Map.entry('І', "I"),
          Map.entry('і', "i"),
          Map.entry('Ї', "II"),
          Map.entry('ї', "ii"),
          Map.entry('Й', "Y"),
          Map.entry('й', "i"),
          Map.entry('К', "K"),
          Map.entry('к', "k"),
          Map.entry('Л', "L"),
          Map.entry('л', "l"),
          Map.entry('М', "M"),
          Map.entry('м', "m"),
          Map.entry('Н', "N"),
          Map.entry('н', "n"),
          Map.entry('О', "O"),
          Map.entry('о', "o"),
          Map.entry('П', "P"),
          Map.entry('п', "p"),
          Map.entry('Р', "R"),
          Map.entry('р', "r"),
          Map.entry('С', "S"),
          Map.entry('с', "s"),
          Map.entry('Т', "T"),
          Map.entry('т', "t"),
          Map.entry('У', "U"),
          Map.entry('у', "u"),
          Map.entry('Ф', "F"),
          Map.entry('ф', "f"),
          Map.entry('Х', "Kh"),
          Map.entry('х', "kh"),
          Map.entry('Ц', "Ts"),
          Map.entry('ц', "ts"),
          Map.entry('Ч', "Ch"),
          Map.entry('ч', "ch"),
          Map.entry('Ш', "Sh"),
          Map.entry('ш', "sh"),
          Map.entry('Щ', "Shch"),
          Map.entry('щ', "shch"),
          Map.entry('Ь', ""),
          Map.entry('ь', ""),
          Map.entry('Ю', "Yu"),
          Map.entry('ю', "iu"),
          Map.entry('Я', "Ya"),
          Map.entry('я', "ia"),
          Map.entry(' ', "-"));

  private final Cloudinary cloudinary;
  static final int MAX_SIZE_MB = 10;
  static final long MAX_FILE_SIZE_BYTES = (MAX_SIZE_MB + 1) * 1024 * 1024;

  @Override
  public List<Image> savedEventImages(List<MultipartFile> images, String title) {
    List<Image> existingImages = new ArrayList<>();
    try {
      title = getString(title);
      boolean main = true;
      int counter = 1;
      for (MultipartFile imageFile : images) {
        String nameImg = title + "-" + counter;
        log.info("savedEventImages title: " + title);
        String translate = transliterateToLatin(nameImg);
        log.info("savedEventImages Translate: " + translate);
        while (imageRepository.existsByName(translate)) {
          translate = UUID.randomUUID() + "-" + translate;
        }
        log.info("savedEventImages  imageFile.getContentType() {} ", imageFile.getContentType());
        String stringUrl = uploadFile(imageFile, translate, Event_FOLDER_NAME);
        Image newImage =
            Image.builder()
                .url(stringUrl)
                .creationDate(LocalDateTime.now())
                .isMain(main)
                .name(translate)
                .contentType(imageFile.getContentType())
                .build();
        Image saveImage = imageRepository.save(newImage);
        main = false;
        counter++;
        log.info(
            "{}::savedEventImg. Return all existing Event Images. bytes - {} ",
            clasName,
            saveImage.getId());
        existingImages.add(saveImage);
      }
      return existingImages;
    } catch (Exception e) {
      throw new GeneralException(
          "Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public Image saveEventImage(MultipartFile image, String title) {
    try {
      title = getString(title);
      String nameImg = title + "/" + image.getOriginalFilename();
      while (imageRepository.existsByName(nameImg)) {
        nameImg = UUID.randomUUID() + "-" + nameImg;
      }
      String stringUrl = uploadFile(image, nameImg, Event_FOLDER_NAME);
      Image newImage =
          Image.builder()
              .url(stringUrl)
              .creationDate(LocalDateTime.now())
              .isMain(false)
              .name(nameImg)
              .build();
      Image saveImage = imageRepository.save(newImage);
      log.info(
          "{}::savedEventImg. Return all existing Event Images. Id - {} ",
          clasName,
          saveImage.getId());

      return saveImage;
    } catch (Exception e) {
      throw new GeneralException(
          "Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  private static String getString(String title) {
    if (title.length() > 20) {
      String[] titleSplit = title.split(" ");
      if (titleSplit.length >= 2) {
        title = titleSplit[0] + " " + titleSplit[1];
        return title;
      } else {
        title = title.substring(0, 20);
        return title;
      }
    }
    return title;
  }

  @Override
  public Image savedUserImage(MultipartFile image, String userId) {
    try {
      String nameImg = userId + "/" + image.getOriginalFilename();
      while (imageRepository.existsByName(nameImg)) {
        nameImg = UUID.randomUUID() + "-" + nameImg;
      }
      String stringUrl = uploadFile(image, nameImg, USERS_FOLDER_NAME);
      Image newImage =
          Image.builder()
              .url(stringUrl)
              .creationDate(LocalDateTime.now())
              .isMain(true)
              .name(nameImg)
              .build();
      Image saveImage = imageRepository.save(newImage);
      log.info(
          "{}::savedEventImg. Return all existing Event Images. Id - {} ",
          clasName,
          saveImage.getId());

      return saveImage;
    } catch (Exception e) {
      throw new GeneralException(
          "Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public String updateImageById(MultipartFile image, String imageId, String folderName) {
    var imageOptional =
        imageRepository
            .findById(new ObjectId(imageId))
            .orElseThrow(
                () ->
                    new GeneralException(
                        "Image not found with ID: " + imageId, HttpStatus.NOT_FOUND));
    try {
      String nameImg = image.getName();
      while (imageRepository.existsByName(nameImg)) {
        nameImg = UUID.randomUUID() + "-" + nameImg;
      }
      String stringUrl = uploadFile(image, nameImg, Event_FOLDER_NAME);

      imageOptional.setUrl(stringUrl);
      imageRepository.save(imageOptional);
    } catch (Exception e) {
      throw new GeneralException(
          "Failed to process image file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return "Image updated successfully";
  }

  @Override
  public void deleteAllEventImg(List<Image> images) {
    if (!images.isEmpty()) {
      for (Image imageFile : images) {
        String imgName = Event_FOLDER_NAME + "/" + imageFile.getName();
        boolean deletionResult = deleteFile(imgName);
        log.info(
            "{}::deleteAll. cloudinary deletion imageFile.getName() {} ",
            clasName,
            imageFile.getName());
        log.info("{}::deleteAll. cloudinary deletion result {} ", clasName, deletionResult);
      }
      imageRepository.deleteAll(images);
      log.info("{}::deleteAll. delete all existing Event Images by id. ", clasName);
    }
  }

  @Override
  public void deleteUserImg(Image image) {
    if (image != null) {
      String imgName = USERS_FOLDER_NAME + "/" + image.getName();
      boolean deletionResult = deleteFile(imgName);
      log.info(
          "{}::deleteUserImg. cloudinary deletion imageFile.getName() {} ",
          clasName,
          image.getName());
      log.info("{}::deleteUserImg. cloudinary deletion result {} ", clasName, deletionResult);

      imageRepository.delete(image);
      log.info("{}::deleteUserImg. delete  existing User Images  {}. ", clasName, image.getName());
    }
  }

  public String uploadFile(MultipartFile file, String fileName, String folderName) {
    if (!checkFileType(file)) {
      throw new GeneralException(
          "File type unsupported " + file.getContentType(), HttpStatus.BAD_REQUEST);
    }
    try {
      byte[] fileData =
          (file.getSize() > MAX_FILE_SIZE_BYTES) ? optimizeImage(file) : file.getBytes();
      String stringUrl =
          cloudinary
              .uploader()
              .upload(
                  fileData,
                  ObjectUtils.asMap("public_id", fileName, "overwrite", true, "folder", folderName))
              .get("url")
              .toString();
      log.info("CloudinaryUtils::uploadFile. Upload File, new image and get image url.");
      return stringUrl;

    } catch (IOException e) {
      log.error("Failed to upload file: {}", file.getName(), e);
      throw new GeneralException(
          "Failed to upload file - " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public byte[] downloadFile(String fileName) {
    //    if (!checkFileType(file)) {
    //      throw new GeneralException(
    //              "File type unsupported " + file.getContentType(), HttpStatus.BAD_REQUEST);
    //    }
    try {

      String imageUrl =
          cloudinary
              .url()
              .secure(true)
              //              .version(1758480771)
              .generate("bookMyEventApp/" + fileName);

      //
      log.info("downloadFile file: {} , {}", fileName, imageUrl);
      // Завантажуємо байти по URL
      try (InputStream in = new URL(imageUrl).openStream();
          ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) > 0) {
          baos.write(buffer, 0, n);
        }
        log.info("downloadFile return  byte[] file.");
        return baos.toByteArray();
      } catch (MalformedURLException ex) {
        throw new RuntimeException(ex);
      }
      //      }catch (IOException ex) {
      //      throw new RuntimeException(ex);
      //    }

    } catch (IOException e) {
      log.error("Failed to upload file: {}", fileName, e);
      throw new GeneralException(
          "Failed to upload file - " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  public boolean deleteFile(String fileName) {
    try {
      var result = cloudinary.uploader().destroy(fileName, ObjectUtils.emptyMap());
      log.info("{}::deleteFile. delete img in cloudinary result. {} ", clasName, result);
      if (result.containsKey("result") && result.get("result").equals("ok")) {
        log.info("{}::deleteFile. delete img in cloudinary id. {} ", clasName, fileName);
        return true;
      }
    } catch (IOException e) {
      log.error("Failed to delete a file: {}", fileName, e);
      throw new GeneralException(
          "Failed to delete file - " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return false;
  }

  private boolean checkFileType(MultipartFile file) {
    Map<String, List<Byte>> signatures =
        Map.of(
            "*.jpeg, *.jpg",
            List.of((byte) 0xFF, (byte) 0xD8),
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
            "*.webp",
            List.of((byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46),
            "*.svg+xml",
            List.of((byte) 0x3C, (byte) 0x73, (byte) 0x76, (byte) 0x67));
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
    log.info("{}::optimizeImage. optimize image file size get {}", clasName, file.getSize());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Thumbnails.of(file.getInputStream())
        .size(1024, 768)
        .outputQuality(0.7)
        .toOutputStream(outputStream);
    byte[] optimizedImage = outputStream.toByteArray();
    if (optimizedImage.length > MAX_FILE_SIZE_BYTES) {
      throw new GeneralException(
          "Optimized image still exceeds the "
              + MAX_SIZE_MB
              + " limit : "
              + file.getOriginalFilename(),
          HttpStatus.PAYLOAD_TOO_LARGE);
    }

    log.info(
        "{}::optimizeImage. optimize image file size return {}", clasName, outputStream.size());
    return optimizedImage;
  }

  public static String transliterateToLatin(String text) {
    StringBuilder sb = new StringBuilder();
    for (char c : text.toCharArray()) {
      sb.append(CYRILLIC_TO_LATIN.getOrDefault(c, String.valueOf(c)));
    }
    return sb.toString().toLowerCase();
  }
}
