import requests
import config
from data.task_data import TaskData
from data.task_response import TaskResponse


class ApiClient:
    def __init__(self):
        self._base_url = f"{config.api_url}/v1"

    def create_task(self, task: TaskData) -> TaskResponse:
        response = requests.post(
            f"{self._base_url}/tasks",
            json={
                "title": task.title,
                "description": task.description,
                "status": task.status,
                "priority": task.priority,
            },
        )
        response.raise_for_status()
        data = response.json()
        return TaskResponse(**data)

    def delete_task(self, task_id: str) -> None:
        response = requests.delete(f"{self._base_url}/tasks/{task_id}")
        response.raise_for_status()

    def get_tasks(self) -> list[TaskResponse]:
        response = requests.get(f"{self._base_url}/tasks")
        response.raise_for_status()
        return [TaskResponse(**item) for item in response.json()]
