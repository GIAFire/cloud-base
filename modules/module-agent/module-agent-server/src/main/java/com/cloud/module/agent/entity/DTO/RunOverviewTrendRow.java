package com.cloud.module.agent.entity.DTO;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RunOverviewTrendRow {

    private LocalDate runDate;
    private Long runCount;
    private Long successCount;
}
