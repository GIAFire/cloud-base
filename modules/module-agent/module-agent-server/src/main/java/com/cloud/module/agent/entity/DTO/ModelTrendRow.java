package com.cloud.module.agent.entity.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ModelTrendRow {
    private LocalDate callDate;
    private Long callCount;
}
