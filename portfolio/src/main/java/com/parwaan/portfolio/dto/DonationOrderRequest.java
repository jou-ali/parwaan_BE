package com.parwaan.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonationOrderRequest {
    @NotNull
    @Min(1)
    private Long amountRupees;

    @Email
    private String email;
}
