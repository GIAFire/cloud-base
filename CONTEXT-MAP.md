# Context Map

## Contexts

- [Agent Runtime](./modules/module-agent/CONTEXT.md) — owns configured Agents, conversations, runs, and durable chat history
- [Admin UI](./ui/ui-admin/CONTEXT.md) — provides authenticated operators with management and Agent conversation workspaces

## Relationships

- **Admin UI → Agent Runtime**: the Admin UI selects an Agent, reads its conversations and messages, and starts streaming runs.

