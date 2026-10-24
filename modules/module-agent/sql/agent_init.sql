/*
 Navicat Premium Data Transfer

 Source Server         : WSL
 Source Server Type    : MySQL
 Source Server Version : 80410 (8.4.10)
 Source Host           : localhost:3306
 Source Schema         : cloud_base

 Target Server Type    : MySQL
 Target Server Version : 80410 (8.4.10)
 File Encoding         : 65001

 Date: 22/10/2026 16:52:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agent_category
-- ----------------------------
DROP TABLE IF EXISTS `agent_category`;
CREATE TABLE `agent_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0启用 1禁用',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `active_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci GENERATED ALWAYS AS (CASE WHEN (`deleted` = b'0') THEN `name` ELSE NULL END) STORED COMMENT '未删除分类的唯一名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_category_tenant_active_name`(`tenant_id` ASC, `active_name` ASC) USING BTREE,
  INDEX `idx_agent_category_tenant_list`(`tenant_id` ASC, `deleted` ASC, `status` ASC, `sort` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'agent分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_category_agent
-- ----------------------------
DROP TABLE IF EXISTS `agent_category_agent`;
CREATE TABLE `agent_category_agent`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` bigint NOT NULL COMMENT 'Agent ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_category_agent_binding`(`tenant_id` ASC, `agent_id` ASC, `category_id` ASC) USING BTREE,
  INDEX `idx_agent_category_agent_category`(`tenant_id` ASC, `category_id` ASC, `agent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent与分类关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_http_header
-- ----------------------------
DROP TABLE IF EXISTS `agent_http_header`;
CREATE TABLE `agent_http_header`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `source_id` bigint NOT NULL COMMENT '关联外部资源表id',
  `source` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '值来源：model、subAgent',
  `header_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求头名称，例如Authorization、X-API-Key',
  `header_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求头值；当前阶段明文存储，仅所属资源详情接口允许返回',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_http_header_source`(`source` ASC, `source_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'HTTP请求头配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_info
-- ----------------------------
DROP TABLE IF EXISTS `agent_info`;
CREATE TABLE `agent_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Agent 主键ID',
  `model_id` bigint NULL DEFAULT NULL COMMENT '关联 ai_agent_model.id',
  `sys_prompt_id` bigint NULL DEFAULT NULL COMMENT '关联 ai_agent_sys_prompt.id',
  `agent_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Agent唯一编码',
  `agent_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Agent 显示名称',
  `agent_kind` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CHAT' COMMENT 'Agent用途：CHAT用户聊天、CREATE_COSPLAYER客服',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Agent 描述',
  `max_iters` int NOT NULL DEFAULT 10 COMMENT '最大循环次数',
  `permission_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ASK' COMMENT '默认权限模式',
  `compaction_enabled` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用上下文压缩：1启用，0关闭',
  `trigger_messages` int NULL DEFAULT 30 COMMENT '触发压缩消息数',
  `keep_messages` int NULL DEFAULT 10 COMMENT '压缩后保留消息数',
  `trigger_tokens` int NULL DEFAULT 6000 COMMENT '触发压缩token数',
  `keep_tokens` int NULL DEFAULT 800 COMMENT '压缩后保留token数',
  `tool_result_eviction_enabled` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否启用大工具结果卸载：1启用，0关闭',
  `memory_enable` bit(1) NULL DEFAULT b'1' COMMENT '是否启用长期记忆:1启用,0禁用',
  `plan_mode_enabled` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否启用Plan Mode：1启用，0关闭',
  `allow_shell_in_plan_mode` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Plan阶段是否允许shell工具：1允许，0禁止；生产环境建议关闭',
  `personality` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '人格设定',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ai_agent_tenant_code`(`agent_code` ASC) USING BTREE,
  INDEX `idx_ai_agent_tenant_updated`(`deleted` ASC, `update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2093151477717348355 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent 定义表：保存一个可视化 Agent 的基础身份信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_message
-- ----------------------------
DROP TABLE IF EXISTS `agent_message`;
CREATE TABLE `agent_message`  (
  `id` bigint NOT NULL COMMENT '消息主键ID',
  `session_id` bigint NOT NULL COMMENT '会话ID，关联agent_session.id',
  `run_id` bigint NOT NULL COMMENT '产生该消息的运行ID',
  `seq` bigint NOT NULL COMMENT '会话内全局消息序号，用于历史恢复和稳定排序',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '协议角色：USER、ASSISTANT、SYSTEM、TOOL',
  `message_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型：USER_TEXT、ASSISTANT_THINKING、ASSISTANT_TEXT',
  `text_content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '可搜索和直接展示的文本内容',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_message_session_seq`(`session_id` ASC, `seq` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_message_run_seq`(`run_id` ASC, `seq` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent消息表：保存输入、思考、正文等消息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_model
-- ----------------------------
DROP TABLE IF EXISTS `agent_model`;
CREATE TABLE `agent_model`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模型配置主键ID',
  `protocol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型接口协议：openaiCompatible、dashscope、anthropic、ollama',
  `base_URL` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '兼容 OpenAI 协议时的自定义 baseUrl',
  `api_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'API Key；当前阶段明文存储，仅模型详情接口允许返回',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型说明',
  `model_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实模型名称，例如 qwen-plus、gpt-4.1、claude-sonnet-4-5',
  `streaming` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否支持流式',
  `thinking` bit(1) NULL DEFAULT NULL COMMENT '是否支持思考',
  `temperature` decimal(4, 3) NULL DEFAULT NULL COMMENT '采样温度，控制随机性',
  `top_p` decimal(4, 3) NULL DEFAULT NULL COMMENT '核采样参数',
  `max_tokens` int NULL DEFAULT NULL COMMENT '最大输出token',
  `thinking_budget` int NULL DEFAULT NULL COMMENT '最大思考token',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_agent_model_tenant_status`(`update_time` ASC) USING BTREE,
  INDEX `idx_ai_agent_model_tenant_protocol`(`protocol` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2093246396150616067 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '模型配置表：把凭证、模型名、生成参数组合成可被 Agent 选择的模型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_run
-- ----------------------------
DROP TABLE IF EXISTS `agent_run`;
CREATE TABLE `agent_run`  (
  `id` bigint NOT NULL COMMENT '运行主键ID',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求id',
  `session_id` bigint NOT NULL COMMENT '会话ID，关联agent_session.id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '运行状态：RUNNING运行中，SUCCEEDED成功，FAILED失败，CANCELLED取消',
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误码，便于后端分类处理',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息，保存内部错误，不建议直接返回给前端',
  `started_at` datetime NOT NULL COMMENT '运行开始时间',
  `ended_at` datetime NULL DEFAULT NULL COMMENT '运行结束时间',
  `duration_ms` bigint NULL DEFAULT NULL COMMENT '运行耗时，单位毫秒',
  `input_tokens` bigint NULL DEFAULT NULL COMMENT '输入Token数',
  `output_tokens` bigint NULL DEFAULT NULL COMMENT '输出Token数',
  `reasoning_tokens` bigint NULL DEFAULT NULL COMMENT '思考Token数',
  `total_tokens` bigint NULL DEFAULT NULL COMMENT '总Token数',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_run_request`(`request_id` ASC, `user_id` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_run_session_created`(`id` ASC, `session_id` ASC, `create_time` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_run_session_status`(`tenant_id` ASC, `session_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent运行表：一次用户请求对应一次AgentScope streamEvents执行' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_session
-- ----------------------------
DROP TABLE IF EXISTS `agent_session`;
CREATE TABLE `agent_session`  (
  `id` bigint NOT NULL COMMENT '会话主键ID',
  `agent_id` bigint NOT NULL COMMENT 'Agent ID，关联智能体定义表',
  `user_id` bigint NOT NULL COMMENT '用户ID，表示该会话归属哪个登录用户',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话标题，可由第一条用户消息或大模型生成',
  `last_message_seq` bigint NOT NULL DEFAULT 0 COMMENT '最后分配的消息序号',
  `last_message_at` datetime NULL DEFAULT NULL COMMENT '最后分配的消息时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_owner_updated`(`id` ASC, `user_id` ASC, `update_time` ASC, `deleted` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_session_owner_agent_recent`(`tenant_id` ASC, `user_id` ASC, `agent_id` ASC, `deleted` ASC, `last_message_at` DESC, `id` DESC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent会话表：保存用户和Agent之间的一段连续对话' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_sys_prompt
-- ----------------------------
DROP TABLE IF EXISTS `agent_sys_prompt`;
CREATE TABLE `agent_sys_prompt`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提示词模板主键ID',
  `prompt_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提示词模板名称',
  `description` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '描述',
  `sys_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '提示词内容',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2113186261867335682 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'Agent 定义表：保存一个可视化 Agent 的基础身份信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for agent_tag
-- ----------------------------
DROP TABLE IF EXISTS `agent_tag`;
CREATE TABLE `agent_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0启用 1禁用',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `active_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci GENERATED ALWAYS AS (CASE WHEN (`deleted` = b'0') THEN `name` ELSE NULL END) STORED COMMENT '未删除标签的唯一名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_tag_tenant_active_name`(`tenant_id` ASC, `active_name` ASC) USING BTREE,
  INDEX `idx_agent_tag_tenant_list`(`tenant_id` ASC, `deleted` ASC, `status` ASC, `sort` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'agent标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for agent_tag_agent
-- ----------------------------
DROP TABLE IF EXISTS `agent_tag_agent`;
CREATE TABLE `agent_tag_agent`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` bigint NOT NULL COMMENT 'Agent ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_tag_agent_binding`(`tenant_id` ASC, `agent_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_agent_tag_agent_tag`(`tenant_id` ASC, `tag_id` ASC, `agent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'agent标签关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
