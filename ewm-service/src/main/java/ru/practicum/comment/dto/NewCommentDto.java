package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewCommentDto {
    @NotBlank(message = "Text must not be blank")
    @Size(max = 512, message = "Text must not exceed 512 characters")
    private String text;
}