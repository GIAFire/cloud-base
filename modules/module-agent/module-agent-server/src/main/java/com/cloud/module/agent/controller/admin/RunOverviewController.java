package com.cloud.module.agent.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.DTO.RunOverviewResponse;
import com.cloud.module.agent.service.RunOverviewService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.function.Supplier;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/overview")
@RequiredArgsConstructor
public class RunOverviewController {

    private final RunOverviewService overviewService;

    @GetMapping
    public CommonResult<RunOverviewResponse> overview(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return handle(() -> overviewService.overview(startDate, endDate));
    }

    @GetMapping("/interactions/page")
    public CommonResult<IPage<RunOverviewResponse.RecentInteraction>> interactionPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword
    ) {
        return handle(() -> overviewService.pageRecentInteractions(current, size, keyword));
    }

    private <T> CommonResult<T> handle(Supplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return CommonResult.error(500,exception.getMessage());
        }
    }
}
