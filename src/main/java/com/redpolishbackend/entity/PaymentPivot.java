package com.redpolishbackend.entity;

import com.paypal.api.payments.Payment;
import com.redpolishbackend.dto.PayDto;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPivot
{
    private Payment payment;
    private Compra compra;
}
