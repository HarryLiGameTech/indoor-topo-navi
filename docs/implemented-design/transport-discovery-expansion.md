# Transport Discovery Expansion

The MVP transport-discovery endpoint and MCP tool query elevator declarations only. They intentionally do not infer transport availability from similarly named elevator-hall nodes.

In the near-ish future, the same feature should support escalators and stairs. The expanded API should retain the current simple and complete response modes, read only compiled transport declarations, and identify each transport's type explicitly so downstream clients can describe it accurately.

This work should land after escalator compilation is implemented and the shared response contract has been generalized beyond elevator-specific naming.
