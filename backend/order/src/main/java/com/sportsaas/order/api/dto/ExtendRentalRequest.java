package com.sportsaas.order.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtendRentalRequest {

    @NotNull(message = "La nouvelle date de fin est obligatoire")
    private LocalDateTime newEndDate;
}
