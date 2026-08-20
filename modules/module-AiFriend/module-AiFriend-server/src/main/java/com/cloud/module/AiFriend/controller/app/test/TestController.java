package com.cloud.module.AiFriend.controller.app.test;

import com.cloud.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.cloud.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI朋友测试")
@RestController
@RequestMapping("/AiFriend/test")
@Validated
public class TestController {

    @GetMapping("/get")
    public CommonResult<String> getConfig() {
        return success("接口调用成功");
    }

}
