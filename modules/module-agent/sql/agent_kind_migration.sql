-- Agent 用途增量迁移（MySQL 8.0+，旧库仅执行一次；新库使用 agent_init.sql）。
-- 部署本次代码前执行；已有 Agent 保持原聊天构造行为。
ALTER TABLE `agent_info`
  ADD COLUMN `agent_kind` varchar(32) NOT NULL DEFAULT 'CHAT'
    COMMENT 'Agent用途：CHAT用户聊天、CREATE_COSPLAYER制卡' AFTER `agent_name`;

-- 迁移后可在管理后台将需要的 Agent 设置为 CREATE_COSPLAYER。
