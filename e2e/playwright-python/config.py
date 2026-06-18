import os
from pathlib import Path
from dotenv import load_dotenv

_root = Path(__file__).parent
_local_env = _root / ".env.e2e.local"
_default_env = _root / ".env.e2e"

load_dotenv(_local_env if _local_env.exists() else _default_env)

base_url: str = os.getenv("E2E_TEST_ENV_URL", "http://localhost:5173")
api_url: str = os.getenv("E2E_API_URL", "http://localhost:8080")
wiremock_url: str = os.getenv("E2E_WIREMOCK_URL", "http://localhost:8085")
