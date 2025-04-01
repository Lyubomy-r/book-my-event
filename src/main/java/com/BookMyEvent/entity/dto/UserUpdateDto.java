package com.BookMyEvent.entity.dto;

import com.BookMyEvent.annotations.ValidDateFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

  @Pattern(regexp = "^[\\p{L} ']{2,15}", message = "Write a correct First Name. Use only chars. Min 2 not more than 15.")
  private String name;
  @Pattern(regexp = "^[\\p{L} ']{0,15}", message = "Write a correct sur name. Use only chars. Min 0 not more than 15.")
  private String surname;
  @Email(message = "Incorrect email format")
  private String email;
  @ValidDateFormat
  private String birthdayDate;
  private String location;
  @Pattern(regexp = "^\\+?[0-9]{0,12}$", message = "Write a correct phone. Use only number and +. Min 10 not more than 13.")
  private String phoneNumber;
  @Size(min = 8, message = "Пароль повинен містити щонайменше 8 символів")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&.]).+$",
      message = "Пароль повинен містити велику літеру, цифру і спеціальний символ")
  private String password;
}
