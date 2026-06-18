from dataclasses import dataclass
from data.task_status import TaskStatus
from data.task_priority import TaskPriority


@dataclass
class TaskData:
    title: str
    description: str
    status: TaskStatus
    priority: TaskPriority
