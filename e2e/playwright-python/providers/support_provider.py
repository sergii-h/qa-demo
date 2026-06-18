from playwright.sync_api import Page
from support.api.api_client import ApiClient
from support.mocks.wiremock_client import WireMockClient
from support.mocks.api_route_mock import ApiRouteMock


class MockProvider:
    def __init__(self, page: Page):
        self.api = ApiRouteMock(page)


class SupportProvider:
    def __init__(self, page: Page):
        self.api = ApiClient()
        self.wiremock = WireMockClient()
        self.mock = MockProvider(page)
