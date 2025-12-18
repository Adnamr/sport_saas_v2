package com.sportsaas.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 255, message = "Le nom doit faire au maximum 255 caracteres")
    private String name;

    @NotBlank(message = "Le SKU est obligatoire")
    @Size(max = 100, message = "Le SKU doit faire au maximum 100 caracteres")
    private String sku;

    @Size(max = 100, message = "Le slug doit faire au maximum 100 caracteres")
    private String slug;

    private String description;

    private String shortDescription;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit etre positif")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    private BigDecimal costPrice;

    private UUID categoryId;

    private String brand;

    private String barcode;

    private boolean taxable = true;

    private BigDecimal weight;

    private String weightUnit = "kg";
}
