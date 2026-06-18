import requests
import config


class WireMockClient:
    def __init__(self):
        self._base_url = config.wiremock_url

    def clear_mocks(self) -> "WireMockClient":
        requests.delete(f"{self._base_url}/__admin/mappings").raise_for_status()
        return self

    def stub_task_validation(self, is_valid: bool) -> "WireMockClient":
        requests.post(
            f"{self._base_url}/__admin/mappings",
            json={
                "request": {
                    "method": "GET",
                    "urlPattern": "/api/tasks/.*/validation",
                },
                "response": {
                    "status": 200,
                    "jsonBody": {"valid": is_valid},
                },
            },
        ).raise_for_status()
        return self
