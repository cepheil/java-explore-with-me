package ru.practicum.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "name must not be empty")
    @Size(min = 2, max = 250, message = "name length must be between 2 and 250 characters")
    private String name;

    @NotBlank(message = "email must not be empty")
    @Email(message = "email must be a valid email address")
    @Size(min = 6, max = 254, message = "email length must be between 6 and 254 characters")
    private String email;

}
