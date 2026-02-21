package com.redpolishbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {
    private Long id;
    private String title;
    private String description;
    private Double porcentage;
    private Date     start_date;
    private Date end_date;
}
