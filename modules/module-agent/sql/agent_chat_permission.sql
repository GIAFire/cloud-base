-- Agent 聊天按钮权限。
-- 隐藏聊天页由前端静态路由承载，因此这里只新增权限点，不新增菜单路由。
-- 执行后请在管理后台把该权限分配给需要使用聊天功能的角色。

INSERT INTO `system_menu` (
    `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`,
    `component`, `component_name`, `status`, `visible`, `keep_alive`,
    `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`
)
SELECT
    'Agent 对话', 'agent:chat:use', 3, 90, parent_menu.parent_id, '', '',
    '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0'
FROM `system_menu` parent_menu
WHERE parent_menu.permission = 'agent:info:query'
  AND parent_menu.deleted = b'0'
  AND NOT EXISTS (
      SELECT 1
      FROM `system_menu` existing_menu
      WHERE existing_menu.permission = 'agent:chat:use'
        AND existing_menu.deleted = b'0'
  )
LIMIT 1;
