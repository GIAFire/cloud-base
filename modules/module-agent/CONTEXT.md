# Agent Runtime

The Agent Runtime context owns configurable conversational Agents and the durable history produced while users interact with them.

## Language

**Agent**:
A configured conversational identity selected before a conversation begins.
_Avoid_: Bot, assistant configuration

**Chat Session**:
A continuous conversation between one tenant user and one Agent. Its Agent cannot change after the session begins.
_Avoid_: Thread, room

**Run**:
One attempt to produce an Agent response for a user message within a Chat Session.
_Avoid_: Request, task

**Message**:
A complete durable user input or Agent response belonging to a Chat Session. Streaming deltas and Thinking are not Messages.
_Avoid_: Chunk, event

**Thinking**:
Ephemeral reasoning content emitted during a Run and excluded from durable chat history.
_Avoid_: Message, answer

