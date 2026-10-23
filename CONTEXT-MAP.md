# Context Map

## Contexts

- [Agent Runtime](./modules/module-agent/CONTEXT.md) — owns configured Agents, conversations, runs, and durable chat history
- [Member](./modules/module-member/CONTEXT.md) — owns member identities and authentication credentials
- [Admin UI](./ui/ui-admin/CONTEXT.md) — provides authenticated operators with management and Agent conversation workspaces
- [AI Friend UI](./ui/ui-AiFriend-uniapp/CONTEXT.md) — provides character discovery and AI companionship conversations for end users

## Relationships

- **Admin UI → Agent Runtime**: the Admin UI selects an Agent, reads its conversations and messages, and starts streaming runs.
- **AI Friend UI → Agent Runtime**: shares the Agent, Chat Session, and Message language; current frontend delivery uses simulated experiences, with backend integration planned separately.

