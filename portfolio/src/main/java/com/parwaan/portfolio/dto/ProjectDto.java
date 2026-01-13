package com.parwaan.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    @NotBlank(message = "Title is required")
    private String title;
    @Size(max = 4000, message = "Description cannot exceed 4000 characters")
    private String description;
    private String repoUrl;
    private String liveUrl;
    private boolean featured;
}