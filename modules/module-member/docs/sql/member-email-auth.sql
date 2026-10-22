-- Member email authentication migration (MySQL 8+)
ALTER TABLE `member_user`
  MODIFY COLUMN `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  DROP INDEX `idx_member_user_tenant_email`,
  ADD UNIQUE INDEX `uk_member_user_tenant_email_deleted` (`tenant_id`, `email`, `deleted`);

CREATE TABLE `member_email_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规范化邮箱',
  `code_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码摘要',
  `scene` tinyint NOT NULL COMMENT '验证场景',
  `create_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建 IP',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `attempt_count` tinyint NOT NULL DEFAULT 0 COMMENT '失败尝试次数',
  `used` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已使用',
  `used_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `used_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '使用 IP',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  INDEX `idx_member_email_code_lookup` (`tenant_id`, `email`, `scene`, `id`),
  INDEX `idx_member_email_code_hourly` (`tenant_id`, `email`, `create_time`)
) ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员邮箱验证码';
