package com.cloud.module.agent.controller.admin;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.module.agent.entity.AiSkillResourceEntity;
import com.cloud.module.agent.entity.DTO.AiSkillResourceSaveRequest;
import com.cloud.module.agent.service.AiSkillResourceService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/agent/skillResource")
@RequiredArgsConstructor
public class AiSkillResourceController {

    private final AiSkillResourceService resourceService;

    @GetMapping("/skill/{skillId}")
    public CommonResult<List<AiSkillResourceEntity>> listBySkill(@PathVariable Long skillId) {
        return success(resourceService.listBySkill(skillId));
    }

    @GetMapping("/content/{id}")
    public CommonResult<String> getContent(@PathVariable Long id) {
        return success(resourceService.getContent(id));
    }

    @PostMapping("/create")
    public CommonResult<AiSkillResourceEntity> create(
            @RequestBody AiSkillResourceSaveRequest request
    ) {
        return success(resourceService.createResource(request));
    }

    @PostMapping("/update")
    public CommonResult<AiSkillResourceEntity> update(
            @RequestBody AiSkillResourceSaveRequest request
    ) {
        return success(resourceService.updateResource(request));
    }

    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return success(resourceService.deleteResource(id));
    }

    @DeleteMapping("/folder")
    public CommonResult<Integer> deleteFolder(
            @RequestParam Long skillId,
            @RequestParam String path
    ) {
        return success(resourceService.deleteFolder(skillId, path));
    }
}
