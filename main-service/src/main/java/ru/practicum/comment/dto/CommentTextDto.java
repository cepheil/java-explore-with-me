package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentTextDto {

    @NotBlank(message = "text must not be empty")
    @Size(min = 3, max = 2000, message = "Comment text length must be between 3 and 2000 characters")
    private String text;

}

