-- Agent 分类/标签增量迁移（MySQL 8.0+，仅执行一次）
--
-- 适用前提：数据库中已经执行过旧版 agent_init.sql，存在以下旧表：
--   agent_category、agent_tag、agent_tag_agent
--
-- 重要：MySQL DDL 会隐式提交，请先备份这三张表。
-- 如果旧表尚未写入数据，@legacy_tenant_id 保持 0 即可；
-- 如果已有数据，请先将它改成这些历史分类/标签实际所属的租户 ID。

SET NAMES utf8mb4;
SET @legacy_tenant_id = 0;

-- 旧 SQL 使用 1=启用、0=禁用；项目 CommonStatusEnum 使用 0=启用、1=禁用。
-- 已经手工按 CommonStatusEnum 写入过数据时，请跳过下面两条 UPDATE。
UPDATE `agent_category`
SET `status` = CASE `status` WHEN 1 THEN 0 WHEN 0 THEN 1 ELSE `status` END;

UPDATE `agent_tag`
SET `status` = CASE `status` WHEN 1 THEN 0 WHEN 0 THEN 1 ELSE `status` END;

-- 1. 分类表：补齐租户、审计、逻辑删除和租户内有效名称唯一约束。
ALTER TABLE `agent_category`
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0启用 1禁用',
  MODIFY COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  ADD COLUMN `creator` varchar(64) NULL DEFAULT NULL COMMENT '创建者' AFTER `status`,
  ADD COLUMN `updater` varchar(64) NULL DEFAULT NULL COMMENT '更新者' AFTER `create_time`,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除' AFTER `update_time`,
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`;

UPDATE `agent_category`
SET `tenant_id` = @legacy_tenant_id
WHERE `tenant_id` = 0;

ALTER TABLE `agent_category`
  ADD COLUMN `active_name` varchar(100)
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    GENERATED ALWAYS AS (
      CASE WHEN (`deleted` = b'0') THEN `name` ELSE NULL END
    ) STORED COMMENT '未删除分类的唯一名称',
  ADD UNIQUE INDEX `uk_agent_category_tenant_active_name`
    (`tenant_id` ASC, `active_name` ASC) USING BTREE,
  ADD INDEX `idx_agent_category_tenant_list`
    (`tenant_id` ASC, `deleted` ASC, `status` ASC, `sort` ASC, `id` ASC) USING BTREE;

-- 2. 标签表：删除未使用的 code，补齐租户、审计、逻辑删除和唯一约束。
ALTER TABLE `agent_tag`
  DROP INDEX `uk_name`,
  DROP COLUMN `code`,
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0启用 1禁用',
  MODIFY COLUMN `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  ADD COLUMN `creator` varchar(64) NULL DEFAULT NULL COMMENT '创建者' AFTER `sort`,
  ADD COLUMN `updater` varchar(64) NULL DEFAULT NULL COMMENT '更新者' AFTER `create_time`,
  ADD COLUMN `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除' AFTER `update_time`,
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `deleted`;

UPDATE `agent_tag`
SET `tenant_id` = @legacy_tenant_id
WHERE `tenant_id` = 0;

ALTER TABLE `agent_tag`
  ADD COLUMN `active_name` varchar(100)
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    GENERATED ALWAYS AS (
      CASE WHEN (`deleted` = b'0') THEN `name` ELSE NULL END
    ) STORED COMMENT '未删除标签的唯一名称',
  ADD UNIQUE INDEX `uk_agent_tag_tenant_active_name`
    (`tenant_id` ASC, `active_name` ASC) USING BTREE,
  ADD INDEX `idx_agent_tag_tenant_list`
    (`tenant_id` ASC, `deleted` ASC, `status` ASC, `sort` ASC, `id` ASC) USING BTREE;

-- 3. 旧标签关联表：改为代理主键，补齐租户字段和双向查询索引。
ALTER TABLE `agent_tag_agent`
  DROP PRIMARY KEY,
  ADD COLUMN `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID' FIRST,
  ADD COLUMN `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号' AFTER `tag_id`,
  ADD PRIMARY KEY (`id`) USING BTREE;

UPDATE `agent_tag_agent` AS `r`
INNER JOIN `agent_info` AS `a` ON `a`.`id` = `r`.`agent_id`
SET `r`.`tenant_id` = `a`.`tenant_id`;

ALTER TABLE `agent_tag_agent`
  ADD UNIQUE INDEX `uk_agent_tag_agent_binding`
    (`tenant_id` ASC, `agent_id` ASC, `tag_id` ASC) USING BTREE,
  ADD INDEX `idx_agent_tag_agent_tag`
    (`tenant_id` ASC, `tag_id` ASC, `agent_id` ASC) USING BTREE;

-- 4. 新增 Agent 与分类多对多关联表。
CREATE TABLE IF NOT EXISTS `agent_category_agent` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `agent_id` bigint NOT NULL COMMENT 'Agent ID，关联 agent_info.id',
  `category_id` bigint NOT NULL COMMENT '分类ID，关联 agent_category.id',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_agent_category_agent_binding`
    (`tenant_id` ASC, `agent_id` ASC, `category_id` ASC) USING BTREE,
  INDEX `idx_agent_category_agent_category`
    (`tenant_id` ASC, `category_id` ASC, `agent_id` ASC) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = 'Agent与分类关联表'
  ROW_FORMAT = Dynamic;

-- 5. 执行后检查：结果应为空；否则存在跨租户关联，需要先修复再启用功能。
SELECT `r`.`id`, `r`.`agent_id`, `r`.`tag_id`,
       `r`.`tenant_id` AS `relation_tenant_id`,
       `a`.`tenant_id` AS `agent_tenant_id`,
       `t`.`tenant_id` AS `tag_tenant_id`
FROM `agent_tag_agent` AS `r`
INNER JOIN `agent_info` AS `a` ON `a`.`id` = `r`.`agent_id`
INNER JOIN `agent_tag` AS `t` ON `t`.`id` = `r`.`tag_id`
WHERE `r`.`tenant_id` <> `a`.`tenant_id`
   OR `r`.`tenant_id` <> `t`.`tenant_id`;

