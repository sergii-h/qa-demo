import uuid
from data.task_data import TaskData
from data.task_response import TaskResponse
from data.task_status import TaskStatus
from data.task_priority import TaskPriority


class TaskContext:
    def __init__(
        self,
        id: str | None = None,
        title: str | None = None,
        description: str | None = None,
        status: TaskStatus = TaskStatus.TODO,
        priority: TaskPriority = TaskPriority.MEDIUM,
    ):
        self.id = id or str(uuid.uuid4())
        self.title = title or f"Task-{str(uuid.uuid4()).split('-')[0]}"
        self.description = description or "Automated test task description"
        self.status = status
        self.priority = priority

    def create_task_data(self) -> TaskData:
        return TaskData(
            title=self.title,
            description=self.description,
            status=self.status,
            priority=self.priority,
        )

    def create_task_response(self) -> TaskResponse:
        return TaskResponse(
            id=self.id,
            title=self.title,
            description=self.description,
            status=self.status,
            priority=self.priority,
        )
