package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewCompilationDto {
    private List<Long> events;
    private Boolean pinned;

    @NotBlank(message = "Field: title. Error: must not be blank. Value: null",
            groups = OnCreate.class)
    @Size(min = 1, max = 50, message = "Title must be between 3 and 120 characters",
            groups = {OnCreate.class, OnUpdate.class})
    private String title;

    public interface OnCreate {}

    public interface OnUpdate {}
}