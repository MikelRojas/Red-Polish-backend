package com.redpolishbackend.dto;

import com.redpolishbackend.entity.Services;
import com.redpolishbackend.entity.User;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CitaDto {
    private Long id;
    private LocalDate date;
    private LocalTime hour;
    private String state;
    private Long userId;
    private Long serviceId;
}
