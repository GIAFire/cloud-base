# Agent Runtime

The Agent Runtime context owns configurable conversational Agents and the durable history produced while users interact with them.

## Language

**Agent**:
A configured conversational identity selected before a conversation begins.
_Avoid_: Bot, assistant configuration

**Agent Category**:
A flat, tenant-owned, coarse-grained navigation classification used to organize Agents. An Agent can belong to zero or more Agent Categories, and classification never grants or restricts access to an Agent.
_Avoid_: Hierarchical category, personal category

**Agent Tag**:
A tenant-owned, fine-grained descriptive label used to characterize and filter Agents. An Agent can have zero or more Agent Tags, and tagging never grants or restricts access to an Agent.
_Avoid_: Personal tag

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

