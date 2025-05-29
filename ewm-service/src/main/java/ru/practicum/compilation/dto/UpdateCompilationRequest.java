package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public class UpdateCompilationRequest {
    private List<Long> events;
    private Boolean pinned;
    @Size(min = 1, max = 50, message = "Title must be between 6 and 254 characters")
    private String title;
}