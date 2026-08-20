# 议题追踪器：本地 Markdown

本仓库的议题和规格以 Markdown 文件形式存放在 `.scratch/` 中。

## 约定

- 每个功能使用一个目录：`.scratch/<功能标识>/`
- 功能规格位于 `.scratch/<功能标识>/spec.md`
- 每个实现议题使用独立文件：`.scratch/<功能标识>/issues/<NN>-<议题标识>.md`
- 编号从 `01` 开始，不使用单一合并议题文件
- 在议题文件顶部附近使用 `Status:` 记录 triage 状态
- triage 状态值遵循 `triage-labels.md`
- 评论和讨论历史追加在文件末尾的 `## Comments` 标题下

## 当技能要求“发布到议题追踪器”时

在 `.scratch/<功能标识>/` 下创建文件；目录不存在时一并创建。

## 当技能要求“获取相关议题”时

读取用户引用的文件路径。用户通常会直接提供路径或议题编号。

## Wayfinder 操作约定

- 地图：`.scratch/<工作项>/map.md`
- 子议题：`.scratch/<工作项>/issues/NN-<议题标识>.md`
- 使用 `Type:` 记录类型：`research`、`prototype`、`grilling` 或 `task`
- 使用 `Status:` 记录执行状态：`claimed` 或 `resolved`
- 使用 `Blocked by: NN, NN` 记录依赖
- 所有依赖均为 `resolved` 时，议题解除阻塞
- 从编号最小的开放、未阻塞、未认领议题开始处理
- 开始工作前将状态改为 `claimed` 并保存
- 完成后在 `## Answer` 下追加答案，将状态改为 `resolved`，并把摘要及链接追加到 `map.md`
