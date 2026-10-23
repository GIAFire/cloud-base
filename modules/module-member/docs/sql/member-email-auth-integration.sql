-- Upgrade for installations that already applied member-email-auth.sql (MySQL 8+).
-- Expand audit fields so valid email identifiers are not rejected or truncated.
ALTER TABLE `system_login_log`
  MODIFY COLUMN `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '用户账号';

ALTER TABLE `member_email_code`
  MODIFY COLUMN `creator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '创建者';
