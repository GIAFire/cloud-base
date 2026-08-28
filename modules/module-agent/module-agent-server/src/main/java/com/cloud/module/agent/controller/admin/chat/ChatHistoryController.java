package com.cloud.module.agent.controller.admin.chat;

import com.cloud.framework.common.pojo.CommonResult;
import com.cloud.framework.common.pojo.PageResult;
import com.cloud.framework.security.core.LoginUser;
import com.cloud.framework.security.core.util.SecurityFrameworkUtils;
import com.cloud.module.agent.chat.application.ChatHistoryApplication;
import com.cloud.module.agent.chat.application.model.ChatMessageSlice;
import com.cloud.module.agent.chat.application.model.ChatSessionSummary;
import com.cloud.module.agent.chat.application.model.StoredChatMessage;
import com.cloud.module.agent.chat.domain.ChatActor;
import com.cloud.module.agent.controller.admin.chat.vo.ChatMessagePageRequest;
import com.cloud.module.agent.controller.admin.chat.vo.ChatMessagePageResponse;
import com.cloud.module.agent.controller.admin.chat.vo.ChatMessageResponse;
import com.cloud.module.agent.controller.admin.chat.vo.ChatSessionPageRequest;
import com.cloud.module.agent.controller.admin.chat.vo.ChatSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.cloud.framework.common.pojo.CommonResult.success;

/** Agent 聊天历史的 HTTP 入站 Adapter。 */
@Tag(name = "智能体运行 - 聊天历史")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/agent/chat")
public class ChatHistoryController {

    private final ChatHistoryApplication chatHistoryApplication;

    @GetMapping("/session/page")
    @Operation(summary = "获得当前用户的 Agent 会话分页")
    @PreAuthorize("@ss.hasPermission('agent:chat:use')")
    public CommonResult<PageResult<ChatSessionResponse>> getSessionPage(
            @Valid ChatSessionPageRequest request
    ) {
        PageResult<ChatSessionSummary> page = chatHistoryApplication.findSessions(
                Long.valueOf(request.getAgentId()),
                request,
                currentActor()
        );
        List<ChatSessionResponse> list = page.getList().stream()
                .map(this::toSessionResponse)
                .toList();
        return success(new PageResult<>(list, page.getTotal()));
    }

    @GetMapping("/message/page")
    @Operation(summary = "获得会话消息游标页")
    @PreAuthorize("@ss.hasPermission('agent:chat:use')")
    public CommonResult<ChatMessagePageResponse> getMessagePage(
            @Valid ChatMessagePageRequest request
    ) {
        Long beforeSeq = request.getBeforeSeq() == null
                ? null
                : Long.valueOf(request.getBeforeSeq());
        ChatMessageSlice slice = chatHistoryApplication.findMessages(
                Long.valueOf(request.getSessionId()),
                beforeSeq,
                request.getPageSize(),
                currentActor()
        );
        List<ChatMessageResponse> list = slice.getList().stream()
                .map(this::toMessageResponse)
                .toList();
        return success(new ChatMessagePageResponse(
                list,
                toStringOrNull(slice.getNextBeforeSeq()),
                slice.isHasMore()
        ));
    }

    private ChatActor currentActor() {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            throw new IllegalStateException("用户未登录");
        }
        return new ChatActor(loginUser.getTenantId(), loginUser.getId());
    }

    private ChatSessionResponse toSessionResponse(ChatSessionSummary session) {
        return new ChatSessionResponse(
                String.valueOf(session.getId()),
                String.valueOf(session.getAgentId()),
                session.getTitle(),
                String.valueOf(session.getLastMessageSeq()),
                session.getLastMessageAt()
        );
    }

    private ChatMessageResponse toMessageResponse(StoredChatMessage message) {
        return new ChatMessageResponse(
                String.valueOf(message.getId()),
                String.valueOf(message.getSessionId()),
                String.valueOf(message.getRunId()),
                String.valueOf(message.getSeq()),
                message.getRole(),
                message.getMessageType(),
                message.getContent(),
                message.getCreateTime()
        );
    }

    private String toStringOrNull(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
