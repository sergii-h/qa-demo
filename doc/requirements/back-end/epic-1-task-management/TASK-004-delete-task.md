# TASK-004: Delete Task

**Points:** 3 · **Epic:** Task Management

As a user, I want to delete a task so that I can remove work items I no longer need.

## Acceptance Criteria

1. Should return HTTP 204 when `DELETE /v1/tasks/{id}` and task exists (hard delete from DB)
2. Should return HTTP 404 with `"Task not found with id: {taskId}"` when ID does not exist
3. Should publish task-deleted event to Kafka on successful delete (payload from pre-delete state)

## Test Plan

1. **UT**
   - Should publish nothing when deleting the task fails
2. **IT**
   - Should return HTTP 204 and remove the task when DELETE `/v1/tasks/{id}` finds it
   - Should return HTTP 404 with `"Task not found with id: {taskId}"` when the id does not exist
   - **Note:** The deleted event is [EVENT-003](../epic-3-event-streaming/EVENT-003-task-deleted-event.md). Publishing nothing on 404 is that story's unit test.
3. **Pact**
   - Should verify provider contract for DELETE `/v1/tasks/{id}` (HTTP 204 success)
4. **E2E**
   - Should delete task through UI (table delete button)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

