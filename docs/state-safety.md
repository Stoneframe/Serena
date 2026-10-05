# Serena state safety

Required safeguards for changes involving state, persistence, time, or Android lifecycle behavior.
Read and apply only the sections relevant to the requested change, alongside the core invariants in
[AGENTS.md](../AGENTS.md). Check current implementation before relying on architectural assumptions.

## Editing and saving

- Trace affected screen, manager/editor, persistence, and notification paths as relevant to the change.
- Verify affected Save, Cancel, refresh, and alarm rescheduling paths. Manager mutations need not persist;
  `Revertible.save()` creates an in-memory checkpoint, while `Serena.save()` persists through storage.

## Schema and migrations

- Inspect `JsonConverter` and its ordered migration registration when changing the schema.
- Test representative older inputs through the affected migration chain, current-data round trips,
  and preservation of user data. Isolated migration tests do not verify the complete chain.

## Imports and replacement

- Test malformed and unsupported inputs. Failure must preserve the last usable saved data.
- Never use real user data as a destructive test fixture.

## Time-dependent behavior

- Use the existing `TimeService` seam for new time-dependent domain behavior and deterministic tests.
- Check relevant date boundaries, recurrence, time-zone/DST conversion, and clock changes.

## Lifecycle and notifications

- Consider backgrounding, recreation, process loss, cold receiver entry, permissions, and alarm recovery
  where relevant. `GlobalState` selections are process-local, not durable restoration state.
- Pair listener registration with callback/resource cleanup.
- JVM tests do not prove Android persistence, lifecycle, or alarm behavior: `MockStorage.save()` is a no-op.
  Perform relevant device/emulator checks or report them as unverified.

## Concurrency

- Before moving model or storage operations off the main thread, define state ownership and write ordering.
  Do not assume the shared graph is thread-safe.
