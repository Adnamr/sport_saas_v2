package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendReservationRequest {

    @NotNull(message = "La duree supplementaire est obligatoire")
    @Positive(message = "La duree doit etre positive")
    private Integer additionalMinutes;
}
