from __future__ import annotations

from playwright.sync_api import Page, Route
from data.task_response import TaskResponse


class ApiRouteMock:
    def __init__(self, page: Page):
        self._page = page

    def create_task(self, response: TaskResponse) -> ApiRouteMock:
        self._route_method("POST", "**/v1/tasks", lambda route: route.fulfill(status=201, json=response.to_dict()))
        return self

    def get_tasks(self, response: list[TaskResponse]) -> ApiRouteMock:
        self._route_method(
            "GET", "**/v1/tasks", lambda route: route.fulfill(status=200, json=[r.to_dict() for r in response])
        )
        return self

    def get_task(self, task_id: str, response: TaskResponse) -> ApiRouteMock:
        self._route_method(
            "GET", f"**/v1/tasks/{task_id}", lambda route: route.fulfill(status=200, json=response.to_dict())
        )
        return self

    def delete_task(self, task_id: str) -> ApiRouteMock:
        self._route_method("DELETE", f"**/v1/tasks/{task_id}", lambda route: route.fulfill(status=204))
        return self

    def update_task(self, task_id: str, response: TaskResponse) -> ApiRouteMock:
        self._route_method(
            "PUT", f"**/v1/tasks/{task_id}", lambda route: route.fulfill(status=200, json=response.to_dict())
        )
        return self

    def get_is_valid(self, task_id: str, is_valid: bool) -> ApiRouteMock:
        self._route_method(
            "GET", f"**/v1/tasks/isValid/{task_id}", lambda route: route.fulfill(status=200, json=is_valid)
        )
        return self

    def _route_method(self, method: str, url: str, handler) -> ApiRouteMock:
        def _handler(route: Route) -> None:
            if route.request.method != method:
                route.fallback()
                return
            handler(route)

        self._page.route(url, _handler)
        return self
