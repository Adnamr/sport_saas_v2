package com.sportsaas.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom doit faire au maximum 255 caracteres")
    private String name;

    @Size(max = 100, message = "Le slug doit faire au maximum 100 caracteres")
    private String slug;

    private String description;

    private String imageUrl;

    private UUID parentId;

    private int sortOrder = 0;
}
