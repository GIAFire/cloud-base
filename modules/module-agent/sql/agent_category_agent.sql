-- Agent 与分类多对多关联表
-- 约定：关联记录采用硬删除；tenant_id 由租户拦截器写入。

SET NAMES utf8mb4;

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

